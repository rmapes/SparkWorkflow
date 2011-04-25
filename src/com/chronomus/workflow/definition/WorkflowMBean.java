package com.chronomus.workflow.definition;

import com.chronomus.workflow.execution.ExecutionException;

public interface WorkflowMBean {
	
	void runService(String serviceName) throws ExecutionException;

}
