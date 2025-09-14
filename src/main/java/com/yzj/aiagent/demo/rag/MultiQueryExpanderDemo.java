package com.yzj.aiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询扩展器组件
 * 针对用户的模糊提示词进行改写
 */
@Component
public class MultiQueryExpanderDemo {

    // 用于构建ChatClient的构造器，ChatClient是与AI模型交互的客户端
    private final ChatClient.Builder chatClientBuilder;

    //构造函数，通过依赖注入获取ChatModel实例并初始化ChatClient
    public MultiQueryExpanderDemo(ChatModel dashscopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
    }

    //使用AI模型分析原始查询，从不同角度生成多个相似的查询问题
    public List<Query> expand(String query) {
        // 构建多查询扩展器实例
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)// 设置AI客户端构造器
                .numberOfQueries(3)// 指定要生成的查询变体数量，这里要求生成3个
                .build();// 构建MultiQueryExpander实例
        List<Query> queries = queryExpander.expand(new Query(query));
        return queries;
    }
}
