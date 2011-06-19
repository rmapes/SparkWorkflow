package com.chronomus.workflow.execution.expressions.primitives;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.chronomus.workflow.execution.ExecutionException;
import com.chronomus.workflow.execution.expressions.Operators.Binary;

public class DatePrimitive implements Primitive {
	
	DateFormat df = new SimpleDateFormat("dd MMM yyyy");

	private final String dateString;

	public DatePrimitive(String dateString) {
		this.dateString = dateString;		
	}

	public DatePrimitive(Date date) {
		dateString = df.format(date);
	}

	@Override
	public String evaluate() {
		return dateString;
	}

	public Primitive apply(TimespanPrimitive rightPrim, Binary op) throws ExecutionException {
		DatePrimitive output;
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(dateString));
		} catch (ParseException e) {
			throw new ExecutionException(e.getMessage());
		}
		int rollAmount = rightPrim.getMultiplier().toValue().intValue();
		switch (op) {
		case plus:
			break;
		case minus:
			rollAmount = -rollAmount;
			break;
		default:
			throw new ExecutionException("Unrecognised operator for date function: " + op);
		}
		int field = 0;
		switch (rightPrim.getType()) {
		case day:
			field = Calendar.DATE;
		}
		cal.add(field, rollAmount);
		output = new DatePrimitive(cal.getTime());
		return output;
	}

}
