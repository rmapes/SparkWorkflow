package com.chronomus.workflow.jmx;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

public abstract class JmxTestCase extends TestCase {

	protected JmxServer server;
	protected final int port = 9999;
	protected final String host = "localhost";
	private Registry serviceStub;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
    	serviceStub = LocateRegistry.createRegistry(port);
		server = new JmxServer(host, port);
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop();
		UnicastRemoteObject.unexportObject(serviceStub, true);
		super.tearDown();
	}

}
