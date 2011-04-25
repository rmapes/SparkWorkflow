package com.chronomus.workflow.parsers;

import java.io.StreamTokenizer;

public class ParseError extends Exception {

	public ParseError(String errMsg, StreamTokenizer tokenizer) {
		super(String.format("At line %d: %s", tokenizer.lineno(), errMsg));
	}

}
