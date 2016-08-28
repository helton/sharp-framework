package me.helton.sharp.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import me.helton.sharp.core.annotations.Inject;

public class ServiceContainer {
	private Logger logger = Logger.getLogger(ServiceContainer.class);
	
	Map<Class<?>, Supplier<?>> types;

	public ServiceContainer() {
		this.types = new HashMap<>();
	}

	public <T> void register(Class<T> providedInterface, Class<? extends T> providedImplementation) {
		if (!providedInterface.isInterface() || providedImplementation.isInterface()) {
			throw new IllegalArgumentException("You should provide an interface and its implementation class respectively");
		}
		types.put(providedInterface, defaultDelegateTo(providedImplementation));
	}

	public <T> void register(Class<T> intf, Supplier<? extends T> delegateMethod) {
		types.put(intf, delegateMethod);
	}
	
	private <T> Supplier<T> defaultDelegateTo(Class<? extends T> impl) {
		return () -> resolve(impl);
	}

	@SuppressWarnings("unchecked")
	public <T> T resolve(Class<T> clazz) {
		logger.debug("Resolving type " + clazz.getName() + "...");
		T instance = null;
		Supplier<?> delegate = types.get(clazz);
		if (delegate == null) {
			instance = getInstance(clazz);
		} else {
			instance = (T) delegate.get();
		}
		return instance;
	}
	
	public <T> T getInstance(Class<T> clazz) {
		T instance = null;
		try {
			instance = (T) clazz.newInstance();			
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		injectDependencies(instance);
		return instance;
	}

	private <T> void injectDependencies(T instance) {
		injectFields(instance);
		injectSetters(instance);
	}

	private <T> void injectSetters(T instance) {
		logger.debug("Injecting fields via setters...");
		Class<?> clazz = instance.getClass();
		
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (isSetter(method)) {
				logger.debug("Analyzing setter " + method.getName() + "...");
				Inject inject = method.getAnnotation(Inject.class);
				if (inject != null) {
					logger.info("Injecting setter " + method.getName() + "...");
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length != 1) {
						throw new IllegalArgumentException(String.format("Set method %s() expected to have only 1 parameter", method.getName()));
					}
					Class<?> injectedType = parameterTypes[0];
					Object injectedValue = resolve(injectedType);
					try {
						method.invoke(instance, injectedValue);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private boolean isSetter(Method method) {
		return method.getName().startsWith("set");
	}
	
	private <T> void injectFields(T instance) {
		logger.debug("Injecting public fields...");
		Class<?> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			logger.debug("Analyzing field " + field.getName() + "...");
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				if (Modifier.isPublic(field.getModifiers())) {
					injectPublicField(instance, field);
				} else {
					logger.warn(String.format("Injected field %s is not public", field.getName()));
				}
			}
		}
	}

	private <T> void injectPublicField(T instance, Field field) {
		try {
			logger.info(String.format("Injecting public field %s...", field.getName()));
			field.set(instance, resolve(field.getType()));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}		
	}

}