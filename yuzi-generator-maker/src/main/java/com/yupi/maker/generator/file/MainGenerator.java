package com.yupi.maker.generator.file;

import com.yupi.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/*
* 核心生成器
* */
public class MainGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {
        String inputRootPath = "d:\\IdeaProjects\\yuzi-generator\\yuzi-generator-demo-projects\\acm-template-pro\\";
        String outputRootPath = "d:\\IdeaProjects\\yuzi-generator\\acm-template-pro";

        String inputPath;
        String outputPath;

        inputPath = new File(inputRootPath,"src/com/yupi/acm/MainTemplate.java.ftl").getAbsolutePath();
        outputPath = new File(outputRootPath,"src/com/yupi/acm/MainTemplate.java").getAbsolutePath();
        DynamicFileGenerator.doGenerate(inputPath, outputPath, model);


        inputPath = new File(inputRootPath,".gitignore").getAbsolutePath();
        outputPath = new File(outputRootPath,".gitignore").getAbsolutePath();
        StaticFileGenerator.copyFileByHutools(inputPath,outputPath);

        inputPath = new File(inputRootPath,"README.md").getAbsolutePath();
        outputPath = new File(outputRootPath,"README.md").getAbsolutePath();
        StaticFileGenerator.copyFileByHutools(inputPath,outputPath);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        DataModel model = new DataModel();
        model.setAuthor("yupi");
        model.setLoop(false);
        model.setOutputText("求和结果：");
        doGenerate(model);
    }
}
