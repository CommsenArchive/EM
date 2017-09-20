package com.commsen.em.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.commsen.em.annotations.internal.ProvidesMany;

@Retention(RetentionPolicy.CLASS)
@Repeatable(ProvidesMany.class)
@Target({
	ElementType.ANNOTATION_TYPE, ElementType.TYPE
})
public @interface Provides {
	
	String raw() default "";

	String value() default "";

	String ns() default "em.contract";
		
	public String[] options() default {};
	
	String extra() default "";

	String version() default "";

	String effective() default "assemble";

	String[] uses() default {};

	String[] mandatory() default {};

}
