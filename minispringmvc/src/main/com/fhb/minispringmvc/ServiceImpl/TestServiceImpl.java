package com.fhb.minispringmvc.ServiceImpl;

import com.fhb.minispringmvc.Service.TestService;
import com.fhb.minispringmvc.framework.MiniAnnotation.MiniService;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb1021@163.com
 * Date: 2019/4/18
 * Time: 11:22
 */

@MiniService
public class TestServiceImpl implements TestService {
    public String get(String name) {
        return "From Server: Welcome " + name;
    }

    public String add(int a, int b) {
        return "The result " + a + "+" + b + "=" + (a + b);
    }

    public String del(String key) {
        return "The record " + key + " delete success";
    }
}
