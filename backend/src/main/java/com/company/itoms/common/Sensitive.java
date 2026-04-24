package com.company.itoms.common;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {
    SensitiveType type();
}
