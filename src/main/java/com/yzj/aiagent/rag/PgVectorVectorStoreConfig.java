package com.yzj.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * PgVector 向量存储配置类
 */
@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 创建并配置PgVector向量存储Bean
     *
     * @param jdbcTemplate Spring JDBC模板，用于数据库操作
     * @param dashscopeEmbeddingModel 嵌入模型，用于将文本转换为向量表示
     * @return 配置好的VectorStore实例
     */
    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 使用建造者模式创建PgVectorStore实例
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 可选: 向量维度，默认为模型尺寸或1536
                .distanceType(COSINE_DISTANCE)       // 可选：向量相似度计算方式，默认为COSINE_DISTANCE（余弦距离）
                .indexType(HNSW)                     // 可选：向量索引类型，默认为HNSW（分层可导航小世界算法）
                .initializeSchema(true)              // 可选：是否自动初始化数据库表结构，默认为false
                .schemaName("public")                // 可选：数据库schema名称，默认为"public"
                .vectorTableName("vector_store")     // 可选：向量存储表名，默认为"vector_store"
                .maxDocumentBatchSize(10000)         // 可选：批量处理文档的最大数量，默认为10000
                .build();
//        // 可选择加载本地文档
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        vectorStore.add(documents);
        return vectorStore;
    }
}