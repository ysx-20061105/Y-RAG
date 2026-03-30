package com.ysx.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.ysx.agent.domain.UserAccount;
import com.ysx.agent.dto.AuthResponse;
import com.ysx.agent.dto.LoginRequest;
import com.ysx.agent.dto.RegisterRequest;
import com.ysx.agent.mapper.UserAccountMapper;
import com.ysx.agent.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long BLOCK_MILLIS = 15 * 60 * 1000L;

    private final UserAccountMapper userAccountMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    // 简单内存级登录失败计数，后续可迁移到集中缓存
    private final ConcurrentMap<String, FailureState> loginFailures = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserAccountMapper userAccountMapper,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long register(RegisterRequest request) {
        boolean hasPhone = request.getPhone() != null && !request.getPhone().isBlank();
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        if (!hasPhone && !hasEmail) {
            throw new IllegalArgumentException("手机号和邮箱至少填写一个");
        }

        UserAccount account = new UserAccount();
        if (hasPhone) {
            account.setUsername(request.getPhone());
        }
        if (hasEmail) {
            account.setEmail(request.getEmail());
        }
        String encoded = passwordEncoder.encode(request.getPassword());
        account.setPassword_hash(encoded);
        account.setStatus(0);
        Date now = new Date();
        account.setCreated_at(now);
        account.setUpdated_at(now);

        userAccountMapper.insert(account);
        return account.getId();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        LoginRequest.IdentifierType type = request.getIdentifierType();
        String key = type + ":" + identifier;
        long now = System.currentTimeMillis();

        FailureState state = loginFailures.get(key);
        if (state != null && state.blockedUntil > now) {
            log.warn("Login blocked for {} due to too many attempts", key);
            throw new IllegalArgumentException("TOO_MANY_ATTEMPTS");
        }

        UserAccount account = findByIdentifier(type, identifier);

        if (account == null) {
            recordFailure(key, now, "BAD_CREDENTIALS");
            throw new IllegalArgumentException("BAD_CREDENTIALS");
        }

        if (account.getStatus() != null && account.getStatus() != 0) {
            recordFailure(key, now, "ACCOUNT_STATUS_INVALID");
            throw new IllegalArgumentException("ACCOUNT_STATUS_INVALID");
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), account.getPassword_hash());
        if (!matches) {
            recordFailure(key, now, "BAD_CREDENTIALS");
            throw new IllegalArgumentException("BAD_CREDENTIALS");
        }

        // 登录成功，清理失败计数
        resetFailure(key);

        // 建立 Sa-Token 会话
        StpUtil.login(account.getId());
        String token = StpUtil.getTokenValue();
        long timeout = StpUtil.getTokenTimeout();
        Long expiresIn = timeout > 0 ? timeout : null;

        log.info("LoginSuccess userId={} type={}", account.getId(), type);

        return new AuthResponse(account.getId(), token, expiresIn);
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private UserAccount findByIdentifier(LoginRequest.IdentifierType type, String identifier) {
        if (type == LoginRequest.IdentifierType.PHONE) {
            return userAccountMapper.selectByUsernameForLogin(identifier);
        }
        return userAccountMapper.selectByEmailForLogin(identifier);
    }

    private void recordFailure(String key, long now, String reason) {
        loginFailures.compute(key, (k, old) -> {
            FailureState s = (old == null) ? new FailureState() : old;
            s.count++;
            if (s.count >= MAX_LOGIN_FAILURES) {
                s.blockedUntil = now + BLOCK_MILLIS;
            }
            return s;
        });
        log.warn("LoginFailure key={} reason={}", key, reason);
    }

    private void resetFailure(String key) {
        loginFailures.remove(key);
    }

    private static class FailureState {
        int count;
        long blockedUntil;
    }
}
