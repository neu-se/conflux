package edu.gmu.swe.phosphor.ignored.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface TableStat {
    String name();

    boolean emphasizeMax() default false;
}
