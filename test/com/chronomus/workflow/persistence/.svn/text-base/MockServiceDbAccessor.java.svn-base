package com.chronomus.workflow.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.ServiceContext;
import com.chronomus.workflow.execution.expressions.Primitive;
import com.chronomus.workflow.persistence.ServiceDbAccessor;

public class MockServiceDbAccessor implements ServiceDbAccessor {

	Map <String, Map<Integer, ServiceContext>> tempStore = new HashMap<String, Map<Integer, ServiceContext>>();
	Map <String, Map<Integer, ServiceContext>> completedContextStore = new HashMap<String, Map<Integer, ServiceContext>>();
	
	@Override
	public synchronized void markContextComplete(String name, ServiceContext context) {
		// Debug
		System.out.println(String.format("Completing context %d for service %s (%d contexts active)", context.getKey(), name, getNumContexts()));
		//
		Map<Integer, ServiceContext> serviceStore = getServiceStore(name);
		getServiceCompletedContextStore(name).put(context.getKey(), context);
		serviceStore.remove(context.getKey());
	}

	@Override
	public synchronized void storeContext(String name, ServiceContext context) {
		// Debug
		System.out.println(String.format("Storing context %d for service %s (%d contexts active)", context.getKey(), name, getNumContexts()));
		//
		Map<Integer, ServiceContext> serviceStore = getServiceStore(name);
		serviceStore.put(context.getKey(), context);
	}

	@Override
	public synchronized Collection<ServiceContext> getContexts(String name) {
		return Collections.unmodifiableCollection(getServiceStore(name).values());
	}

	// Visible for testing
	public Map<Integer, ServiceContext> getServiceStore(String name) {
		Map<String, Map<Integer, ServiceContext>> contextStore = tempStore;
		return getServiceStoreFromContextStore(name, contextStore);
	}

	// Visible for testing
	public Map<Integer, ServiceContext> getServiceCompletedContextStore(String name) {
		Map<String, Map<Integer, ServiceContext>> contextStore = completedContextStore;
		return getServiceStoreFromContextStore(name, contextStore);
	}

	private Map<Integer, ServiceContext> getServiceStoreFromContextStore(
			String name, Map<String, Map<Integer, ServiceContext>> contextStore) {
		Map<Integer, ServiceContext> serviceStore = contextStore.get(name);
		if (serviceStore == null) {
			serviceStore = new HashMap<Integer, ServiceContext>();
			contextStore.put(name, serviceStore);
		}
		return serviceStore;
	}

	// return variable value found in service context
	public String getContextVariable(Service service, String variable) {
		String serviceName = service.getName();
		Map<Integer, ServiceContext> contextStore = getServiceCompletedContextStore(serviceName);
		// Assume that there will be only one or none
		Iterator<ServiceContext> iterator = contextStore.values().iterator();
		if (iterator.hasNext()) {
			Primitive value = iterator.next().getVariables().get(variable);
			return value != null ? value.toString() : null;
		}
		return null;
	}

	@Override
	public int getNumContexts() {
		int i = 0;
		for (Map<Integer, ServiceContext> serviceStore : tempStore.values()) {
			i += serviceStore.size();
		}
		return i;
	}
}
