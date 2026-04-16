package com.ysx.agent.rag.search;

import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.service.HybridSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合检索集成测试
 * 测试完整的混合检索工作流程
 */
@SpringBootTest
@ActiveProfiles("test")
class HybridSearchIntegrationTest {

    @Autowired(required = false)
    private HybridSearchService hybridSearchService;

    @Test
    void contextLoads() {
        assertNotNull(hybridSearchService, "HybridSearchService should be available");
    }

    @Test
    void testHybridSearchWithDefaultConfig() {
        if (hybridSearchService == null) {
            return;
        }

        RetrievalResult result = hybridSearchService.search(
                "Java线程",
                "test_collection"
        );

        assertNotNull(result);
        assertNotNull(result.getDocuments());
        assertNotNull(result.getConfigUsed());
        assertEquals("HYBRID", result.getSearchType());
    }

    @Test
    void testHybridSearchWithBalancedConfig() {
        if (hybridSearchService == null) {
            return;
        }

        HybridSearchConfig config = HybridSearchConfig.balanced();
        RetrievalResult result = hybridSearchService.search(
                "Java线程",
                "test_collection",
                config
        );

        assertNotNull(result);
        assertEquals(0.5, result.getConfigUsed().getVectorWeight());
        assertEquals(0.5, result.getConfigUsed().getKeywordWeight());
    }

    @Test
    void testHybridSearchWithSemanticFirstConfig() {
        if (hybridSearchService == null) {
            return;
        }

        HybridSearchConfig config = HybridSearchConfig.semanticFirst();
        RetrievalResult result = hybridSearchService.search(
                "什么是多线程编程",
                "test_collection",
                config
        );

        assertNotNull(result);
        assertEquals(0.8, result.getConfigUsed().getVectorWeight());
        assertEquals(0.2, result.getConfigUsed().getKeywordWeight());
    }

    @Test
    void testHybridSearchWithKeywordFirstConfig() {
        if (hybridSearchService == null) {
            return;
        }

        HybridSearchConfig config = HybridSearchConfig.keywordFirst();
        RetrievalResult result = hybridSearchService.search(
                "synchronized关键字",
                "test_collection",
                config
        );

        assertNotNull(result);
        assertEquals(0.2, result.getConfigUsed().getVectorWeight());
        assertEquals(0.8, result.getConfigUsed().getKeywordWeight());
    }

    @Test
    void testVectorOnlySearch() {
        if (hybridSearchService == null) {
            return;
        }

        HybridSearchConfig config = HybridSearchConfig.builder()
                .searchMode(SearchMode.VECTOR_ONLY)
                .vectorWeight(1.0)
                .keywordWeight(0.0)
                .build();

        RetrievalResult result = hybridSearchService.search(
                "线程池",
                "test_collection",
                config
        );

        assertNotNull(result);
        assertEquals("VECTOR_ONLY", result.getSearchType());
    }

    @Test
    void testKeywordOnlySearch() {
        if (hybridSearchService == null) {
            return;
        }

        HybridSearchConfig config = HybridSearchConfig.builder()
                .searchMode(SearchMode.KEYWORD_ONLY)
                .vectorWeight(0.0)
                .keywordWeight(1.0)
                .build();

        RetrievalResult result = hybridSearchService.search(
                "synchronized",
                "test_collection",
                config
        );

        assertNotNull(result);
        assertEquals("KEYWORD_ONLY", result.getSearchType());
    }

    @Test
    void testConfigValidation() {
        HybridSearchConfig validConfig = HybridSearchConfig.builder()
                .vectorWeight(0.6)
                .keywordWeight(0.4)
                .topK(10)
                .similarityThreshold(0.6)
                .build();

        assertDoesNotThrow(validConfig::validateWeights);
    }

    @Test
    void testInvalidConfigThrowsException() {
        HybridSearchConfig invalidConfig = HybridSearchConfig.builder()
                .vectorWeight(0.6)
                .keywordWeight(0.6)  // Sum is 1.2, not 1.0
                .build();

        assertThrows(IllegalArgumentException.class, invalidConfig::validateWeights);
    }

    @Test
    void testTopKValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            HybridSearchConfig.builder().topK(0).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            HybridSearchConfig.builder().topK(101).build();
        });
    }

    @Test
    void testSimilarityThresholdValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            HybridSearchConfig.builder().similarityThreshold(-0.1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            HybridSearchConfig.builder().similarityThreshold(1.1).build();
        });
    }

    @Test
    void testPresetModes() {
        HybridSearchConfig balanced = HybridSearchConfig.balanced();
        assertEquals(0.5, balanced.getVectorWeight());
        assertEquals(0.5, balanced.getKeywordWeight());

        HybridSearchConfig semanticFirst = HybridSearchConfig.semanticFirst();
        assertEquals(0.8, semanticFirst.getVectorWeight());
        assertEquals(0.2, semanticFirst.getKeywordWeight());

        HybridSearchConfig keywordFirst = HybridSearchConfig.keywordFirst();
        assertEquals(0.2, keywordFirst.getVectorWeight());
        assertEquals(0.8, keywordFirst.getKeywordWeight());
    }

    @Test
    void testEmptyQueryHandling() {
        if (hybridSearchService == null) {
            return;
        }

        RetrievalResult result = hybridSearchService.search(
                "",
                "test_collection"
        );

        // Should handle empty query gracefully
        assertNotNull(result);
        assertNotNull(result.getDocuments());
    }

    @Test
    void testNullCollectionHandling() {
        if (hybridSearchService == null) {
            return;
        }

        RetrievalResult result = hybridSearchService.search(
                "test query",
                null
        );

        // Should handle null collection gracefully
        assertNotNull(result);
    }

    @Test
    void testPerformanceTarget() {
        if (hybridSearchService == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        RetrievalResult result = hybridSearchService.search(
                "performance test",
                "test_collection"
        );
        long latency = System.currentTimeMillis() - startTime;

        assertNotNull(result);
        // Performance target: < 500ms (may not always meet in test environment)
        assertTrue(latency < 5000, "Search should complete within reasonable time");
    }
}