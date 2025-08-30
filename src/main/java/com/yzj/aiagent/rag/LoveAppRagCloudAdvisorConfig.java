package com.yzj.aiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 云知识库配置类
 */
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    // 定义一个Bean，用于检索增强建议器
    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        // 创建DashScope API实例，使用注入的API密钥
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
        // 定义知识库索引名称
        final String KNOWLEDGE_INDEX = "恋爱大师";
        // 创建文档检索器，配置DashScope文档检索选项
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        // 设置索引名称
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        // 构建并返回检索增强建议器
        return RetrievalAugmentationAdvisor.builder()
                // 设置文档检索器
                .documentRetriever(documentRetriever)
                .build();
    }
}
