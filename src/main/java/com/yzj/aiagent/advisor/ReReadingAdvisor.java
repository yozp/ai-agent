package com.yzj.aiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力（高成本）
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 返回当前类的简单名称作为 Advisor 标识
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 返回执行顺序值，数值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 在请求处理前记录用户输入的文本
     * 让AI更仔细地阅读问题（通过重复强调）
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {

        // 创建原用户参数的副本（避免修改原始数据）
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());
        //添加新参数，将用户原始查询文本存储到 re2_input_query 键中
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        //{re2_input_query} 这是模板占位符，在实际处理时会被替换为真正的值
        return AdvisedRequest.from(advisedRequest) // 基于原请求创建Builder
                .userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """) // 设置新的用户文本
                .userParams(advisedUserParams) // 设置新的参数映射
                .build(); // 构建最终对象
    }

    /**
     * 同步调用环绕处理方法
     * @param advisedRequest 请求对象
     * @param chain 调用链，用于继续执行后续处理
     * @return 处理后的响应对象
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    /**
     * 流式调用环绕处理方法
     * @param advisedRequest 请求对象
     * @param chain 调用链，用于继续执行后续处理
     * @return 处理后的响应流
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }
}