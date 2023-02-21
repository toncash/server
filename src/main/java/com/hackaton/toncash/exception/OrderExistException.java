package com.hackaton.toncash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class OrderExistException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8393148568195503751L;

	public OrderExistException(Long id) {
		super("User with id " + id + " is exist");
	}

}
