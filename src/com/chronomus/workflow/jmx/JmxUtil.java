package com.chronomus.workflow.jmx;

public final class JmxUtil {
	
	private JmxUtil() {}

	public static String connectionString(String host, int port) {
		return String.format("service:jmx:rmi:///jndi/rmi://%s:%s/server", host, port);
	}

	public static String objectNameString(String domain, String mbeanClassName) {
		return domain + ":type=" + mbeanClassName + ",index=1";
	}

}
