package com.chronomus.workflow.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxClient {
	
	
	private MBeanServerConnection mbsc;

	public JmxClient(String connectionString) {
		super();
		JMXServiceURL url;
		try {
			url = new JMXServiceURL(connectionString);
	        JMXConnector jmxc = JMXConnectorFactory.connect(url, null); 
	        mbsc = jmxc.getMBeanServerConnection(); 
		} catch (MalformedURLException e) {
			throw new RuntimeException("Couldn't connect to " + connectionString, e);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't connect to " + connectionString, e);
		} 
	}

	public JmxClient(String host, int port) {
		this(JmxUtil.connectionString(host, port));
	}

	public Object invoke(ObjectName mbeanName, String command) throws Exception {	      
	      // Run invocation 
	      return invoke(mbeanName, command, null, null); 
	}

	public Object invoke(ObjectName mbeanName, String command, Object[] parameters, String[] parameterTypes) throws Exception {	      
	      // Run invocation 
	      return mbsc.invoke(mbeanName, command, parameters, parameterTypes); 
	}

	public void runService(String name, String command) throws MalformedObjectNameException, NullPointerException, Exception {
		runService(name, command, null, null);
	}

	public void runService(String name, String command, Object[] parameters, String[] parameterTypes) throws MalformedObjectNameException, NullPointerException, Exception {
        String domain = mbsc.getDefaultDomain(); 
        String mbeanObjectNameStr = JmxUtil.objectNameString(domain, name); 
   		ObjectName mbeanObjectName = 
    			ObjectName.getInstance(mbeanObjectNameStr); 
		invoke(mbeanObjectName, command, parameters, parameterTypes);
	}

	// Allow messages to be sent via command line
	public static void main(String[] args) throws MalformedObjectNameException, NullPointerException, Exception {
		if (args.length < 2) {
			usage("Not enough arguments");
			System.exit(-2);
		}
		String mbeanName = args[0];
		String command = args[1];
		// Need to get host and port from args
		JmxClient client = new JmxClient("localhost",1099);
		if (args.length==2) {
			client.runService(mbeanName, command);
		} else {
			String[] params = Arrays.copyOfRange(args, 2, args.length);
			String[] paramTypes = new String[params.length];
			Arrays.fill(paramTypes, "java.lang.String");
			client.runService(mbeanName, command, params, paramTypes);
		}
	}

	private static void usage(String error) {
		System.out.println(error);
		System.out.println("JmxClient targetBean method parameters");
	}
}
