package com.yzj.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ManusTest {
  
    @Resource
    private Manus Manus;

    //测试AI智能体
    @Test
    void run() {  
        String userPrompt = """  
                你好，我叫yunikon，我现在在广州大学城，
                请帮我找到不超过三个的 5 公里内合适的约会地点，
                并且制定一份详细的约会计划，并将计划的内容以 PDF 格式输出""";
        String answer = Manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }  
}
