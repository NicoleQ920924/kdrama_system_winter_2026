package com.kdrama.backend.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AiConfiguration {

    /**
     * Configure PgVectorStore for semantic search using PostgreSQL pgvector extension
     */
    @Bean
    @ConditionalOnProperty(name = "spring.ai.vectorstore.pgvector.enabled", havingValue = "true", matchIfMissing = true)
    public PgVectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
    }

}
