package com.yzj.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

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
	 */
	private AdvisedRequest before(AdvisedRequest request) {
		log.info("AI Request: {}", request.userText());
		return request;
	}

	/**
	 * 在响应处理后记录 AI 回复的文本
	 */
	private void observeAfter(AdvisedResponse advisedResponse) {
		log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
	}

	/**
	 * 同步调用环绕处理方法
	 * @param advisedRequest 请求对象
	 * @param chain 调用链，用于继续执行后续处理
	 * @return 处理后的响应对象
	 */
	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		// 1. 调用前置处理记录请求日志
		advisedRequest = this.before(advisedRequest);
		// 2. 执行调用链获取响应
		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
		// 3. 调用后置处理记录响应日志
		this.observeAfter(advisedResponse);
		return advisedResponse;
	}

	/**
	 * 流式调用环绕处理方法
	 * @param advisedRequest 请求对象
	 * @param chain 调用链，用于继续执行后续处理
	 * @return 处理后的响应流
	 */
	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		// 1. 调用前置处理记录请求日志
		advisedRequest = this.before(advisedRequest);
		// 2. 执行调用链获取响应流
		Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
		// 3. 使用消息聚合器聚合流式响应，并在完成后记录日志
 		return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
	}
}
