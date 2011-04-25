package com.chronomus.workflow.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.chronomus.workflow.execution.ServiceContext;

public class ServiceDbAccessorImpl implements ServiceDbAccessor {

	// TODO: make this use real database
	Map <String, Map<Integer, ServiceContext>> tempStore = new HashMap<String, Map<Integer, ServiceContext>>();
	// Required table
	// CREATE TABLE ServiceStore {
	//  ServiceName varchar(32),
	//  ContextId int,
	//  Context BLOB
	// } CONSTRAINT ServiceStore_PK (ServiceName, ContextId)
	
	@Override
	public synchronized void markContextComplete(String name, ServiceContext context) {
		// Delete or mark as complete and purge?
		// DELETE FROM ServiceStore WHERE ServiceName = name AND ContextId = context.getKey()
		Map<Integer, ServiceContext> serviceStore = getServiceStore(name);
		serviceStore.remove(context.getKey());
	}

	@Override
	public synchronized void storeContext(String name, ServiceContext context) {
		// Should we overwrite existing context, or throw an error if present?
		// We should probably also store start time
		// INSERT INTO ServiceStore (ServiceName, ContextId, Context) values (name, context.getKey(), context.serialize())
		Map<Integer, ServiceContext> serviceStore = getServiceStore(name);
		serviceStore.put(context.getKey(), context);
	}

	@Override
	public synchronized Collection<ServiceContext> getContexts(String name) {
		// SELECT Context FROM ServiceStore; 
		return Collections.unmodifiableCollection(getServiceStore(name).values());
	}

	private Map<Integer, ServiceContext> getServiceStore(String name) {
		Map<Integer, ServiceContext> serviceStore = tempStore.get(name);
		if (serviceStore == null) {
			serviceStore = new HashMap<Integer, ServiceContext>();
			tempStore.put(name, serviceStore);
		}
		return serviceStore;
	}

	@Override
	public int getNumContexts() {
		// SELECT COUNT(*) FROM ServiceStore;
		int i = 0;
		for (Map<Integer, ServiceContext> serviceStore : tempStore.values()) {
			i += serviceStore.size();
		}
		return i;
	}

}
