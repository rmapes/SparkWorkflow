package com.chronomus.workflow.jmx;

import javax.management.ObjectName;

public class JmxTest extends JmxTestCase {
	
	public void testSimpleCommand() throws Exception {
		// register test bean
		MockBean bean = new MockBean();
		bean.called = false;
		ObjectName mbeanName = server.register(bean);
		assertEquals(false, bean.called);
		// launch server
		server.start();
		assertEquals(false, bean.called);
		// Fire jmx request
		JmxClient client = new JmxClient(server.getConnectionString());
		client.invoke(mbeanName, "callObject");
		assertEquals(true, bean.called);	
	}

}

