package com.yupi.maker.template.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum FileFilterRuleEnum {

    CONTAINS("包含","contains"),
    START_WITH("前缀匹配","startWith"),
    END_WITH("后缀匹配","endWith"),
    REGEX("正则","regex"),
    EQUALS("相等","equals");

    private final String text;
    private final String value;

    FileFilterRuleEnum(String text,String value){
        this.text = text ;
        this.value = value;
    }

    // 根据value获取枚举
    public static FileFilterRuleEnum getEnumByValue(String value){
        if(ObjectUtil.isEmpty(value)){
            return null ;
        }
        for(FileFilterRuleEnum anEnum : FileFilterRuleEnum.values()){
            if(anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null ;
    }
}
