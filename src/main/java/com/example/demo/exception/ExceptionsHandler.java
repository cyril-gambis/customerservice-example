package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {

	@ExceptionHandler(BadArgumentException.class)
	public ResponseEntity<?> handleMissingTenantIdException(BadArgumentException exc) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
	}
	
}
