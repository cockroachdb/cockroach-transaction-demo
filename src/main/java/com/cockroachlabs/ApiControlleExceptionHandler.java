package com.cockroachlabs;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Exception handler for the {@link ApiController}.
 *
 * @author Greg L. Turnquist
 */
@ControllerAdvice(basePackageClasses = ApiController.class)
class ApiControlleExceptionHandler {

	/**
	 * Transform an {@link InsufficientInventory} into an RFC-7807 {@link ProblemDetail} record.
	 */
	@ResponseBody
	@ExceptionHandler(InsufficientInventory.class)
	public ProblemDetail handleControllerException(HttpServletRequest request, Throwable ex) {

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(getStatus(request), ex.getMessage());
		problemDetail.setTitle(ex.getMessage());
		return problemDetail;
	}

	/**
	 * Attempt to extract the {@link HttpStatus} and a given {@link HttpServletRequest}.
	 */

	private HttpStatus getStatus(HttpServletRequest request) {

		return Optional //
				.ofNullable((Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)) //
				.map(HttpStatus::resolve) //
				.orElse(HttpStatus.PRECONDITION_FAILED);
	}
}
