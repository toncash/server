package com.hackaton.toncash.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class UserExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8393148568194403751L;

	public UserExistException(Long id) {
		super("User with id " + id + " is exist");
	}

}
