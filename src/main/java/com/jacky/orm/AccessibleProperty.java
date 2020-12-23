package com.jacky.orm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Represent a bean property.
 * 
 * @author liaoxuefeng
 */
class AccessibleProperty {

	// Method:
	final Method getter;
	final Method setter;

	// java type:
	final Class<?> propertyType;

	// java bean property name:
	final String propertyName;

	// table column name:
	final String columnName;

	boolean isId() {
		return this.getter.isAnnotationPresent(Id.class);
	}

	// is id && is id marked as @GeneratedValue(strategy=GenerationType.IDENTITY)
	boolean isIdentityId() {
		if (!isId()) {
			return false;
		}
		GeneratedValue gv = this.getter.getAnnotation(GeneratedValue.class);
		if (gv == null) {
			return false;
		}
		GenerationType gt = gv.strategy();
		return gt == GenerationType.IDENTITY;
	}

	boolean isInsertable() {
		if (isIdentityId()) {
			return false;
		}
		Column col = this.getter.getAnnotation(Column.class);
		return col == null || col.insertable();
	}

	boolean isUpdatable() {
		if (isId()) {
			return false;
		}
		Column col = this.getter.getAnnotation(Column.class);
		return col == null || col.updatable();
	}

	public AccessibleProperty(PropertyDescriptor pd) {
		this.getter = pd.getReadMethod();
		this.setter = pd.getWriteMethod();
		this.propertyType = pd.getReadMethod().getReturnType();
		this.propertyName = pd.getName();
		this.columnName = getColumnName(pd.getReadMethod(), propertyName);
	}

	private static String getColumnName(Method m, String defaultName) {
		Column col = m.getAnnotation(Column.class);
		if (col == null || col.name().isEmpty()) {
			return defaultName;
		}
		return col.name();
	}
}
