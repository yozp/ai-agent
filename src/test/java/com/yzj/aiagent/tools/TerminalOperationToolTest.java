package com.yzj.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TerminalOperationToolTest {

    //测试终端操作功能
    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "dir";//显示当前目录下的文件和子目录
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
    }
}
