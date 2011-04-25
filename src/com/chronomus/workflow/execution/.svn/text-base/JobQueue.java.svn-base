package com.chronomus.workflow.execution;

import java.util.Collection;

import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class JobQueue {

	private final ServiceDbAccessor dbAccessor;
	private final String name;

	public JobQueue(String name, ServiceDbAccessor mockDbAccessor) {
		this.name = name;
		this.dbAccessor = mockDbAccessor;
	}

	public void add(ServiceContext context) {
		dbAccessor.storeContext(name, context);
	}

	synchronized public ServiceContext pop() {
		Collection<ServiceContext> contexts = dbAccessor.getContexts(name);
		if (contexts.isEmpty()) {
			return null;
		}
		ServiceContext context = contexts.iterator().next();
		dbAccessor.markContextComplete(name, context);
		return context;
	}

}
