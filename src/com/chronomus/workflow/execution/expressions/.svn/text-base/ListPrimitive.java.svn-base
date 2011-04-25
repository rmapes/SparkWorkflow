package com.chronomus.workflow.execution.expressions;

import java.util.ArrayList;
import java.util.List;

public class ListPrimitive implements Primitive {

	private List<Primitive> values;

	public ListPrimitive(String... splitValues) {
		values = new ArrayList<Primitive>();
		for (String value : splitValues) {
			values.add(new StringPrimitive(value));
		}
	}

	@Override
	public String evaluate() {
		return values.toString();
	}

	public List<Primitive> expressions() {
		return values;
	}

}
