package com.yupi.generator;

import cn.hutool.core.io.FileUtil;

public class StaticGenerator {
    public static void copyFileByHutools(String inputPath,String outputPath){
        FileUtil.copy(inputPath,outputPath,false);
    }
}
