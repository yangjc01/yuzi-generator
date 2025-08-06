package com.yupi.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;
import com.yupi.maker.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MetaValidator {
    public static void doValidAndFill(Meta meta){
        validAndFillMetaRoot(meta);
        validAndFillFileConfig(meta);
        validAndFillModelConfig(meta);
    }

    private static void validAndFillModelConfig(Meta meta) {
        // modelConfig 校验和默认值
        Meta.ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfig.ModelInfo> modelInfoList = modelConfig.getModels();
        if(CollectionUtil.isNotEmpty(modelInfoList)){
            for(Meta.ModelConfig.ModelInfo modelInfo : modelInfoList){
                // 为group , 不校验
                String groupKey = modelInfo.getGroupKey();
                if(StrUtil.isNotEmpty(groupKey)){
                    List<Meta.ModelConfig.ModelInfo> subModelInfoList = modelInfo.getModels();
                    String allArgsStr = subModelInfoList.stream()
                            .map(subModelInfo -> String.format("\"-%s\"",subModelInfo.getFieldName()))
                            .collect(Collectors.joining(", "));
                    System.out.println(allArgsStr);
                    modelInfo.setAllArgsStr(allArgsStr);
                    continue;
                }
                // 输出路径默认值
                String fieldName = modelInfo.getFieldName();
                if(StrUtil.isBlank(fieldName)){
                    throw new MetaException("未填写 filedName");
                }

                String modelInfoType = modelInfo.getType();
                if(StrUtil.isEmpty(modelInfoType)){
                    //modelInfo.setType("String");
                    modelInfo.setType(ModelTypeEnum.STRING.getValue());
                }
            }
        }
    }

    private static void validAndFillFileConfig(Meta meta) {
        // fileConfig 检验和默认值
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        //sourceRootPath : 必填
        String sourceRootPath = fileConfig.getSourceRootPath();
        if(StrUtil.isBlank(sourceRootPath)){
            throw new MetaException("未填写 soureRootPath");
        }
        // inputRootPath: .source + sourceRootPath 的最后一个层级路径
        String inputRootPath = fileConfig.getInputRootPath();
        String defaultInputRootPath = ".source/"+ FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
        if(StrUtil.isEmpty(inputRootPath)){
            fileConfig.setInputRootPath(defaultInputRootPath);
        }

        // outputRootPath : 默认为当前路径下的 generated
        String outputRootPath = fileConfig.getOutputRootPath();
        String defaultOutputRootPath = "generated";
        if(StrUtil.isEmpty(outputRootPath)){
            fileConfig.setOutputRootPath(defaultOutputRootPath);
        }

        String fileConfigType = fileConfig.getType();
        //String defaultType = "dir";
        String defaultType = FileTypeEnum.DIR.getValue();
        if(StrUtil.isEmpty(fileConfigType)){
            fileConfig.setType(defaultType);
        }

        // fileInfo 默认值
        List<Meta.FileConfig.FileInfo> fileInfoList = fileConfig.getFiles();
        if (!CollectionUtil.isNotEmpty(fileInfoList)) {
            return;
        }
        for(Meta.FileConfig.FileInfo fileInfo : fileInfoList){
            String type = fileInfo.getType();
            // 类型未group，不校验文件输入输出路径等信息，但是组里面的文件需要校验，此处没有校验，待优化。
            if(FileTypeEnum.GROUP.getValue().equals(type)){
                continue;
            }
            // inputPath 必填
            String inputPath = fileInfo.getInputPath();
            if(StrUtil.isBlank(inputPath)){
                throw new MetaException("未填写 inputPath");
            }

            // outputPath 默认等于inputpath
            String outputPath = fileInfo.getInputPath();
            if(StrUtil.isBlank(outputPath)){
                fileInfo.setOutputPath(inputPath);
            }

            // type : 默认有后缀 如文件后缀 .java 为file ,否则为dir
            // String type = fileInfo.getType();
            if(StrUtil.isBlank(type)){
                // fileInfo.setType("dir");
                fileInfo.setType(FileTypeEnum.DIR.getValue());
            }else{
                // fileInfo.setType("file");
                fileInfo.setType(FileTypeEnum.FILE.getValue());
            }

            // generateType: 如果文件结果不为ftl , 则默认为static ,否则为dynamic
            String generateType = fileInfo.getGenerateType();
            if(StrUtil.isBlank(generateType)){
                if(inputPath.endsWith(".ftl")){
                    //fileInfo.setGenerateType("dynamic");
                    fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
                }else{
                    //fileInfo.setGenerateType("static");
                    fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                }
            }
        }
    }

    private static void validAndFillMetaRoot(Meta meta) {
        // 基础信息校验和默认值
        /*
        String name = meta.getName();
        if(StrUtil.isBlank(name)){
            name = "my-generator";
            meta.setName(name);
        }

        String description = meta.getDescription();
        if(StrUtil.isBlank(description)){
            description = "我的模板代码生成器";
            meta.setDescription(description);
        }

        String author = meta.getAuthor();
        if(StrUtil.isBlank(author)){
            author = "yupi";
            meta.setAuthor(author);
        }

        String basePackage = meta.getBasePackage();
        if(StrUtil.isBlank(basePackage)){
            basePackage = "com.yupi";
            meta.setBasePackage(basePackage);
        }

        String version = meta.getVersion();
        if(StrUtil.isBlank(version)){
            version = "1.0";
            meta.setVersion(version);
        }

        String createTime = meta.getCreateTime();
        if(StrUtil.isBlank(createTime)){
            createTime = DateUtil.now();
            meta.setCreateTime(createTime);
        }
         */
        // 简写
        String name = StrUtil.blankToDefault(meta.getName(),"my-generator");
        String description = StrUtil.blankToDefault(meta.getDescription(),"我的模板代码生成器");
        String author = StrUtil.blankToDefault(meta.getAuthor(),"yupi");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(),"com.yupi");
        String version = StrUtil.blankToDefault(meta.getVersion(),"1.0");
        String createTime = StrUtil.blankToDefault(meta.getCreateTime(),DateUtil.now());
        meta.setName(name);
        meta.setDescription(description);
        meta.setAuthor(author);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setCreateTime(createTime);
    }
}
