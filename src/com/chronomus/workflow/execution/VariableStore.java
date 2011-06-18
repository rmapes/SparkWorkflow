package com.chronomus.workflow.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.chronomus.workflow.execution.expressions.primitives.Primitive;
import com.chronomus.workflow.execution.expressions.primitives.StringPrimitive;

public class VariableStore extends HashMap<String, Primitive> {

	public VariableStore() {
		super();
	}

	public VariableStore(Properties baseContext) {
		super();
		if (baseContext != null) {
			for (Map.Entry<Object, Object> property : baseContext.entrySet()) {
				put((String)property.getKey(), new StringPrimitive((String)property.getValue()));
			}
		}
	}

	public String getProperty(String name) {
		return get(name).evaluate();
	}

}
