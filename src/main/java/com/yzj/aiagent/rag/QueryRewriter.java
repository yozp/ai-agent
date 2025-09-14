package com.yzj.aiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器组件
 * 该类负责使用AI模型对用户输入的查询进行重写优化，使其更适合后续的检索或处理。
 */
@Component
public class QueryRewriter {

    // 查询转换器接口，用于执行具体的查询重写逻辑
    private final QueryTransformer queryTransformer;

    //构造函数，通过依赖注入获取ChatModel实例并初始化查询转换器
    public QueryRewriter(ChatModel dashscopeChatModel) {
        // 使用注入的AI模型构建ChatClient构造器
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        //初始化查询转换器，RewriteQueryTransformer是QueryTransformer的一个具体的实现
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    //查询重写
    public String doQueryRewrite(String prompt) {
        // 原始查询字符串包装成Query对象，然后执行查询重写转换
        Query transformedQuery = queryTransformer.transform(new Query(prompt));
        // 从重写后的Query对象中提取文本内容并返回
        return transformedQuery.text();
    }
}