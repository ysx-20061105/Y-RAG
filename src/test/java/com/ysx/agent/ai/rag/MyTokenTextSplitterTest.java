/*
package com.ysx.agent.ai.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyTokenTextSplitterTest {
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Test
    void split() {
        Document doc1 = new Document("This is a long piece of text that needs to be split into smaller chunks for processing.",
                Map.of("source", "example.txt"));
        Document doc2 = new Document("Another document with content that will be split based on token count.",
                Map.of("source", "example2.txt"));
        ArrayList<Document> documentLoaders = new ArrayList<>();
        documentLoaders.add(doc1);
        documentLoaders.add(doc2);
        List<Document> documents = myTokenTextSplitter.splitCustomized(documentLoaders);
        assertEquals(2, documents.size());
    }
}*/
