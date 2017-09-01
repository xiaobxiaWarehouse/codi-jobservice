package com.codi.jobservice.job;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月15日 下午3:36:39
 */
public class JDKTest {

    @Test
    public void listTest() {
        List<String> a = new ArrayList<>(10);
        System.out.println(a.isEmpty());
        System.out.println(a.size());
    }

}
