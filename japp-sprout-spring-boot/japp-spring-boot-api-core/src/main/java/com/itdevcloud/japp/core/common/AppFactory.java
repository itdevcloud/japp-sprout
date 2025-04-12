/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.core.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itdevcloud.japp.core.api.bean.BaseRequest;
import com.itdevcloud.japp.core.api.bean.BaseResponse;
import com.itdevcloud.japp.core.processor.RequestProcessor;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.core.service.customization.CustomizableComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 * This is a factory class for classes which implement AppFactoryComponentI
 * interface. this class is useful for getting class instance outside of IOC
 * container. to avoid dead loop, this class should not be used in class
 * initiating process
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */
@Component
public class AppFactory {

	private static Logger logger = LogManager.getLogger(AppFactory.class);

	private static Map<String, CommandInfo> commandInfoMap = null;
	private static Map<Class<?>, AppFactoryComponentI> classComponentMap = null;
	private static List<AppFactoryComponentI> factoryComponentList = null;

	@Autowired
	public AppFactory(List<AppFactoryComponentI> componentList) {
		factoryComponentList = componentList;
		if (factoryComponentList == null || factoryComponentList.isEmpty()) {
			throw new RuntimeException(
					"There is no AppFactoryComponentI implemetation class detected, please check code! \n");
		}
	}

	@PostConstruct
	private void init() {
		logger.info("AppFactory.init() - begin........componentList size = " + factoryComponentList.size());

		commandInfoMap = new HashMap<String, CommandInfo>();
		classComponentMap = new HashMap<Class<?>, AppFactoryComponentI>();
		Map<Class<?>, List<CustomizableComponentI>> customizableServiceMap = new HashMap<Class<?>, List<CustomizableComponentI>>();

		List<String> processorErrorList = new ArrayList<String>();

		for (AppFactoryComponentI component : factoryComponentList) {
			if (component instanceof RequestProcessor) {
				RequestProcessor processor = (RequestProcessor) component;
				CommandInfo commandInfo = getCommandInfo(processor);
				if (commandInfo != null) {
					commandInfoMap.put(commandInfo.getCommand(), commandInfo);
				} else {
					processorErrorList.add(processor.getClass().getSimpleName());
				}
			} else if (component instanceof BaseRequest || component instanceof BaseResponse) {
				// do nothing about it
				continue;
			} else if (component instanceof CustomizableComponentI) {
				CustomizableComponentI service = (CustomizableComponentI) component;
				Class<?> serviceInterfaceClass = service.getInterfaceClass();
				List<CustomizableComponentI> processedList = customizableServiceMap.get(serviceInterfaceClass);
				if (processedList == null) {
					processedList = new ArrayList<CustomizableComponentI>();
				}
				processedList.add(service);
				customizableServiceMap.put(serviceInterfaceClass, processedList);
			} else {
				// logger.info("AppFactory: component - " +
				// component.getClass().getSimpleName());
				classComponentMap.put(getSpringIocImplOriginalClass(component.getClass()), component);
			}

		}
		// process customizable services
		Set<Class<?>> serviceKeySet = customizableServiceMap.keySet();
		for (Class<?> serviceInterfaceClass : serviceKeySet) {
			List<CustomizableComponentI> processedList = customizableServiceMap.get(serviceInterfaceClass);
			if (processedList == null || processedList.isEmpty()) {
				throw new RuntimeException(
						"No Service implementation class detected for: " + serviceInterfaceClass.getSimpleName());
			} else if (processedList.size() > 2) {
				throw new RuntimeException("There is more than one custom service implemetation class detected for: "
						+ serviceInterfaceClass.getSimpleName() + ", please check code !");
			} else if (processedList.size() == 1) {
				classComponentMap.put(serviceInterfaceClass, processedList.get(0));
			} else {
				if (!(processedList.get(0).getClass().getSimpleName()).startsWith("Default")) {
					classComponentMap.put(serviceInterfaceClass, processedList.get(0));
				} else {
					classComponentMap.put(serviceInterfaceClass, processedList.get(1));
				}
			}
		}
		// print out AppFactory Components
		Set<Class<?>> keyClassSet = classComponentMap.keySet();
		Object[] keyClassArr = keyClassSet.toArray();

		String[] classNameArr = new String[keyClassSet.size()];
		String str = "AppFactory supported component: ";
		for (int i = 0; i < keyClassSet.size(); i++) {
			classNameArr[i] = ((Class<?>) keyClassArr[i]).getSimpleName();
		}
		Arrays.sort(classNameArr);
		for (int i = 0; i < keyClassSet.size(); i++) {
			if (i != 0)
				str = str + "," + classNameArr[i];
		}
		str = str + "\nTOTAL Factory Component Number = " + keyClassArr.length;
		logger.debug(str);

		initAppComponents();

		if (processorErrorList != null && !processorErrorList.isEmpty()) {
			String errStr = "Following processors can not be succefully parsed - error detected when parse command, request or response object, check code!\n";
			for (String error : processorErrorList) {
				errStr = errStr + error + "\n";
			}
			logger.error(errStr);
		}

		Set<String> keySet = commandInfoMap.keySet();
		logger.info("AppFactory.init() - supported command set size = " + commandInfoMap.size() + " - " + keySet);
		String jsonTemplate = "AppFactory.init() - supported command Json Request Template: ";
		Gson gson = new GsonBuilder().serializeNulls().create();
		for (String keyStr : keySet) {
			CommandInfo commandInfo = commandInfoMap.get(keyStr);
			jsonTemplate = jsonTemplate + "\ncommand = <" + keyStr + "> request template: "
					+ gson.toJson(commandInfo.getRequest());
		}
		logger.info(jsonTemplate + "\n");

		logger.info("AppFactory.init().....end........");

		return;
	}

	/*
	 * This is specific for Spring container - sometimes it implements class
	 * automatically, we need to get original class
	 */
	private Class<?> getSpringIocImplOriginalClass(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		String name = clazz.getSimpleName();
		int idx = name.indexOf("$$");
		if (idx > 0) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

	private CommandInfo getCommandInfo(RequestProcessor processor) {

		if (processor == null) {
			throw new RuntimeException("AppFactory.getCommandInfo() - Processor parameter is null, check code!");
		}
		String classSimpleName = processor.getClass().getSimpleName();
		int idx = classSimpleName.indexOf(AppUtil.PROCESSOR_POSTFIX);
		if (idx <= 0) {
			logger.error("AppFactory.getCommandInfo() - Processor Class Name is not defined correctly - <"
					+ classSimpleName + ">, ignored!");
			return null;
		}
		String command = classSimpleName.substring(0, idx);
		if (StringUtil.isEmptyOrNull(command)) {
			logger.error("AppFactory.getCommandInfo() - command is null or empty for processor <" + classSimpleName
					+ ">, ignored!");
			return null;
		}
		if ("Request".equalsIgnoreCase(command)) {
			logger.info("AppFactory.getCommandInfo() - command is 'Request', <" + classSimpleName + ">, ignored!");
			return null;
		}
		String requestSimpleName = command + "Request";
		String responseSimpleName = command + "Response";
		BaseRequest request = (BaseRequest) getFactoryComponent(requestSimpleName);
		BaseResponse response = (BaseResponse) getFactoryComponent(responseSimpleName);

		if (request == null || response == null) {
			logger.info("AppFactory.getCommandInfo() - con not find request or response for the command <" + command
					+ ">, ignored the processor: " + classSimpleName + "!");
			return null;
		}
		command = command.toLowerCase();

		CommandInfo commandInfo = new CommandInfo();
		commandInfo.setCommand(command);
		commandInfo.setProcessor(processor);
		commandInfo.setRequest(request);
		commandInfo.setResponse(response);

		return commandInfo;
	}

	private AppFactoryComponentI getFactoryComponent(String classSimpleName) {
		for (AppFactoryComponentI component : factoryComponentList) {
			if (component.getClass().getSimpleName().equalsIgnoreCase(classSimpleName)) {
				return component;
			}
		}
		return null;
	}

	private void initAppComponents() {

		Field[] declaredFields = AppComponents.class.getDeclaredFields();
		if (declaredFields == null) {
			logger.error("Can not find declared fields in AppComponents class, check code!!!");
		}
		Object obj = null;
		List<String> errorFieldList = new ArrayList<String>();
		try {
			for (Field field : declaredFields) {
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
					field.setAccessible(true);
					obj = getComponent(field.getType());
					if (obj != null) {
						field.set(null, obj);
						logger.debug("init field successfully: '" + field.getName() + "' ");
					} else {
						errorFieldList
								.add("Field Name: '" + field.getName() + ", class '" + field.getType().getSimpleName());
					}
				}
			}
		} catch (Exception e) {
			logger.error("initAppComponents() - failed, exception: " + AppUtil.getStackTrace(e));
			throw AppUtil.throwRuntimeException(e);
		}
		if (!errorFieldList.isEmpty()) {
			String errStr = "Following Fields cannot be initialized, check code!\n";
			for (String fieldStr : errorFieldList) {
				errStr = errStr + fieldStr + "\n";
			}
			throw new RuntimeException(errStr);
		}
		return;
	}

//	public static <T> List<T> getAppFactoryComponents(Class<T> componentClass) {
//		if (componentClass == null) {
//			return null;
//		}
//		List<T> list = new ArrayList<T>();
//		for (Object component : classComponentMap.values()) {
//			if(component instanceof T )
//			try {
//				T obj = (T) component;
//				list.add(obj);
//			} catch (ClassCastException e) {
//				continue;
//			}
//		}
//		return list;
//	}

	public static <T> T getComponent(Class<T> componentClass) {
		if (componentClass == null) {
			return null;
		}
		return (T) classComponentMap.get(componentClass);
	}

	public static CommandInfo getCommandInfo(String command, String requestSimpleName) {
		if (StringUtil.isEmptyOrNull(command) && StringUtil.isEmptyOrNull(requestSimpleName)) {
			throw new RuntimeException("command or requestSimpleName is null, please check code!!!");
		}
		if (StringUtil.isEmptyOrNull(command)) {
			int idx = requestSimpleName.indexOf(AppUtil.REQUEST_POSTFIX);
			if (idx <= 0) {
				logger.error("AppFactory.getCommandInfo() - requestSimpleName parameter is not correct - <"
						+ requestSimpleName + ">, ignored! no commandInfo returned. ");
				return null;
			}
			command = requestSimpleName.substring(0, idx);
		}
		CommandInfo commandInfo = commandInfoMap.get(command.toLowerCase());
		if (commandInfo == null) {
			logger.error("Command - '" + command + "' is not supported, no commandInfo is found, check code!!!");
		}
		return commandInfo;
	}

	public static RequestProcessor getRequestProcessor(String requestSimpleName) {
		if (commandInfoMap == null || commandInfoMap.isEmpty() || StringUtil.isEmptyOrNull(requestSimpleName)) {
			throw new RuntimeException("commandInfoMap is null or empty, or command is null, please check code!!!");
		}
		int idx = requestSimpleName.indexOf("Request");
		if (idx <= 0) {
			String errStr = "getRequestProcessor() - requestSimpleName is not correct - <" + requestSimpleName
					+ ">, check code!";
			logger.error(errStr);
			// throw new RuntimeException(errStr);
			return null;
		}
		String command = requestSimpleName.substring(0, idx);

		CommandInfo commandInfo = commandInfoMap.get(command.toLowerCase());
		RequestProcessor processor = commandInfo == null ? null : commandInfo.getProcessor();
		if (processor != null) {
			return processor;
		} else {
			String errStr = "Request - '" + requestSimpleName
					+ "' is not supported, no processor is found, check code!!!";
			logger.error(errStr);
			// throw new RuntimeException(errStr);
			return null;

		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> interfaceImpl, Class<?>[] parameterTypes, Object[] initargs) {
		if (interfaceImpl == null) {
			throw new RuntimeException(
					"Must provide 'interfaceImpl' when using AppFactory to get an implementation instance");
		}
		String implClassName = interfaceImpl.getName();
		logger.debug(
				"AppFactory.getInstance(...) begin, interface implementation class name = '" + implClassName + "'...");
		Method m = null;
		T t = null;
		try {
			m = interfaceImpl.getMethod("getInstance", parameterTypes);
			t = (T) m.invoke(null, initargs);
			logger.debug("AppFactory.getInstance(...) end, call getInstance(), returned class type = "
					+ t.getClass().getName());
			return t;
		} catch (Exception e) {
			// there is no static getInstance() method, try default constructor
			logger.debug("Can not find static getInstance(...) method in class '" + implClassName
					+ "', try to use default constructor to create an instance. " + e);
			try {
				Constructor<?> construtor = interfaceImpl.getConstructor(parameterTypes);
				t = (T) construtor.newInstance(initargs);
			} catch (NoSuchMethodException e0) {
				try {
					logger.debug(
							"Can not find static getInstance(...) or constructor(...) method in class '" + implClassName
									+ "', try to use getInstance() or default construtor to create an instance. " + e0);
					if (parameterTypes != null && parameterTypes.length > 0) {
						return getInstance(interfaceImpl);
					} else {
						throw e0;
					}
				} catch (Exception ee) {
					throw new RuntimeException("can not get an instance for class '" + implClassName
							+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
							ee);
				}
			} catch (Exception e1) {
				throw new RuntimeException("can not get an instance for class '" + implClassName
						+ "', please make sure the class has a getInstance(...) method or a constructor(...) or has has a getInstance() method or a default constructor.",
						e1);
			}
			logger.debug("AppFactory.getInstance() end, invoke default constructor, returned class type = "
					+ t.getClass().getName());
			return t;
		}
	}

	public static <T> T getInstance(Class<T> interfaceImpl) {
		T t = getInstance(interfaceImpl, (Class[]) null, (Object[]) null);
		return t;
	}

}
