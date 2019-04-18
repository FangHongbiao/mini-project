package com.fhb.minispringmvc.framework.MiniAnnotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb1021@163.com
 * Date: 2019/4/18
 * Time: 11:32
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MiniController {
    String value() default "";
}
