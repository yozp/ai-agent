package com.yzj.aiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆实现类
 * 使用Kryo序列化框架将对话数据存储到文件中
 */
public class FileBasedChatMemory implements ChatMemory{

    // 基础存储目录路径
    private final String BASE_DIR;
    // Kryo序列化实例，静态共享以提高性能
    private static final Kryo kryo = new Kryo();

    // 静态初始化块，配置Kryo序列化器
    static {
        // 关闭注册要求，允许序列化任意类
        kryo.setRegistrationRequired(false);
        // 设置实例化策略为标准策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 构造函数
     * @param dir 文件存储的基础目录路径
     */
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        // 确保基础目录存在，不存在则创建
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 向指定对话添加消息列表
     * @param conversationId 对话ID
     * @param messages 要添加的消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取现有对话或创建新对话
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        // 添加新消息到对话中
        conversationMessages.addAll(messages);
        // 保存更新后的对话到文件
        saveConversation(conversationId, conversationMessages);
    }

    /**
     * 获取指定对话的最后N条消息
     * @param conversationId 对话ID
     * @param lastN 要获取的消息数量
     * @return 最后N条消息的列表
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 获取对话中的所有消息
        List<Message> allMessages = getOrCreateConversation(conversationId);
        // 使用流处理获取最后N条消息
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    /**
     * 清除指定对话的所有消息
     * @param conversationId 对话ID
     */
    @Override
    public void clear(String conversationId) {
        // 获取对话对应的文件
        File file = getConversationFile(conversationId);
        // 如果文件存在则删除
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取或创建对话消息列表
     * 如果文件存在则从文件中读取，否则创建新列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        // 如果对话文件存在，则从中读取消息
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                // 使用Kryo反序列化消息列表
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存对话消息到文件
     * @param conversationId 对话ID
     * @param messages 要保存的消息列表
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            // 使用Kryo序列化消息列表到文件
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据对话ID生成对应的文件对象
     * @param conversationId 对话ID
     * @return 对应的文件对象
     */
    private File getConversationFile(String conversationId) {
        // 文件名为对话ID加上.kryo扩展名
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}