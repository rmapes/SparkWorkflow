package com.chronomus.workflow.jmx;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class JmxServer {
	
	private MBeanServer mbs;
	private JMXConnectorServer cs;
	private String connectionString;
	private final int port;

	public JmxServer(String host, int port) {
	       this.port = port;
		mbs = MBeanServerFactory.createMBeanServer(); 		
			JMXServiceURL url;
			try {
				connectionString = JmxUtil.connectionString(host, this.port);
				url = new JMXServiceURL(connectionString);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Can't start JMX server", e);
			} 
			try {
				cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
			} catch (IOException e) {
				throw new RuntimeException("Can't start JMX server", e);
			} 
	}

	public ObjectName register(Object bean) {
        String mbeanClassName = bean.getClass().getName();
        return register(bean, mbeanClassName);
	}


	public ObjectName register(Object bean, String serviceName) {
        String domain = mbs.getDefaultDomain(); 
        String mbeanObjectNameStr = JmxUtil.objectNameString(domain, serviceName); 
        try {
    		ObjectName mbeanObjectName = 
    			ObjectName.getInstance(mbeanObjectNameStr); 
			return mbs.registerMBean(bean, mbeanObjectName).getObjectName();
		} catch (Exception e) {
			throw new RuntimeException("Could not create MBean " + serviceName, e);
		} 
	} 

	public void start() {
		try {
			cs.start();
		} catch (IOException e) {
			// Unexpected exception during run. Let's just abort
			throw new RuntimeException("Unexpected exception in JMXServer", e);
		}
	}

	public void stop() {
		try {
			cs.stop();
		} catch (IOException e1) {
			// We're shutting down anyway, so just output and ignore
			e1.printStackTrace();
		}
	}

	public String getConnectionString() {
		return connectionString;
	}

	public int getPort() {
		return this.port;
	}
}
