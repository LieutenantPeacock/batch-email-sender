package com.ltpeacock.batchemailsender;

/**
 * Class holding String constants for content types for emails.
 * @author LieutenantPeacock
 *
 */
public abstract class MailContentTypes {
	private MailContentTypes() {
	}
	
	/**
	 * This content type indicates that the email uses HTML.
	 */
	public static final String HTML = "text/html";
	/**
	 * This content type indicates that the email should be sent as plain text.
	 */
	public static final String TEXT = "text/plain";
}
