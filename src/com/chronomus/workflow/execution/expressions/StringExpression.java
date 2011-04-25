package com.chronomus.workflow.execution.expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chronomus.workflow.execution.VariableStore;

public class StringExpression implements Expression {

	private final String value;

	public StringExpression(String value) {
		this.value = unescapeString(value);	
	}
	
	public Primitive evaluate(VariableStore context) {
		String stringContent = value;// unescapeString(value);
		stringContent = replaceVariables(context, stringContent);
		return new StringPrimitive(stringContent);
	}

	private String unescapeString(String escapedString) {
		// TODO(rmapes): finalise list of escapables
		// ["n","t","b","r","f","\\","'","\""]
		escapedString = escapedString.replaceAll("\\\\n", "\n");
		escapedString = escapedString.replaceAll("\\\\t", "\t");
		escapedString = escapedString.replaceAll("\\\\b", "\b");
		escapedString = escapedString.replaceAll("\\\\r", "\r");
		escapedString = escapedString.replaceAll("\\\\f", "\f");
		escapedString = escapedString.replaceAll("\\\\'", "\'");
		escapedString = escapedString.replaceAll("\\\\\"", "\"");
		return escapedString;
	}

	private String replaceVariables(VariableStore context, String stringContent) {
		Pattern variables = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = variables.matcher(stringContent);
		while (matcher.find()) {
			for (int i = 0; i < matcher.groupCount(); i++) {
				String replacementString = matcher.group(i);
				String variableName = replacementString.substring(2, replacementString.length() -1);
				stringContent = stringContent.replace(replacementString, context.getProperty(variableName));
			}
			matcher = variables.matcher(stringContent);
		}
		return stringContent;
	}

	public String toString() {
		return value;
	}

}
