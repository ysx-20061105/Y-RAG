package com.ysx.agent.rag.etl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.ai.vectorstore.qdrant")
public class QdrantProperties {
    private String host = "localhost";
    private int port = 6334;
    private boolean initializeSchema = true;
    private String collectionNamePrefix = "kb_";
    private boolean enabled = true;

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public boolean isInitializeSchema() {
        return initializeSchema;
    }
    public void setInitializeSchema(boolean initializeSchema) {
        this.initializeSchema = initializeSchema;
    }
    public String getCollectionNamePrefix() {
        return collectionNamePrefix;
    }
    public void setCollectionNamePrefix(String collectionNamePrefix) {
        this.collectionNamePrefix = collectionNamePrefix;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
