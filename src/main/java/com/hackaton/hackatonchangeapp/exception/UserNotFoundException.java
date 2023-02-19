package com.hackaton.hackatonchangeapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8393111568194403751L;

	public UserNotFoundException(String username) {
		super("User with username " + username + " doesn't exist");
	}

}
