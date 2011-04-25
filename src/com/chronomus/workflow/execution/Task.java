package com.chronomus.workflow.execution;


public interface Task {
	void run(ServiceContext context) throws ExecutionException;
}
