package com.yupi.maker.generator.main;

public class MainGenerator extends GenerateTemplate {
    @Override
    // 重新方法可以覆盖实现不同行为
    protected void buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        // super.buildDist(outputPath, sourceCopyDestPath, jarPath, shellOutputFilePath);
        System.out.println("不要给我输出dist了");
    }
}
