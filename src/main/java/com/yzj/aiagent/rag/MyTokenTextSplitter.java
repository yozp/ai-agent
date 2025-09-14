package com.yzj.aiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义令牌文本分割器组件（不推荐）
 */
@Component
class MyTokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        // 创建默认配置的令牌文本分割器
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        // 创建自定义配置的令牌文本分割器
        // 200 - 每个分割块的最大令牌数
        // 100 - 分割块之间的重叠令牌数（用于保持上下文连贯性）
        // 10 - 最小分割块大小（令牌数）
        // 5000 - 处理的最大令牌数限制
        // true - 是否包含原始文档的元数据
        TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 10, 5000, true);
        return splitter.apply(documents);
    }
}
