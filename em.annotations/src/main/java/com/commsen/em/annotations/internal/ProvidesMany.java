package com.commsen.em.annotations.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import com.commsen.em.annotations.Provides;


@Target({
	ElementType.ANNOTATION_TYPE, ElementType.TYPE
})
public @interface ProvidesMany {
	Provides[] value();
}
