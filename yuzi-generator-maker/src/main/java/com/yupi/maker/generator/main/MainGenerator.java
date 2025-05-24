package com.yupi.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.yupi.maker.generator.JarGenerator;
import com.yupi.maker.generator.ScriptGenerator;
import com.yupi.maker.generator.file.DynamicFileGenerator;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        // 测试能否获取到meta对象
        Meta meta = MetaManager.getMetaObject();
        // System.out.println(meta);

        // 输出根路径
        String projectPath = System.getProperty("user.dir");
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker
        // System.out.println(projectPath);
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker\generated\acm-template-pro-generator
        // System.out.println(outputPath);
        if(!FileUtil.exist(outputPath)){
            FileUtil.mkdir(outputPath);
        }

        //复制原始文件
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        // d:/IdeaProjects/yuzi-generator/yuzi-generator-demo-projects/acm-template-pro/
        // System.out.println(sourceRootPath);
        String sourceCopyDestPath = outputPath + File.separator + ".source" ;
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker\generated\acm-template-pro-generator\.source
        // System.out.println(sourceCopyDestPath);
        FileUtil.copy(sourceRootPath, sourceCopyDestPath, false);

        // 读取resourcees目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();
        // D:/IdeaProjects/yuzi-generator/yuzi-generator-maker/target/classes/
        // System.out.println(inputResourcePath);

        // java包基础路径
        String outputBasePackage = meta.getBasePackage();
        // System.out.println(outputBasePackage);  //com.yupi
        String outputBasePackagePath = StrUtil.join("\\", StrUtil.split(outputBasePackage,"."));
        // System.out.println(outputBasePackagePath);  //com/yupi
        String outputBaseJavaPackagePath = outputPath + File.separator + "src\\main\\java\\" + outputBasePackagePath;
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker\generated\acm-template-pro-generator\src\main\java\com\yupi
        // System.out.println(outputBaseJavaPackagePath);

        String inputFilePath ;
        String outputFilePath;

        // model.DataModel  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/model/DataModel.java.ftl";
        // D:/IdeaProjects/yuzi-generator/yuzi-generator-maker/target/classes/templates/java/model/DataModel.java.ftl
        // System.out.println(inputFilePath);
        outputFilePath = outputBaseJavaPackagePath + "\\model\\DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.ConfigCommand  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\cli\\command\\ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.GenerateCommand  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\cli\\command\\GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.ListCommand  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\cli\\command\\ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.CommandExecutor  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\cli\\CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // Main  注意斜杠路径的写法
        inputFilePath = inputResourcePath + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.DynamicGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\generator\\DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.MainGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\generator\\MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.StaticGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "\\generator\\StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + "\\pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // README.md
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath + "\\README.md";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);
        // 构建jar包
        JarGenerator.doGenerate(outputPath);

        // 封装脚本
        String shellOutputFilePath = outputPath + File.separator + "generator";
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);

        // 生成精简版的程序（产物包）
        String distOutputPath = outputPath + "-dist";
        // 拷贝jar包
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath = outputPath + File.separator + jarPath ;
        FileUtil.copy(jarAbsolutePath,targetAbsolutePath,true);
        // 拷贝脚本文件
        FileUtil.copy(shellOutputFilePath,distOutputPath,true);
        FileUtil.copy(shellOutputFilePath + ".bat",distOutputPath,true);
        // 拷贝源模板文件
        FileUtil.copy(sourceCopyDestPath,distOutputPath,true);
    }
}
