package com.chronomus.workflow.execution;

import java.util.Properties;

public class ServiceContext {
	
	private static int sequence = 0;

	private VariableStore variables;

	private int primary_key;

	public ServiceContext(Properties baseContext) {
		this.variables = new VariableStore(baseContext);
		this.primary_key = sequence++;
	}

	public ServiceContext(ServiceContext context) {
		this((Properties) null);
		this.variables.putAll(context.getVariables());
	}

	public ServiceContext() {
		this((Properties) null);
	}

	public VariableStore getVariables() {
		return variables;
	}

	public Integer getKey() {
		return primary_key;
	}

}
