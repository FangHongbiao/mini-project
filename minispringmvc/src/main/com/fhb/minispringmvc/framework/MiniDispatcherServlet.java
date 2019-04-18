package com.fhb.minispringmvc.framework;

import com.fhb.minispringmvc.framework.MiniAnnotation.*;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb1021@163.com
 * Date: 2019/4/18
 * Time: 11:30
 */

public class MiniDispatcherServlet extends HttpServlet {
    // 配置文件的名称, 与web.xml中配置的相同
    private static final String LOCATION = "contextConfigLocation";

    // 保存所有配置信息
    private Properties p = new Properties();

    // 保存所有被扫描到的相关类名
    private List<String> classNames = new ArrayList<>();

    // IOC容器
    private Map<String, Object> ioc = new HashMap<>();

    // 保存所有URL和方法的映射关系
    private Map<String, Method> handlerMapping = new HashMap<>();

    public MiniDispatcherServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doPostDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doPostDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (handlerMapping.isEmpty()) return;

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found");
            return;
        }


        // 获取请求参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        // 获取url对应的方法
        Method method = handlerMapping.get(url);

        // 获取方法的参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // 获取方法参数名称



        // 保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            System.out.println(parameterType.getSimpleName());
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;

            // TODO 这部分处理的有bug
            } else if (parameterType == String.class){
                MiniRequestParam pa = (MiniRequestParam) parameterAnnotations[i][0];
                if (pa == null) {
                    resp.getWriter().write("Params error");
                    return;
                }
                String paramName = pa.value();
                if (!parameterMap.containsKey(paramName)) {
                    resp.getWriter().write("Params error");
                    return;
                }
                String[] value = parameterMap.get(paramName);
                paramValues[i] = value[0];
            }
        }

        try {
            String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());

            method.invoke(this.ioc.get(beanName), paramValues);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter(LOCATION));

        // 2. 扫描包
        doScanPackage(p.getProperty("scanPackage"));

        // 3. 初始化相关类, 并添加到IOC容器
        doInit();

        // 4. 依赖注入
        doAutowired();

        // 5. 构造HandlerMapping
        initHandlerMapping();

        // 6. 初始化完成
        System.out.println("mini-springmvc is inited");
    }

    private void initHandlerMapping() {

        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            // 没有MiniController注解的类中不可能有MiniRequestMapping
            if (!clazz.isAnnotationPresent(MiniController.class)) continue;

            String baseURL= "";

            if (clazz.isAnnotationPresent(MiniRequestMapping.class)) {
                MiniRequestMapping annotation = clazz.getAnnotation(MiniRequestMapping.class);
                baseURL = annotation.value();
            }

            // 注意这里不是clazz.getDeclaredMethods(), 因为私有方法是不可能成为对外访问的接口的
            Method[] methods = clazz.getMethods();

            for (Method method : methods) {

                // 没有@MiniRequestMapping 注解的可以忽略
                if (!method.isAnnotationPresent(MiniRequestMapping.class)) continue;

                MiniRequestMapping requestMapping = method.getAnnotation(MiniRequestMapping.class);

                String url = ("/" + baseURL + "/" + requestMapping.value()).replaceAll("/+", "/");

                handlerMapping.put(url, method);

                System.out.println("mapped: " + url + " -> " + method);
            }
        }

    }

    private void doAutowired() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {

                // 没有MiniAutowried注解的字段不需要考虑
                if (!field.isAnnotationPresent(MiniAutowried.class)) continue;

                MiniAutowried autowried = field.getAnnotation(MiniAutowried.class);

                String beanName = autowried.value().trim();

                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                // 设置私有属性访问权限
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInit() {
        if (classNames.size() == 0) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                // Controller 注解
                if (clazz.isAnnotationPresent(MiniController.class)) {
                    // 默认将首字母小写作为beanName
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());

                    // Service 注解
                } else if (clazz.isAnnotationPresent(MiniService.class)) {
                    MiniService service = clazz.getAnnotation(MiniService.class);
                    String beanName = service.value();

                    // 如果用户自己设置了名字, 就使用用户提供的, (在用户使用Autowired注入时也必须带上这个名字)
                    if (!"".endsWith(beanName.trim())) {
                        ioc.put(beanName, clazz.newInstance());
                        continue;
                    }

                    // 如果用户没有设置, 就按接口类型创建一个实例
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(i.getName(), clazz.newInstance());
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String lowerFirstCase(String str) {
        char[] ss = str.toCharArray();
        ss[0] += 32;
        return String.valueOf(ss);
    }

    private void doScanPackage(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        if (url == null) {
            return;
        }

        File dir = new File(url.getFile());

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanPackage(scanPackage + '.' + file.getName());
            } else {
                if (file.getName().endsWith(".class")) {
                    classNames.add(scanPackage + "." + file.getName().replace(".class", "").trim());
                }
            }
        }

    }

    private void doLoadConfig(String location) {
        InputStream fis = null;
        try {
            fis = this.getClass().getClassLoader().getResourceAsStream(location);
            p.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
