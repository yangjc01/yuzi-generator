package com.yupi.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;
import com.yupi.maker.template.model.FileFilterConfig;
import com.yupi.maker.template.model.TemplateMakerConfig;
import com.yupi.maker.template.model.TemplateMakerFileConfig;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;
import com.yupi.maker.template.model.TemplateMakerModelConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {

    /**
     *制作模板
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig){
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        Long id = templateMakerConfig.getId();
        return  makeTemplate(meta,originProjectPath,templateMakerFileConfig,templateMakerModelConfig,id);
    }

    /**
    * 制作文件模板
    * */

    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig,String sourceRootPath,File inputFile){
        // 要挖坑的文件的绝对路径（用于制作模板）
        // 注意win系统需要对路径的转义
        String fileInputAbsolutePath = inputFile.getAbsolutePath().replaceAll("\\\\","/");
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        // 文件输入输出相对路径（用于生成配置）
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath+"/","");
        String fileOutputPath = fileInputPath + ".ftl";

        String fileContent;
        // 如果已有模板文件，说明不是第一次制作，则在模板基础上再次挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if(hasTemplateFile){
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        String replacement ;

        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()){
            // 不是分组
            if(modelGroupConfig == null ){
                replacement = String.format("${%s}",modelInfoConfig.getFieldName());
            } else {
                // 是分组
                String groupKey = modelGroupConfig.getGroupKye();
                // 注意挖坑要多挖一个层级
                replacement = String.format("${%s.%s}",groupKey,modelInfoConfig.getFieldName());
            }
            newFileContent = StrUtil.replace(newFileContent,modelInfoConfig.getReplaceText(),replacement);
        }

        // 文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        // 注意文件输入路径要和输入路径反转，ftl模板路径要放到setInputPath中，方便以后生成模板文件。
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // 是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);

        // 之前不存在模板文件，并且没有更改文件内容，则为静态生成
        if(!hasTemplateFile){
            if(contentEquals){
                // fileInputPath是不带ftl结尾的路径
                // 前面setInputPath是设置的fileOutputPath，这个是有ftl结尾的，由于不需要生成模板文件，所以需要将setInputPath设置为没有ftl后缀fileInputPath。
                // 而前面的setOutputPath本来就设置的fileInputPath，所以不需要改变。
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                // 没有模板文件，需要挖坑，生成模板文件
                FileUtil.writeUtf8String(newFileContent,fileOutputAbsolutePath);
            }
        } else if (!contentEquals){
            // 有模板文件，且新增加了内容，生成模板文件
            FileUtil.writeUtf8String(newFileContent,fileOutputAbsolutePath);
        }

        return fileInfo;
    }

    /**
    * 制作模板
    * */

    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, Long id){
        // 没有id则生成
        if(id == null){
            id = IdUtil.getSnowflakeNextId();
        }

        // 复制目录
        // 指定原始项目路径
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;

        // 是否为首次制作模板
        // 目录不存在，则是首次制作,则需要复制目录
        if(!FileUtil.exist(templatePath)){
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath,templatePath,true);
        }

        //一、输入信息
        //2.输入文件信息
        // FileUtil.getLastPathEle 取originProjectPath的最后一级目录： acm-template
        // D:\IdeaProjects\yuzi-generator\yuzi-generator-maker\.temp\1945258343145541632\acm-template
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // win系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\","/");

        // 生成文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、生成配置文件，放到与springboot-init同级目录下，避免输入路径为根目录"./"时，meta.json也被制作成模板文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

        // 如果已有meta.json文件，说明不是第一次制作，则在meta基础上继续修改
        if(FileUtil.exist(metaOutputPath)){
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath),Meta.class);
            BeanUtil.copyProperties(newMeta,oldMeta, CopyOptions.create().ignoreNullValue());
            newMeta = oldMeta;

            // 追加配置参数
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
        } else {
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }
        // 2. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta),metaOutputPath);

        return id;
    }

    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 保存本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        // 非null校验
        if(templateMakerModelConfig == null){
            return newModelInfoList;
        }

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        // 非null校验
        if(CollUtil.isEmpty(models)){
            return newModelInfoList;
        }
        // 转换为配置接收的ModelInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig,modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if(modelGroupConfig != null ){
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKye();
            String groupName = modelGroupConfig.getGroupName();

            Meta.ModelConfig.ModelInfo groupModelInfo =  new Meta.ModelConfig.ModelInfo();
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setGroupName(groupName);
            groupModelInfo.setCondition(condition);

            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        // 二、生成文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        // 非空校验
        if(templateMakerFileConfig == null){
            return newFileInfoList;
        }


        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        // 非空校验
        if(CollUtil.isEmpty(fileInfoConfigList)){
            return newFileInfoList;
        }
        for(TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList){
            String inputFilePath = fileInfoConfig.getPath();

            // 如果填的是相对路径，要改为绝对路径
            if(!inputFilePath.startsWith(sourceRootPath)){
                inputFilePath = sourceRootPath + File.separator + inputFilePath;
            }

        /*
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;
            // 输入的是目录
            if(FileUtil.isDirectory(inputFileAbsolutePath)){
                List<File> fileList = FileUtil.loopFiles(inputFileAbsolutePath);
                for(File file : fileList){
                    Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(modelInfo,searchStr,sourceRootPath,file);
                    newFileInfoList.add(fileInfo);
                }
            } else {
                // 输入的是文件
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(modelInfo,searchStr,sourceRootPath,new File(inputFileAbsolutePath));
                newFileInfoList.add(fileInfo);
            }
        */

            // 获取过滤后的文件列表（不会存在目录）
            List<File> fileList = FileFilter.doFilter(inputFilePath,fileInfoConfig.getFilterConfigList());
            // 不处理已生成的ftl模板文件
            fileList = fileList.stream().filter(file->!file.getAbsolutePath().endsWith(".ftl")).collect(Collectors.toList());
            for(File file : fileList){
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath,file);
                newFileInfoList.add(fileInfo);
            }
        }

        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if(fileGroupConfig != null){
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            // 新增分组配置
            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);

            // 文件全放到一个分组内
            groupFileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    // 模型去重
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList){
        // 策略： 同分组内模型merge ,不同分组保留

        // 1. 有分组的，以组为单位
        Map<String,List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap =  modelInfoList
                .stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey));
        // 2. 同组内的模型配置合并
        // 保存每个组对应的合并后的对象map
        Map<String,Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for(Map.Entry<String,List<Meta.ModelConfig.ModelInfo>> entry: groupKeyModelInfoListMap.entrySet()){
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
            .flatMap(modelInfo -> modelInfo.getModels().stream()).collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName,o->o,(e,r)->r)).values());

            // 使用新的group 配置
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey,newModelInfo);
        }
        // 3.将模型分组添加到结果列表
        List<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        // 4.将未分组的模型添加到结果列表
        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = modelInfoList.stream().filter(modelInfo->StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupModelInfoList.stream().collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName,o->o,(e,r)->r)).values()));

        return resultList;
    }

    // 文件去重
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList){
        // 策略：同分组内文件merge ,不同分组保留
        // 1. 有分组的，以组为单位划分
        Map<String,List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList
                .stream()
                .filter(fileInfo -> StrUtil.isNotEmpty(fileInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey));
        // 2.同组内的文件配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String,Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for(Map.Entry<String,List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()){
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath,o -> o,(e,r) -> r)).values());

            // 使用新的group 配置
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey,newFileInfo);
        }

        // 3.将文件分组添加到结果列表
        List<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        // 4. 将未分组的文件添加到结果列表
        List<Meta.FileConfig.FileInfo> noGroupFileInfoList = fileInfoList.stream().filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupFileInfoList.stream().collect(Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath,o -> o,(e,r)->r)).values()));

        return resultList;
    }
/*
// 使用单元测试，就不需要main方法了
    public static void main(String[] args){

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

        // List<String> inputFilePathList = Arrays.asList(inputFilePath1,inputFilePath2);


        // 输入模型参数信息(首次)
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");
        modelInfo.setDefaultValue("sum = ");

        // 替换变量（首次）
        String searchStr = "Sum: ";

        long id = makeTemplate(meta,originProjectPath,inputFilePath,modelInfo,searchStr,null);
        // 1945478580362158080
        System.out.println(id);


        // 输入模型参数信息(第二次)

        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");



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



        // 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKye("mysql");
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

        // 替换变量（第二次）
        //String searchStr = "MainTemplate";

        // 替换变量（第三次）
        // String searchStr = "BaseResponse";

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


        // 第一次
        // long id = makeTemplate(meta,originProjectPath,inputFilePathList,modelInfo,searchStr,null);

        // 第二次需要
        // long id = makeTemplate(meta,originProjectPath,inputFilePathList,modelInfo,searchStr,1945478580362158080L);

        // 测试文件过滤
        long id = makeTemplate(meta,originProjectPath,templateMakerFileConfig,templateMakerModelConfig,1945478580362158080L);
        System.out.println(id);
        }
*/
}
