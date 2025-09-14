package com.yzj.aiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 自定义错误处理工厂类
 * 工厂模式：负责创建和配置特定的 ContextualQueryAugmenter 实例
 */
public class LoveAppContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter createInstance() {
        // 当用户的问题与恋爱主题无关时，使用此模板回复用户
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系管理员
                """);
        // 使用建造者模式构建并配置 ContextualQueryAugmenter 实例
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)//不允许空上下文
                .emptyContextPromptTemplate(emptyContextPromptTemplate)//设置空上下文时的回复模板
                .build();
    }
}
