package com.commsen.em.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Requires("configurator")
@Retention(RetentionPolicy.CLASS)
public @interface RequiresConfigurator {}

