package com.yupi.maker;

// import com.yupi.maker.cli.CommandExecutor;

import com.yupi.maker.generator.main.MainGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        // 测试命令
        //args = new String[]{"generate","-l","-a","-o"};
        //args = new String[]{"config"};
        //args = new String[]{"list"};
        //CommandExecutor commandExecutor = new CommandExecutor();
        //commandExecutor.doExecute(args);

        MainGenerator mainGenerator = new MainGenerator();
        mainGenerator.doGenerate();
    }
}
