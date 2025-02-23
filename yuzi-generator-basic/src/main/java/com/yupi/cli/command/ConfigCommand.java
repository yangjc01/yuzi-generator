package com.yupi.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.yupi.model.MainTemplateConfig;
import picocli.CommandLine;

import java.lang.reflect.Field;
@CommandLine.Command(name="config" ,description = "查看参数信息",mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable{
    @Override
    public void run() {
        System.out.println("查看参数信息");

        // 通过反射获取要打印属性的类
        //Class<?> myClass = MainTemplateConfig.class;
        //获取所有字段
        //Field[] fields = myClass.getDeclaredFields();

        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);

        // 遍历并打印每个字段的信息
        for(Field field : fields){
            System.out.println("字段名称："+field.getName());
            System.out.println("字段类型："+field.getType());
            System.out.println("---");
        }
 }
}
