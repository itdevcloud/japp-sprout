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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.itdevcloud.japp.se.common.util.StringUtil;

import static java.lang.System.out;
/**
 * This is for object recursive copy until pre-defined level
 * After reach pre-defined "removeLevel", all references to the classes belong to the
 * objReferencePackagePrefixList will be set to null to avoid dead loop.
 * 
 * This is good for deep copy simple value objects. e.g. element of a list should not be a list 
 * This is can be used to copy JPA entities.
 * 
 * 
 * @author Marvin Sun
 * @since 1.0.0
 */
public class RecursiveDeepCopier {

	private ArrayList<String> objReferencePackagePrefixList = null;
	private int removeLevel = 2;

	public RecursiveDeepCopier(String objReferencePackagePrefix, int removeLevel) {
		String[] prefixes = null;
		this.objReferencePackagePrefixList = new ArrayList<String>();
		if (!StringUtil.isEmptyOrNull(objReferencePackagePrefix)){
			prefixes = objReferencePackagePrefix.split(";");
			for (String s : prefixes) {
				objReferencePackagePrefixList.add(s.trim());
			}
		}
		if (removeLevel < 0) {
			removeLevel = 2;
		}
		this.removeLevel = removeLevel;
	}

	public int getRemoveLevel() {
		return removeLevel;
	}

	public void setRemoveLevel(int removeLevel) {
		if (removeLevel < 0) {
			removeLevel = 2;
		}
		this.removeLevel = removeLevel;
	}

	@SuppressWarnings("unchecked")
	public <T> T deepCopyAndRemoveReferenceObject(T srcObj) {
		if (srcObj == null) {
			return null;
		}
		int currentLevel = 0;
		if (srcObj instanceof List) {
			List<?> srcList = (List<?>) srcObj;
			if (srcList.isEmpty()) {
				return null;
			} else {
				return (T) deepCopyAndRemoveReferenceObject(srcObj, srcObj.getClass(), srcList.get(0).getClass(),
						currentLevel);
			}
		} else {
			return (T) deepCopyAndRemoveReferenceObject(srcObj, srcObj.getClass(), null, currentLevel);
		}
	}

	@SuppressWarnings("unchecked")
	private <T, G> T deepCopyAndRemoveReferenceObject(Object srcObj, Class<T> srcClass, Class<G> genericTypeClass,
			int currentLevel) {
		if (srcObj instanceof List) {
			if (genericTypeClass == null) {
				return null;
			} else {
				List<G> srcList = (List<G>) srcObj;
				if (srcList.isEmpty()) {
					return (T) new ArrayList<G>();
				} else {
					List<G> targetList = new ArrayList<G>();
					for (int i = 0; i < srcList.size(); i++) {
						//out.println("------ copy list element, i = " + i);
						// list element can not be a list again, if so, it will return null;
						G obj = (G) deepCopyAndRemoveReferenceObject(srcList.get(i), srcList.get(i).getClass(), null,
								currentLevel);
						if (obj != null) {
							targetList.add(obj);
						}
					}
					return (T) targetList;
				}
			}
		}
		T targetObj = null;
		//out.println("------*********** copy starts.... " + srcObj.getClass());
		try {
			Method[] allMethods = srcObj.getClass().getMethods();
			targetObj = (T) srcObj.getClass().newInstance();
			for (Method method : allMethods) {
				String methodName = method.getName();
				if (methodName.startsWith("get") && method.getParameterCount() == 0
						&& Modifier.isPublic(method.getModifiers()) && !method.getReturnType().equals(Void.TYPE)) {
					// this method is a get method
					Method setMethod = getSetMethod(allMethods, method);
					if (setMethod == null) {
						// no corresponding set method found in the output class definition, do nothing
						// do not copy
						continue;
					}
					// out.println("------set methodName=" + setMethod.getName() );
					Object tmpObj = method.invoke(srcObj);
					if (tmpObj == null) {
						setMethod.invoke(targetObj, new Object[] { null });
						continue;
					} else if (tmpObj instanceof List) {
						List<?> tmpList = (List<?>) tmpObj;
						if (tmpList.size()>0) {
							Object obj = deepCopyAndRemoveReferenceObject(tmpList, tmpList.getClass(),
									tmpList.get(0).getClass(), currentLevel + 1);
							setMethod.invoke(targetObj, new Object[] { obj });
						}
					} else {
						String packageName = tmpObj.getClass().getPackage().getName();
						boolean isEntityPackage = foundPackage(this.objReferencePackagePrefixList, packageName);
						if (isEntityPackage) {
							if (currentLevel >= this.removeLevel) {
								setMethod.invoke(targetObj, new Object[] { null });
							} else {
								setMethod.invoke(targetObj, new Object[] { deepCopyAndRemoveReferenceObject(tmpObj,
										tmpObj.getClass(), null, currentLevel + 1) });
							}
						} else {
							out.println("--- set new non reference value: " + method.getName() + ":" + tmpObj.getClass().getName());
							setMethod.invoke(targetObj, new Object[] { tmpObj });
						}
					}
				} else {
					// not a get method
					continue;
				}
			} // end for

			return targetObj;
		} catch (Throwable t) {
			t.printStackTrace();
			return targetObj;
		}

	}

	private Method getSetMethod(Method[] allMethods, Method getMethod) {
		if (allMethods == null || getMethod == null) {
			return null;
		}
		String setMethodName = getMethod.getName().replaceFirst("get", "set");
		String methodName = null;
		Type[] methodParameterTypes = null;
		for (Method method : allMethods) {
			methodName = method.getName();
			methodParameterTypes = method.getGenericParameterTypes();
			if (methodName.equals(setMethodName) && methodParameterTypes.length == 1
					&& (methodParameterTypes[0].getTypeName()).equals(getMethod.getGenericReturnType().getTypeName())
					&& Modifier.isPublic(method.getModifiers()) && method.getReturnType().equals(Void.TYPE)) {
				// this is the corresponding set method
				return method;
			}
		} // end for
		return null;
	}

	private boolean foundPackage(List<String> packagePrefixList, String packageName) {
		if (StringUtil.isEmptyOrNull(packageName) || packagePrefixList == null || packagePrefixList.isEmpty()) {
			return false;
		}
		for (String s : packagePrefixList) {
			if (packageName.trim().indexOf(s) == 0) {
				return true;
			}
		}
		return false;
	}

	public static void main(String... args) {
		RecursiveDeepCopier erCopier = new RecursiveDeepCopier("com.itdevcloud.", 2);

		//Session sessionCopy = erCopier.deepCopyAndRemoveReferenceObject(session1);

	}
}
