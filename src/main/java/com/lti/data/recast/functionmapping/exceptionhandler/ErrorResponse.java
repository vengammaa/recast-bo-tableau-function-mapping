package com.lti.data.recast.functionmapping.exceptionhandler;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse<T> {
	/**
	 * To show error message for exception handling
	 */
	private List<T> errors = new ArrayList<>(1);

	public ErrorResponse(List<T> errors) {
		this.errors = errors;
	}

	public List<T> getErrors() {
		return errors;
	}

}
