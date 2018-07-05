package com.commsen.em.maven.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class IndentingLogger implements Logger {

	private Logger logger;
	
	private static ThreadLocal<Integer> indent = ThreadLocal.withInitial(() -> 0);
	
	private IndentingLogger(Logger logger) {
		this.logger = logger;
	}

	public static IndentingLogger wrap (Logger logger) {
		return new IndentingLogger(logger);
	}
	
	public void indent () {
		indent.set(indent.get() + 1);
	}

	public void unindent () {
		indent.set(indent.get() - 1);
	}

	private String getIndent() {
		return Stream.generate(() -> "  ").limit(indent.get()).collect(Collectors.joining());
	}
	
	public String getName() {
		return logger.getName();
	}

	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	public void trace(String msg) {
		logger.trace(getIndent() + msg);
	}

	public void trace(String format, Object arg) {
		logger.trace(getIndent() + format, arg);
	}

	public void trace(String format, Object arg1, Object arg2) {
		logger.trace(getIndent() + format, arg1, arg2);
	}

	public void trace(String format, Object... arguments) {
		logger.trace(getIndent() + format, arguments);
	}

	public void trace(String msg, Throwable t) {
		logger.trace(getIndent() + msg, t);
	}

	public boolean isTraceEnabled(Marker marker) {
		return logger.isTraceEnabled(marker);
	}

	public void trace(Marker marker, String msg) {
		logger.trace(marker, getIndent() + msg);
	}

	public void trace(Marker marker, String format, Object arg) {
		logger.trace(marker, getIndent() + format, arg);
	}

	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.trace(marker, getIndent() + format, arg1, arg2);
	}

	public void trace(Marker marker, String format, Object... argArray) {
		logger.trace(marker, getIndent() + format, argArray);
	}

	public void trace(Marker marker, String msg, Throwable t) {
		logger.trace(marker, getIndent() + msg, t);
	}

	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public void debug(String msg) {
		logger.debug(getIndent() + msg);
	}

	public void debug(String format, Object arg) {
		logger.debug(getIndent() + format, arg);
	}

	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(getIndent() + format, arg1, arg2);
	}

	public void debug(String format, Object... arguments) {
		logger.debug(getIndent() + format, arguments);
	}

	public void debug(String msg, Throwable t) {
		logger.debug(getIndent() + msg, t);
	}

	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled(marker);
	}

	public void debug(Marker marker, String msg) {
		logger.debug(marker, getIndent() + msg);
	}

	public void debug(Marker marker, String format, Object arg) {
		logger.debug(marker, getIndent() + format, arg);
	}

	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker, getIndent() + format, arg1, arg2);
	}

	public void debug(Marker marker, String format, Object... arguments) {
		logger.debug(marker, getIndent() + format, arguments);
	}

	public void debug(Marker marker, String msg, Throwable t) {
		logger.debug(marker, getIndent() + msg, t);
	}

	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public void info(String msg) {
		logger.info(getIndent() + msg);
	}

	public void info(String format, Object arg) {
		logger.info(getIndent() + format, arg);
	}

	public void info(String format, Object arg1, Object arg2) {
		logger.info(getIndent() + format, arg1, arg2);
	}

	public void info(String format, Object... arguments) {
		logger.info(getIndent() + format, arguments);
	}

	public void info(String msg, Throwable t) {
		logger.info(getIndent() + msg, t);
	}

	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled(marker);
	}

	public void info(Marker marker, String msg) {
		logger.info(marker, getIndent() + msg);
	}

	public void info(Marker marker, String format, Object arg) {
		logger.info(marker, getIndent() + format, arg);
	}

	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.info(marker, getIndent() + format, arg1, arg2);
	}

	public void info(Marker marker, String format, Object... arguments) {
		logger.info(marker, getIndent() + format, arguments);
	}

	public void info(Marker marker, String msg, Throwable t) {
		logger.info(marker, getIndent() + msg, t);
	}

	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	public void warn(String msg) {
		logger.warn(getIndent() + msg);
	}

	public void warn(String format, Object arg) {
		logger.warn(getIndent() + format, arg);
	}

	public void warn(String format, Object... arguments) {
		logger.warn(getIndent() + format, arguments);
	}

	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(getIndent() + format, arg1, arg2);
	}

	public void warn(String msg, Throwable t) {
		logger.warn(getIndent() + msg, t);
	}

	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled(marker);
	}

	public void warn(Marker marker, String msg) {
		logger.warn(marker, getIndent() + msg);
	}

	public void warn(Marker marker, String format, Object arg) {
		logger.warn(marker, getIndent() + format, arg);
	}

	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.warn(marker, getIndent() + format, arg1, arg2);
	}

	public void warn(Marker marker, String format, Object... arguments) {
		logger.warn(marker, getIndent() + format, arguments);
	}

	public void warn(Marker marker, String msg, Throwable t) {
		logger.warn(marker, getIndent() + msg, t);
	}

	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	public void error(String msg) {
		logger.error(getIndent() + msg);
	}

	public void error(String format, Object arg) {
		logger.error(getIndent() + format, arg);
	}

	public void error(String format, Object arg1, Object arg2) {
		logger.error(getIndent() + format, arg1, arg2);
	}

	public void error(String format, Object... arguments) {
		logger.error(getIndent() + format, arguments);
	}

	public void error(String msg, Throwable t) {
		logger.error(getIndent() + msg, t);
	}

	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

	public void error(Marker marker, String msg) {
		logger.error(marker, getIndent() + msg);
	}

	public void error(Marker marker, String format, Object arg) {
		logger.error(marker, getIndent() + format, arg);
	}

	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, getIndent() + format, arg1, arg2);
	}

	public void error(Marker marker, String format, Object... arguments) {
		logger.error(marker, getIndent() + format, arguments);
	}

	public void error(Marker marker, String msg, Throwable t) {
		logger.error(marker, getIndent() + msg, t);
	}

}
