package com.ltpeacock.batchemailsender.exception;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public class MailSendingException extends Exception {
	private static final long serialVersionUID = 542446978477601320L;
	private final ErrorCode errorCode;
	
	public MailSendingException(final ErrorCode errorCode) {
		this(errorCode, null, null);
	}
	
	public MailSendingException(final ErrorCode errorCode, String message) {
		this(errorCode, message, null);
	}

	public MailSendingException(final ErrorCode errorCode, Throwable cause) {
		this(errorCode, null, cause);
	}
	
	public MailSendingException(final ErrorCode errorCode, Throwable cause, boolean useCauseMessage) {
		this(errorCode, null, cause, useCauseMessage);
	}
	
	public MailSendingException(final ErrorCode errorCode, String message, Throwable cause) {
		this(errorCode, message, cause, false);
	}

	public MailSendingException(final ErrorCode errorCode, String message, Throwable cause, boolean useCauseMessage) {
		super("ErrorCode[" + errorCode.getErrorCode() + "]: "
				+ (message != null ?
						message
						: (useCauseMessage && cause != null ? 
							cause.getMessage() 
							: errorCode.getDescription())),
				cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}
}
