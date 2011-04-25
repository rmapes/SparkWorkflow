package com.chronomus.workflow.persistence;

import java.util.Collection;

import com.chronomus.workflow.execution.ServiceContext;

public interface ServiceDbAccessor {

	public void storeContext(String name, ServiceContext context);

	public void markContextComplete(String name, ServiceContext context);

	public Collection<ServiceContext> getContexts(String name);

	public int getNumContexts();

}
