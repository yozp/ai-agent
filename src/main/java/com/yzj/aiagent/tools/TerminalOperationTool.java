package com.yzj.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 终端操作工具类
 */
public class TerminalOperationTool {

    /**
     * 非windows操作系统在终端中执行指定的命令并返回执行结果
     */
    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder(); // 用于存储命令输出结果
        try {

//            // 1.windows操作系统执行终端命令
//            ProcessBuilder builder = new ProcessBuilder("cmd.exe  ", "/c", command);
//            Process process = builder.start();
            // 2.非windows操作系统执行终端命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令的标准输出流
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                // 逐行读取输出内容
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            // 等待命令执行完成并获取退出码
            int exitCode = process.waitFor();
            // 检查命令是否执行成功（退出码为0表示成功）
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException e) {
            // 处理IO异常（如命令不存在、无法执行等）
            output.append("Error executing command: ").append(e.getMessage());
        } catch (InterruptedException e) {
            // 处理线程中断异常
            output.append("Command execution interrupted: ").append(e.getMessage());
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
        return output.toString();
    }
}