### 实现一个mini版的SpringMVC
1. 从一个SpringMVC的应用的工作流程考虑需要实现什么?
    1. 用户发送请求, 请求被SpringMVC的DispatcherServlet拦截, 调用与URL对应的Method --> 核心: 实现DispatcherServlet拦截所有请求, 实现URL到具体的方法的Mapping(HandlerMapping)
    2. 开发基于SpringMVC的应用时, Spring帮助完成依赖注入
    --> 实现IOC, DI
    --> 自动扫描加了注解的包, 实现各种注解
    --> 发现加了@Controller, Service的注解的类, 生成实例, 放到IOC容器里
    --> 发现加了@Autowired注解的字段, 注入IOC容器中对应的实例
    3. 实现URL到Method的Mapping
    --> 发现加了@RequestMapping的方法, 将URL到方法的映射加到Map容器中保存, 同时需要考虑到其类上的@RequestMapping注解
2. 没有完成的功能
    1. IOC的循环依赖问题
    2. AOP
    3. 实例的scope(这里考虑的都是单例)
    4. @RequestParam注解, 只支持字符串类型, 参数值只能有一个


