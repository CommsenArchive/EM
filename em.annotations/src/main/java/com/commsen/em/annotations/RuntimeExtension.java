package com.commsen.em.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({
	ElementType.TYPE
})
public @interface RuntimeExtension {

	enum Type {
		FRAMEWORK, BOOTCLASSPATH
	}
	
	Type value() default Type.FRAMEWORK;
	
}
