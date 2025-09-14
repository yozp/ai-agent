package com.yzj.aiagent.rag;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义批处理策略
 * 处理文本嵌入任务时，根据token数量进行智能批处理，避免超出模型限制
 */
@Configuration
public class EmbeddingConfig {
    @Bean
    public BatchingStrategy customTokenCountBatchingStrategy() {
        return new TokenCountBatchingStrategy(
            EncodingType.CL100K_BASE,  // 指定使用OpenAI的CL100K编码器（GPT-4等模型使用）
            8000,                      // 最大token数量限制，接近模型上下文长度限制（如8192）
            0.1                        // 10%的保留比例，可能用于缓冲或安全边际
        );
    }
}
