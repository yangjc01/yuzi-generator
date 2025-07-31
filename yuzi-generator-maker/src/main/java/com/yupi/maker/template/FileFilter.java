package com.yupi.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.yupi.maker.template.model.FileFilterConfig;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileFilter {

    // 对某个文件或目录进行过滤，返回文件列表
    // 即时传入的是单个文件，也能通过该方法获取到列表，保持一致
    public static List<File> doFilter(String filePath,List<FileFilterConfig> fileFilterConfigList){
        // 根据路径获取所有文件
        List<File> fileList = FileUtil.loopFiles(filePath);
        return fileList.stream()
                .filter(file -> doSingleFileFilter(fileFilterConfigList,file))
                .collect(Collectors.toList());
    }


    // 单个文件过滤
    public static  boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file){
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        // 所有过滤器校验结束的结果
        boolean result = true;
        if(CollUtil.isEmpty(fileFilterConfigList)){
            return true ;
        }

        for(FileFilterConfig fileFilterConfig : fileFilterConfigList){
            String range = fileFilterConfig.getRange();
            String rule =  fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if(fileFilterRangeEnum == null){
                continue;
            }

            // 要过滤的内容
            String content =  fileName ;
            switch (fileFilterRangeEnum){
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            FileFilterRuleEnum filterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if(filterRuleEnum == null){
                continue;
            }

            switch (filterRuleEnum){
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case START_WITH:
                    result = content.startsWith(value);
                    break;
                case END_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }
            // 有一个不满足就直接返回
            if(!result){
                return false;
            }
        }
        // 条件都满足，返回true
        return true;
    }
}
