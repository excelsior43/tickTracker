/**
 * 
 */
package com.tick.tracker.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tick.tracker.MessageDTO.StatisticsNotFoundException;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 * This is a generic Exception handler.
 * This handler returns 
 * HTTP 204 "NO CONTENT" is returned when @StatisticsNotFoundException is thrown when statistics data is unavailable
 * 
 */

@ControllerAdvice
public class TickExceptionHandler 
	extends ResponseEntityExceptionHandler { 
	
	@ExceptionHandler(value = { StatisticsNotFoundException.class })
	protected ResponseEntity<Object> handleConflict(
			StatisticsNotFoundException ex, WebRequest request) {
		return handleExceptionInternal(ex, "Nothing found to display", new HttpHeaders(), HttpStatus.NO_CONTENT, request);
		}
}