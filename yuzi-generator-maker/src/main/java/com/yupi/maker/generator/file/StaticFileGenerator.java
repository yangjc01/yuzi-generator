package com.yupi.maker.generator.file;

import cn.hutool.core.io.FileUtil;

public class StaticFileGenerator {
    public static void copyFileByHutools(String inputPath,String outputPath){
        FileUtil.copy(inputPath,outputPath,false);
    }
}
