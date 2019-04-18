package com.fhb.minispringmvc.Controller;

import com.fhb.minispringmvc.Service.TestService;
import com.fhb.minispringmvc.framework.MiniAnnotation.MiniAutowried;
import com.fhb.minispringmvc.framework.MiniAnnotation.MiniController;
import com.fhb.minispringmvc.framework.MiniAnnotation.MiniRequestMapping;
import com.fhb.minispringmvc.framework.MiniAnnotation.MiniRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb1021@163.com
 * Date: 2019/4/18
 * Time: 11:24
 */
@MiniController
@MiniRequestMapping("/test")
public class TestController {

    @MiniAutowried
    private TestService testService;

    @MiniRequestMapping("get")
    public void get(HttpServletRequest req, HttpServletResponse resp,
                    @MiniRequestParam("name") String name)  {
        String s = testService.get(name);
        try {
            resp.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MiniRequestMapping("add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @MiniRequestParam("a") String a, @MiniRequestParam("b") String b) {
        String s = testService.add(Integer.parseInt(a), Integer.parseInt(b));
        try {
            resp.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MiniRequestMapping("del")
    public void del(HttpServletRequest req, HttpServletResponse resp,
                    @MiniRequestParam("key") String key) {
        String s = testService.del(key);
        try {
            resp.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
