package com.ltpeacock.batchemailsender.exception;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public enum ErrorCode {
	NO_RECIPIENTS("0001", "No recipients"),
	INVALID_EMAIL_ADDRESS("0002", "Invalid email address"),
	ERROR_SENDING("0003", "Error sending email"),
	IO_ERROR("0004", "I/O error"),
	ERROR_READING_DATA("0005", "Error reading data file")
	;
	private final String errorCode, description;

	ErrorCode(final String errorCode, final String description) {
		this.errorCode = errorCode;
		this.description = description;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getDescription() {
		return description;
	}
}