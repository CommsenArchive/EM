package com.commsen.em.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.commsen.em.annotations.internal.RequiresMany;


@Retention(RetentionPolicy.CLASS)
@Repeatable(RequiresMany.class)
@Target({
	ElementType.ANNOTATION_TYPE, ElementType.TYPE
})
public @interface Requires {

	String raw() default "";

	String value() default "";

	String ns() default "em.contract";

	String filter() default "";

	String effective() default "assemble";

	String resolution() default "";

	String extra() default "";

}
