package com.yupi.maker.template;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;
import com.yupi.maker.template.model.*;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TemplateMakerTest {
    /**
     * 测试同配置多次生成时，会强制变为静态生成bug
     */

    @Test
    public void makeTemplate() {
        // 构造配置参数
        Meta meta = new Meta();
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");


        // 指定原始项目路径
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker
        String projectPath = System.getProperty("user.dir");
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-demo-projects/acm-template
        // String originProjectPath = new File(projectPath).getParent() + File.separator + "yuzi-generator-demo-projects/acm-template";
        // String inputFilePath = "src/com/yupi/acm/MainTemplate.java";

        // 更换springboot的目录
        String originProjectPath = new File(projectPath).getParent() + File.separator + "yuzi-generator-demo-projects/springboot-init";
        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";
        // String inputFilePath2 = "src/main/java/com/yupi/springbootinit/controller";
        // 换一个新的目录，测试追加的功能
        // String inputFilePath2 = "src/main/java/com/yupi/springbootinit/constant";
        // 测试yml文件挖多个坑
        String inputFilePath2 = "src/main/resources/application.yml";


        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
/*
        // 不分组的配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig.setFieldName("className");
        modelInfoConfig.setType("String");
        modelInfoConfig.setDefaultValue("BaseResponse");
        modelInfoConfig.setReplaceText("BaseResponse");
        // 不分组的配置
        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig);
*/

        // 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1,modelInfoConfig2);

        templateMakerModelConfig.setModels(modelInfoConfigList);

        // 文件过滤
        // 只返回inputFilePath1路径下文件名包含Base的
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileInfoConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileInfoConfig);
        fileInfoConfig1.setFilterConfigList(fileFilterConfigList);
        // 返回inputFilePath2下的所有文件
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(inputFilePath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1,fileInfoConfig2));

        // 分组配置
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText");
        fileGroupConfig.setGroupKey("test");
        fileGroupConfig.setGroupName("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        // 测试文件过滤
        long id = TemplateMaker.makeTemplate(meta,originProjectPath,templateMakerFileConfig,templateMakerModelConfig,null,1945478580362158080L);
        System.out.println("id的值为："+id);
    }

    /**
     * 同文件目录多次生成时，会扫码新生成的ftl文件 bug ,过滤掉ftl文件。
     */
    @Test
    public void makeTemplate2(){
        // 构造配置参数
        Meta meta = new Meta();
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");


        // 指定原始项目路径
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker
        String projectPath = System.getProperty("user.dir");
        // 更换springboot的目录
        String originProjectPath = new File(projectPath).getParent() + File.separator + "yuzi-generator-demo-projects/springboot-init";
        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";

        // 文件参数配置
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));


        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // 不分组的配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig.setFieldName("className");
        modelInfoConfig.setType("String");
        modelInfoConfig.setDefaultValue("BaseResponse");
        modelInfoConfig.setReplaceText("BaseResponse");
        // 不分组的配置
        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig);

        templateMakerModelConfig.setModels(modelInfoConfigList);

        long id = TemplateMaker.makeTemplate(meta,originProjectPath,templateMakerFileConfig,templateMakerModelConfig,null,1945478580362158080L);
        System.out.println("id的值为："+id);
    }

    @Test
    public void testMakeTemplateWithJSON(){
        String configStr = ResourceUtil.readUtf8Str("templateMaker.json");
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        Long id = TemplateMaker.makeTemplate(templateMakerConfig);
        System.out.println("id的值为："+id);
    }

    @Test
    public void makeSpringBootTemplate(){
        String rootPath = "examples/springboot-init/";
        String configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker.json");
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        Long id = TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker1.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker2.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker3.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker4.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker5.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker6.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker7.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath+"templateMaker8.json");
        templateMakerConfig = JSONUtil.toBean(configStr,TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        System.out.println("id的值为："+id);
    }
}
