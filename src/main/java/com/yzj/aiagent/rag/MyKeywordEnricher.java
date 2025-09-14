package com.yzj.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 AI 的文档元信息增强器（为文档补充元信息）
 * 本项目该增强器暂时只应用在本地的RAG知识库
 */
@Component
class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    //主要功能是为每个文档生成关键词并添加到其元数据中
    List<Document> enrichDocuments(List<Document> documents) {
        // 参数1: 使用的AI聊天模型（此处为注入的dashscopeChatModel）
        // 参数2: 5 - 表示希望为每个文档生成的关键词数量
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.dashscopeChatModel, 5);
        return enricher.apply(documents);
    }
}