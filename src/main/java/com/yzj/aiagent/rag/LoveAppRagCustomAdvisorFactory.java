package com.yzj.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 * 工厂模式
 */
@Slf4j
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 创建LoveApp自定义RAG检索增强顾问
     * 该方法用于构建一个根据状态过滤的文档检索增强顾问
     *
     * @param vectorStore 向量存储实例，用于文档检索
     * @param filename    文档过滤条件，用于筛选特定条件的文档
     * @return 配置好的RetrievalAugmentationAdvisor实例，可用于增强AI对话的检索能力
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String filename) {
        // 构建过滤表达式：只检索filename字段
        Filter.Expression expression = new FilterExpressionBuilder()
                .nin("filename", filename) // 添加不包含条件：字段"filename"的值不能包含参数filename
                .build(); // 构建过滤表达式

        // 创建文档检索器，配置检索参数
        // VectorStoreDocumentRetriever是DocumentRetriever的一个实现类
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore) // 设置向量存储源
                .filterExpression(expression) // 设置过滤条件：按状态过滤文档
                .similarityThreshold(0.5) // 设置相似度阈值：只返回相似度大于0.5的文档（0-1范围）
                .topK(3) // 设置返回文档数量：最多返回3个最相关的文档
                .build(); // 构建DocumentRetriever实例

        // 创建并返回检索增强顾问
        // 该顾问将在AI生成回答时，自动从vectorStore中检索相关文档作为上下文
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever) // 设置文档检索器
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())//设置空上下文时的回复
                .build(); // 构建RetrievalAugmentationAdvisor实例
    }
}