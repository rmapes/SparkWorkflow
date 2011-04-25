package com.chronomus.workflow.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.Service;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.expressions.Expression;

public class Job implements Definition {

	private final String name;
	private Collection<Task> taskDefinitions = new ArrayList<Task>();
	private Service service;
	private List<Expression> outputs;
	private List<String> inputs;
	private Map<String, Expression> configVariables = new HashMap<String, Expression>();

	public Job(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Collection<Task> getTasks() {
		return taskDefinitions ;
	}

	public void addTask(Task execution) {
		if (execution == null) {
			throw new NullPointerException();
		}
		taskDefinitions.add(execution);
	}

	public void setInputs(List<String> inputs) {
		this.inputs = inputs;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void setOutputs(List<Expression> outputs) {
		this.outputs = outputs;
	}
	
	public List<Expression> getOutputs() {
		return outputs;
	}

	public void setConfig(List<Assignment> config) {
		for (Assignment assignment : config) {
			configVariables.put(assignment.getName(), assignment.getRvalue());
		}
	}

	public Map<String, Expression> getConfigVariables() {
		return configVariables ;
	}

}
