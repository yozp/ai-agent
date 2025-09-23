package com.yzj.aiagent.app;

import com.yzj.aiagent.advisor.MyLoggerAdvisor;
import com.yzj.aiagent.advisor.ReReadingAdvisor;
import com.yzj.aiagent.chatmemory.FileBasedChatMemory;
import com.yzj.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.yzj.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ToolCallback[] allTools;

    //mcp相关，为了部署上线，可以暂停掉
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 构造器会自动初始化
     * 这部分代码会在 Spring 创建 Bean 时执行
     */
    public LoveApp(ChatModel dashscopeChatModel) {
//        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        // 初始化基于文件的对话记忆（持久化）
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 基础多轮对话
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                        loveAppVectorStore, "xxx")
                )// 应用自定义的 RAG 检索增强服务
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
//        //这里因为有了自定义日志 Advisor，可以不输出
//        log.info("content: {}", content);
        return content;
    }

    /**
     * 基础多轮对话（SSE 流式传输）
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 基础多轮对话（结构化输出）
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()//创建聊天客户端构建器
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")//设置系统提示词
                .user(message)//设置用户消息
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))//配置对话记忆顾问
                .call()//执行调用并解析响应
                .entity(LoveReport.class);//记录日志并返回结果
//        //这里因为有了自定义日志 Advisor，可以不输出
//        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    /**
     * 基于RAG本地知识库的多轮对话
     */
    public String doChatWithRag(String message, String chatId) {
        //查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)//添加用户提示词（问题）
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)// 对话ID
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)// 检索的历史消息数量
                )//配置对话记忆顾问
//                .advisors(new MyLoggerAdvisor()) // 开启日志，便于观察效果
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))// 1.应用本地知识库问答
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))// 2.应用 RAG 检索增强服务（基于 PgVector 向量存储）
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                        loveAppVectorStore, "单身")
                )// 3.应用自定义的 RAG 检索增强服务
                .call()
                .chatResponse();
        chatClient.prompt().call().content();
        String content = chatResponse.getResult().getOutput().getText();
//        //这里因为有了自定义日志 Advisor，可以不输出
//        log.info("content: {}", content);
        return content;
    }

    /**
     * 基于RAG云知识库的多轮对话
     */
    public String doChatWithRag2(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                .advisors(new MyLoggerAdvisor()) // 开启日志，便于观察效果
                .advisors(loveAppRagCloudAdvisor)// 应用云知识库问答
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
//        //这里因为有了自定义日志 Advisor，可以不输出
//        log.info("content: {}", content);
        return content;
    }

    //--------------------------------------------------------------------------------------------------------------------------

    /**
     * AI 调用工具服务
     */
    public String doChatWithTools(String message,String chatId){
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse();
        String context = response.getResult().getOutput().getText();
        return context;
    }

    /**
     * AI 调用 MCP服务
     */
    public String doChatWithMcp(String message,String chatId){
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String context = response.getResult().getOutput().getText();
        return context;
    }

    //--------------------------------------------------------------------------------------------------------------------------

    /**
     * 恋爱报告类
     * 使用了 Java 14 引入的 Record 类语法，这是一种简洁的定义不可变数据载体的方式
     */
    record LoveReport(String title, List<String> suggestions) {
    }


}
