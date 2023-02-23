package com.hackaton.toncash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = -8393111568194409951L;

	public OrderNotFoundException(String id) {
		super("Order with id " + id + " doesn't exist");
	}

}
