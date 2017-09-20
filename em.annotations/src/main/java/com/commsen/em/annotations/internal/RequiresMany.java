package com.commsen.em.annotations.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.commsen.em.annotations.Requires;

@Retention(RetentionPolicy.CLASS)
@Target({
	ElementType.ANNOTATION_TYPE, ElementType.TYPE
})
public @interface RequiresMany {
	Requires[] value();
}
