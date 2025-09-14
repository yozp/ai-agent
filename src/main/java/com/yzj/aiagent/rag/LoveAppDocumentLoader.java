package com.yzj.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地文档加载器类
 * 负责从类路径加载Markdown文件并将其转换为Document对象
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    //spring的资源解析类
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 Markdown 文档
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
//                // 提取文档倒数第 3 和第 2 个字作为标签（太生硬，不推荐）
//                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                // 配置Markdown文档读取器
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()//创建一个配置建造器
                        .withHorizontalRuleCreateDocument(true)//按章节分割成多个小文档，如遇到 (--- 或 ***)
                        .withIncludeCodeBlock(false)//设置解析时不包含代码块（``` 中的内容）
                        .withIncludeBlockquote(false)//设置解析时不包含引用块 (> 开头的内容）
                        .withAdditionalMetadata("filename", filename)//向生成的每个 Document 对象中添加一个额外的元数据键值对，键是 "filename"
//                        .withAdditionalMetadata("status", status)//为每篇文章添加特定标签，例如"状态"
                        .build();//生成一个 MarkdownDocumentReaderConfig 配置对象
                // 创建Markdown文档读取器实例
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
