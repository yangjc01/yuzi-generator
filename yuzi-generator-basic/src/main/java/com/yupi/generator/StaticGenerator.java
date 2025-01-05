package com.yupi.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class StaticGenerator {
    public static void copyFileByHutools(String inputPath,String outputPath){
        FileUtil.copy(inputPath,outputPath,false);
    }

    public static void copyFilesByRecursive(String inputPath,String outputPath){
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        try {
            copyFileByRecursive(inputFile,outputFile);
        } catch (IOException e) {
            System.err.println("文件复制失败");
            e.printStackTrace();
        }
    }

    public static void copyFileByRecursive(File inputFile, File outputFile) throws IOException {
        //区分是文件还是目录
        if(inputFile.isDirectory()){
            //System.out.println(inputFile.getName());
            // 将源目录创建为输出目录的子目录
            File destOutputFile = new File(outputFile,inputFile.getName());
            // 如果是目录，就创建目标目录
            if(!destOutputFile.exists()){
                destOutputFile.mkdir();
            }
            // 获取目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            // 无子文件,直接结束
            if(ArrayUtil.isEmpty(files)){
                return;
            }
            for(File file : files){
                // 递归拷贝下一层文件
                copyFileByRecursive(file,destOutputFile);
            }
        } else {
            //是文件，直接复制到目录目录下
            Path destPath = outputFile.toPath().resolve(inputFile.getName());
            //System.out.println("目标路径:" + destPath);
            Files.copy(inputFile.toPath(),destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    public static void main(String[] args) {
        //获取整个项目的根目录
        System.out.println(System.getProperty("user.dir"));

        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        //System.out.println(parentFile);
        String inputPath = new File(parentFile,"yuzi-generator-demo-projects/acm-template").getAbsolutePath();
        //System.out.println(inputPath);
        String outputPath = projectPath;
        // 调用Hutool的方法
        //StaticGenerator.copyFileByHutools(inputPath,outputPath);
        // 调用自己写的递归方法
        StaticGenerator.copyFilesByRecursive(inputPath,outputPath);

    }
}
