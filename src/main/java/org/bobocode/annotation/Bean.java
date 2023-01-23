package org.bobocode.annotation;

import java.lang.annotation.*;

/**
 * Bean - annotation to define the types for which you need to create objects and put them in context
 * value() = the name of bean in the context
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
    String value() default "";
}
