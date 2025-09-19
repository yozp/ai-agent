package com.yzj.imagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    //测试图片搜索服务
    @Test
    void searchImage() {
        String result = imageSearchTool.searchImage("computer");
        Assertions.assertNotNull(result);
    }
}
