package com.ysx.agent.rag.llm;

import com.ysx.agent.rag.search.YRAGSearch;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YRAGAgentTest {
    @Resource
    private YRAGAgent agent;

    @Resource
    private YRAGSearch yragSearch;

    @Test
    void testDoChatWithRag(){
        String collectionName="kb_"+1;
//        String queryText = yragSearch.doQueryRewrite("java中什么是包装类型？？？？");
        String result = agent.doChatWithRag(collectionName, "java中什么是包装类型？？？", "default");
        Assertions.assertNotNull(result);
        System.out.println(result);
    }
}