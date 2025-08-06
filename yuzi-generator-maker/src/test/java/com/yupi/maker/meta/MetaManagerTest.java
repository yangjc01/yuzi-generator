package com.yupi.maker.meta;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetaManagerTest {

    @Test
    public void getMetaObject() {
        Meta meta = MetaManager.getMetaObject();
        System.out.println(meta);
    }
}
