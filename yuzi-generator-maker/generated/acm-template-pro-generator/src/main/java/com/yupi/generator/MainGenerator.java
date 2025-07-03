package com.yupi.generator;

import com.yupi.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/*
* 定义宏，重复使用模板
* */

/*
* 核心生成器
* */
public class MainGenerator {
    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        String inputRootPath = ".source/acm-template-pro/";
        String outputRootPath = "generated";

        String inputPath;
        String outputPath;

            boolean loop = model.loop;
            boolean needGit = model.needGit;
            String author = model.author;
            String outputText = model.outputText;

                    if(needGit){
               inputPath = new File(inputRootPath,".gitignore").getAbsolutePath();
               outputPath = new File(outputRootPath,".gitignore").getAbsolutePath();
                   StaticGenerator.copyFileByHutools(inputPath,outputPath);
               inputPath = new File(inputRootPath,"README.md").getAbsolutePath();
               outputPath = new File(outputRootPath,"README.md").getAbsolutePath();
                   StaticGenerator.copyFileByHutools(inputPath,outputPath);
                    }
               inputPath = new File(inputRootPath,"src/com/yupi/acm/MainTemplate.java.ftl").getAbsolutePath();
               outputPath = new File(outputRootPath,"src/com/yupi/acm/MainTemplate.java").getAbsolutePath();
                   DynamicGenerator.doGenerate(inputPath, outputPath, model);
    }
}
