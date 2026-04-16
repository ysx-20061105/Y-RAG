package com.ysx.agent.service.impl;

import com.ysx.agent.rag.search.RetrievalResult;
import com.ysx.agent.rag.search.YRAGSearch;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HybridSearchServiceImplTest {

    @Resource
    private YRAGSearch yragSearch;

    @Resource
    private HybridSearchServiceImpl hybridSearchService;

    @Test
    void doSearch(){
        String collectionName="kb_"+1;
        String message = "java中什么是包装类型？？？";
        String rewrittenMessage = yragSearch.doQueryRewrite(message);

        // 使用混合检索获取文档
        RetrievalResult retrievalResult = hybridSearchService.search(rewrittenMessage, collectionName, null);
        System.out.println(retrievalResult);
    }
}