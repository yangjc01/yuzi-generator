package ${basePackage}.generator;

import com.yupi.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/*
* 定义宏，重复使用模板
* */
<#macro generateFile indent fileInfo>
    ${indent}inputPath = new File(inputRootPath,"${fileInfo.inputPath}").getAbsolutePath();
    ${indent}outputPath = new File(outputRootPath,"${fileInfo.outputPath}").getAbsolutePath();
    <#if fileInfo.generateType == "static">
        ${indent}StaticGenerator.copyFileByHutools(inputPath,outputPath);
    <#else>
        ${indent}DynamicGenerator.doGenerate(inputPath, outputPath, model);
    </#if>
</#macro>

/*
* 核心生成器
* */
public class MainGenerator {
    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;

        <#list modelConfig.models as modelInfo>
        <#-- 有分组 -->
        <#if modelInfo.groupKey??>
            <#list modelInfo.models as subModelInfo>
                ${subModelInfo.type} ${subModelInfo.fieldName} = model.${modelInfo.groupKey}.${subModelInfo.fieldName};
            </#list>
        <#else>
            ${modelInfo.type} ${modelInfo.fieldName} = model.${modelInfo.fieldName};
        </#if>
        </#list>

        <#list fileConfig.files as fileInfo>
            <#if fileInfo.groupKey??>
                <#if fileInfo.condition??>
                    if(${fileInfo.condition}){
                    <#list fileInfo.files as fileInfo>
                        <@generateFile fileInfo=fileInfo indent="           "/>
                    </#list>
                    }
                <#else>
                    <#list fileInfo.files as fileInfo>
                        <@generateFile fileInfo=fileInfo indent="           "/>
                    </#list>
                </#if>
            <#else>
                <#if fileInfo.condition??>
                    if(${fileInfo.condition}){
                    <@generateFile fileInfo=fileInfo indent="           "/>
                    }
                <#else>
                    <@generateFile fileInfo=fileInfo indent="           "/>
                </#if>
            </#if>
        </#list>
    }
}
