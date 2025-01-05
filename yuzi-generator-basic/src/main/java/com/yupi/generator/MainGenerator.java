package com.yupi.generator;

import com.yupi.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/*
* 核心生成器
* */
public class MainGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        //整个项目根路径
        File parentFile = new File(projectPath).getParentFile();
        //输入路径
        String inputPath = new File(parentFile,"yuzi-generator-demo-projects/acm-template").getAbsolutePath();
        //输出路径
        String outputPath = projectPath;
        //调用自己写的递归方法生成静态文件
        StaticGenerator.copyFilesByRecursive(inputPath,outputPath);
        //生成动态文件
        String inputDynamicFilePath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputDynamicFilePath = outputPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";
        DynamicGenerator.doGenerate(inputDynamicFilePath, outputDynamicFilePath, model);
    }
    public static void main(String[] args) throws TemplateException, IOException {
        //创建数据模型
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("yupi3");
        //不使用循环
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("求和结果3：");
        doGenerate(mainTemplateConfig);
    }
}