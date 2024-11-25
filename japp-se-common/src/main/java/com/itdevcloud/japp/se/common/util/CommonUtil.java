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
package com.itdevcloud.japp.se.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class CommonUtil {

	public static void throwRuntimeException(Throwable e) {
		if (e == null) {
			throw new RuntimeException("throwRuntimeException() --- Throwable is null!");
		} else if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else {
			throw new RuntimeException(e);
		}
	}

	public static RuntimeException getRuntimeException(Throwable e) {
		if (e == null) {
			return new RuntimeException("getRuntimeException() - Throwable is null!");
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	public static String getStackTrace(Throwable t) {
		if (t == null) {
			return null;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String str = t.getMessage() + "\n" + sw.toString();
		pw.close();
		try {
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static String generateUUID() {
		String uuidStr = UUID.randomUUID().toString();
		return uuidStr;
	}

	public static String objectToString(Object obj, int level) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Map) {
			return mapToString((Map<?, ?>) obj, level);
		} else if (obj instanceof Set) {
			return setToString((Set<?>) obj, level);
		} else if (obj instanceof List) {
			return listToString((List<?>) obj, level);
		} else if (obj.getClass().isArray()) {
			return arrayToString((Object[]) obj, level);
		} else if (obj instanceof Properties) {
			return propertiesToString((Properties) obj, level);
		} else {
			return obj.toString();
		}
	}

	public static String propertiesToString(Properties properties, int level) {
		if (properties == null) {
			return null;
		}
		if (properties.isEmpty()) {
			return "";
		}
		Set<?> keySet = properties.keySet();
		Object key = null;
		Object value = null;

		Object[] keyArr = keySet.toArray();
		Arrays.sort(keyArr);

		StringBuffer sb = new StringBuffer();
		String prefix = "";
		for (int i = 0; i < level * 4; i++) {
			prefix = prefix + " ";
		}
		sb.append("( Properties ):\n");

		for (int i = 0; i < keyArr.length; i++) {
			key = keyArr[i];
			value = properties.get(key);
			sb.append(prefix + key + " = ");
			sb.append(objectToString(value, level + 1) + "\n");
		}
		return sb.toString();
	}

	public static String mapToString(Map<?, ?> map, int level) {
		if (map == null) {
			return null;
		}
		if (map.isEmpty()) {
			return "";
		}
		Set<?> keySet = map.keySet();
		Object key = null;
		Object value = null;

		Object[] keyArr = keySet.toArray();
		Arrays.sort(keyArr);

		StringBuffer sb = new StringBuffer();
		String prefix = "";
		for (int i = 0; i < level * 4; i++) {
			prefix = prefix + " ";
		}
		sb.append("( Map ):\n");

		for (int i = 0; i < keyArr.length; i++) {
			key = keyArr[i];
			value = map.get(key);
			sb.append(prefix + key + " = ");
			sb.append(objectToString(value, level + 1) + "\n");
		}
		return sb.toString();
	}

	public static String listToString(List<?> list, int level) {
		if (list == null) {
			return null;
		}
		if (list.isEmpty()) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String prefix = "";
		Object value = null;
		for (int i = 0; i < level * 4; i++) {
			prefix = prefix + " ";
		}
		sb.append("( List ):\n");
		for (int i = 0; i < list.size(); i++) {
			value = list.get(i);
			sb.append(prefix + i + " = ");
			sb.append(objectToString(value, level + 1) + "\n");
		}
		return sb.toString();
	}

	public static String setToString(Set<?> set, int level) {
		if (set == null) {
			return null;
		}
		if (set.isEmpty()) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		String prefix = "";
		for (int i = 0; i < level * 4; i++) {
			prefix = prefix + " ";
		}
		sb.append("( Set ):\n");

		for (Object object : set) {
			sb.append(objectToString(object, level + 1) + "\n");
		}
		return sb.toString();
	}

	public static String arrayToString(Object[] arr, int level) {
		if (arr == null) {
			return null;
		}
		if (arr.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String prefix = "";
		Object value = null;
		for (int i = 0; i < level * 4; i++) {
			prefix = prefix + " ";
		}
		sb.append("( Array ):\n");
		for (int i = 0; i < arr.length; i++) {
			value = arr[i];
			sb.append(prefix + i + " = ");
			sb.append(objectToString(value, level + 1) + "\n");
		}
		return sb.toString();
	}

	public static String listToString(List<?> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		StringBuffer sb = new StringBuffer("[");
		int i = 0;
		for (Object object : list) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("" + object);
			i = 1;
		}
		sb.append("]");
		return sb.toString();
	}

	public static BigDecimal convertToBigDecimal(Object obj, String format) {
		if (StringUtil.isEmptyOrNull(format)) {
			return convertToBigDecimal(obj);
		}
		String value = (obj == null ? "0" : obj.toString());
		DecimalFormat df = new DecimalFormat(format);
		BigDecimal bd = new BigDecimal(df.format(new Double(value)));
		return bd;
	}

	private static BigDecimal convertToBigDecimal(Object obj) {
		BigDecimal bd = (obj == null ? null : new BigDecimal(obj.toString()));
		return bd;
	}

	public static double convertBigDecimalToDouble(BigDecimal b) {
		double d = (b == null ? 0 : b.doubleValue());
		return d;
	}

	public static double SumBigDecimal(BigDecimal b1, BigDecimal b2) {
		double d1 = (b1 == null ? 0 : b1.doubleValue());
		double d2 = (b2 == null ? 0 : b2.doubleValue());
		return d1 + d2;
	}

	public static List<String> getMyLocalIp(List<String> includePrefixList) {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					// System.out.println("Local IP Address: " + address.getHostAddress());
					if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
						String addr = address.getHostAddress();
						if (StringUtil.isEmptyOrNull(addr)) {
							continue;
						}
						if (includePrefixList == null || includePrefixList.isEmpty()) {
							ipList.add(addr);
						} else {
							for (String prefixStr : includePrefixList) {
								if (prefixStr != null && addr.startsWith(prefixStr)) {
									ipList.add(addr);
								}
							}
						}
					}
				}
			}
			return ipList;
		} catch (Throwable t) {
			System.out.println(t);
			t.printStackTrace();
			return ipList;
		}
	}

	public static String getMyFirstLocalIp(List<String> includePrefixList) {
		List<String> ipList = getMyLocalIp(includePrefixList);
		String ip = null;
		if (ipList != null && !ipList.isEmpty()) {
			ip = ipList.get(0);
		}
		return StringUtil.isEmptyOrNull(ip) ? "localhost" : ip.trim();
	}

	public static boolean isPortAvailable(int port) {
		/*
		 * try (ServerSocket serverSocket = new ServerSocket()) { //
		 * setReuseAddress(false) is required only on macOS, // otherwise the code will
		 * not work correctly on that platform serverSocket.setReuseAddress(false);
		 * serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"),
		 * port), 1); return true; } catch (Exception ex) { return false; }
		 */

	       System.out.println("Check port availability......port # " + port );
		if (port < 1024 || port > 65535) {
			return false;
		}
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				try {
					ds.close();
				} catch (Exception e) {
				}
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
        System.out.println("port # " + port + " is not available.......");
		return false;
	}

	public static int getNextAvailablePort(int startPort, int endPort) {
		if (startPort <= 1024 && startPort > 65535) {
			return -1;
		}
		if (startPort > endPort) {
			endPort = startPort;
		}
		if (endPort > 65535) {
			endPort = 65535;
		}
		for (int i = startPort; i <= endPort; i++) {
			if (isPortAvailable(i)) {
				return i;
			}
		}
		return -1;
	}

}
