package com.jacky.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * A simple ORM wrapper for JdbcTemplate.
 * 
 * @author liaoxuefeng
 */
public class DbTemplate {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	final JdbcTemplate jdbcTemplate;

	// class -> Mapper:
	private Map<Class<?>, Mapper<?>> classMapping;

	public DbTemplate(JdbcTemplate jdbcTemplate, String basePackage) {
		this.jdbcTemplate = jdbcTemplate;
		List<Class<?>> classes = scanEntities(basePackage);
		Map<Class<?>, Mapper<?>> classMapping = new HashMap<>();
		try {
			for (Class<?> clazz : classes) {
				logger.info("Found class: " + clazz.getName());
				Mapper<?> mapper = new Mapper<>(clazz);
				classMapping.put(clazz, mapper);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.classMapping = classMapping;
	}

	/**
	 * Get a model instance by class type and id. EntityNotFoundException is thrown
	 * if not found.
	 * 
	 * @param <T>   Generic type.
	 * @param clazz Entity class.
	 * @param id    Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T get(Class<T> clazz, Object id) {
		T t = fetch(clazz, id);
		if (t == null) {
			throw new EntityNotFoundException(clazz.getSimpleName());
		}
		return t;
	}

	/**
	 * Get a model instance by class type and id. Return null if not found.
	 * 
	 * @param <T>   Generic type.
	 * @param clazz Entity class.
	 * @param id    Id value.
	 * @return Entity bean found by id.
	 */
	public <T> T fetch(Class<T> clazz, Object id) {
		Mapper<T> mapper = getMapper(clazz);
		logger.info("SQL: " + mapper.selectSQL);
		List<T> list = (List<T>) jdbcTemplate.query(mapper.selectSQL, new Object[] { id }, mapper.rowMapper);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * Remove bean by id.
	 * 
	 * @param bean The entity.
	 */
	public <T> void delete(T bean) {
		try {
			Mapper<?> mapper = getMapper(bean.getClass());
			delete(bean.getClass(), mapper.getIdValue(bean));
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Remove bean by id.
	 * 
	 * @param bean The entity.
	 */
	public <T> void delete(Class<T> clazz, Object id) {
		Mapper<?> mapper = getMapper(clazz);
		logger.info("SQL: " + mapper.deleteSQL);
		jdbcTemplate.update(mapper.deleteSQL, id);
	}

	@SuppressWarnings("rawtypes")
	public Select select(String... selectFields) {
		return new Select(new Criteria(this), selectFields);
	}

	public <T> From<T> from(Class<T> entityClass) {
		Mapper<T> mapper = getMapper(entityClass);
		return new From<>(new Criteria<>(this), mapper);
	}

	/**
	 * Update entity's updatable properties by id.
	 * 
	 * @param <T>  Generic type.
	 * @param bean Entity object.
	 */
	public <T> void update(T bean) {
		try {
			Mapper<?> mapper = getMapper(bean.getClass());
			Object[] args = new Object[mapper.updatableProperties.size() + 1];
			int n = 0;
			for (AccessibleProperty prop : mapper.updatableProperties) {
				args[n] = prop.getter.invoke(bean);
				n++;
			}
			args[n] = mapper.id.getter.invoke(bean);
			logger.info("SQL: " + mapper.updateSQL);
			jdbcTemplate.update(mapper.updateSQL, args);
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}

	public <T> void insert(T bean) {
		try {
			int rows;
			final Mapper<?> mapper = getMapper(bean.getClass());
			Object[] args = new Object[mapper.insertableProperties.size()];
			int n = 0;
			for (AccessibleProperty prop : mapper.insertableProperties) {
				args[n] = prop.getter.invoke(bean);
				n++;
			}
			logger.info("SQL: " + mapper.insertSQL);
			if (mapper.id.isIdentityId()) {
				// using identityId:
				KeyHolder keyHolder = new GeneratedKeyHolder();
				rows = jdbcTemplate.update(new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(mapper.insertSQL,
								Statement.RETURN_GENERATED_KEYS);
						for (int i = 0; i < args.length; i++) {
							ps.setObject(i + 1, args[i]);
						}
						return ps;
					}
				}, keyHolder);
				if (rows == 1) {
					mapper.id.setter.invoke(bean, keyHolder.getKey());
				}
			} else {
				// id is specified:
				rows = jdbcTemplate.update(mapper.insertSQL, args);
			}
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}

	// get mapper by class:
	@SuppressWarnings("unchecked")
	<T> Mapper<T> getMapper(Class<T> clazz) {
		Mapper<T> mapper = (Mapper<T>) this.classMapping.get(clazz);
		if (mapper == null) {
			throw new RuntimeException("Target class is not a registered entity: " + clazz.getName());
		}
		return mapper;
	}

	private static List<Class<?>> scanEntities(String basePackage) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		List<Class<?>> classes = new ArrayList<>();
		Set<BeanDefinition> beans = provider.findCandidateComponents(basePackage);
		for (BeanDefinition bean : beans) {
			try {
				classes.add(Class.forName(bean.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return classes;
	}
}
