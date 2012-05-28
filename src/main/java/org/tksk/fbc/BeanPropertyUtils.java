package org.tksk.fbc;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class BeanPropertyUtils {

	private static final List<String> JAVA_KEYWORDS = Arrays.asList(
			"assert", "break", "case", "catch", "class", "const", "continue", "default", "do", "else",
            "enum", "extends", "finally", "final", "for", "goto", "if", "implements", "import", "interface",
            "instanceof", "new", "package", "return", "static", "super", "switch", "synchronized", "this",
            "throws", "throw", "try", "void", "while");

    public static BigDecimal getBeanValue(Object bean, String name, MathContext mc) {
    	name = name.trim();

    	if(JAVA_KEYWORDS.contains(name)) {
			throw new IllegalArgumentException("Java's keyword: " + name);
    	}

		final Object candidate;
		final Class<?> retType;

    	try {

    		if(bean instanceof Map) {
    			@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) bean;
    			candidate = map.get(name);
    			if(candidate == null) {
    				throw new IllegalArgumentException("bean has no such key: " + name);
    			}

    			retType = candidate.getClass();
    		} else {
				PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, name);

				if(pd == null) {
					throw new IllegalArgumentException("bean has no such property: " + name);
				}

				retType = pd.getPropertyType();
				// PropertyUtils.getProperty(bean, name);
				Method getter = pd.getReadMethod();
				if(getter == null) {
					throw new IllegalArgumentException(String.format("bean has no getter for property: '%s'", name));
				}

				candidate = getter.invoke(bean);
    		}

			BigDecimal ret = null; // ?

			if(retType.equals(BigDecimal.class)) {
				ret = (BigDecimal) candidate;

			} else if(retType.equals(long.class) || retType.equals(Long.class)){
				ret = new BigDecimal((Long) candidate, mc);

			} else if(retType.equals(int.class) || retType.equals(Integer.class)){
				ret = new BigDecimal((Integer) candidate, mc);

			} else if(retType.equals(double.class) || retType.equals(Double.class)){
				ret = new BigDecimal((Double) candidate, mc);

			} else {
				new IllegalArgumentException("no supported type: " + retType);
			}

	    	return ret;

		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		}
    }

    public static BigDecimal setBeanValue(Object bean, String name, BigDecimal value) {
    	name = name.trim();

    	if(JAVA_KEYWORDS.contains(name)) {
			throw new IllegalArgumentException("Java's keyword: " + name);
    	}

		if(bean instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) bean;
			map.put(name, value);

			return value;
		}

		final Class<?> paramType;

    	try {
			PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, name);

			if(pd == null) {
				throw new IllegalArgumentException("bean has no such property: " + name);
			}

			paramType = pd.getPropertyType();
			// PropertyUtils.getProperty(bean, name);
			Method setter = pd.getWriteMethod();
			if(setter == null) {
				throw new IllegalArgumentException(String.format("bean has no setter for property: '%s'", name));
			}

			if(paramType.equals(BigDecimal.class)) {
				setter.invoke(bean, value);

			} else if(paramType.equals(long.class) || paramType.equals(Long.class)){
				setter.invoke(bean, value.longValue());

			} else if(paramType.equals(int.class) || paramType.equals(Integer.class)){
				setter.invoke(bean, value.intValue());

			} else if(paramType.equals(double.class) || paramType.equals(Double.class)){
				setter.invoke(bean, value.doubleValue());

			} else {
				new IllegalArgumentException("no supported type: " + paramType);
			}

	    	return value;

		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("wrong bean: " + bean);
		}
    }

}
