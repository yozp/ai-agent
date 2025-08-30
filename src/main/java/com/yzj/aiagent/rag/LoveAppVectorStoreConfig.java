package com.yzj.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量存储配置类
 * 配置和初始化应用程序的向量存储组件
 */
@Configuration // 声明这是一个Spring配置类，用于定义Bean
public class LoveAppVectorStoreConfig {

    // 注入文档加载器组件，用于加载Markdown文档
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 创建并配置向量存储Bean
     * @param dashscopeEmbeddingModel 嵌入模型，用于将文本转换为向量表示
     * @return 配置好的向量存储实例
     */
    @Bean // 声明该方法返回一个由Spring管理的Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 构建简单的向量存储实例，传入嵌入模型
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 使用文档加载器加载所有Markdown文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 将加载的文档添加到向量存储中
        // 文档会被自动转换为向量并建立索引
        simpleVectorStore.add(documents);
        // 返回配置好的向量存储实例
        return simpleVectorStore;
    }
}