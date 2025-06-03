package com.yupi.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

public class MetaManager {
    private static volatile Meta meta;

    private MetaManager(){
        // 私有构造方法，防止外部实例化
    }

    public static Meta getMetaObject(){
        if(meta == null){
            synchronized (MetaManager.class){
                if(meta == null){
                    meta = InitMeta();
                }
            }
        }
        return meta;
    }

    private static Meta InitMeta() {
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        Meta newMeta = JSONUtil.toBean(metaJson,Meta.class);
        // 访问Meta对象中文件配置的方法
        //Meta.FileConfig fileConfig = newMeta.getFileConfig();
        // meta基础信息校验
        MetaValidator.doValidAndFill(newMeta);
        return newMeta;
    }
}
