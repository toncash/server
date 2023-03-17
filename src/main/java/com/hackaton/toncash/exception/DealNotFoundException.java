package com.hackaton.toncash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class DealNotFoundException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8393111577194409951L;

	public DealNotFoundException(String id) {
		super("Deal with id " + id + " doesn't exist");
	}

}
