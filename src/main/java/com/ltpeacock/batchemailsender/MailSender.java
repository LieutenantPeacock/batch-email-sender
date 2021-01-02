package com.ltpeacock.batchemailsender;

import java.io.File;

import com.ltpeacock.batchemailsender.exception.MailSendingException;

/**
 * Interface for classes capable of sending emails to implement.
 * @author LieutenantPeacock
 *
 */
public interface MailSender {
	default void sendEmail(String to, String subject, String body, String contentType) throws MailSendingException {
		sendEmail(new String[] { to }, null, null, subject, body, contentType, null);
	}
	
	default void sendEmail(String to, String subject, String body, String contentType, File[] attachments) throws MailSendingException {
		sendEmail(new String[] { to }, null, null, subject, body, contentType, attachments);
	}

	default void sendEmail(String to, String cc, String bcc, String subject, String body, String contentType) throws MailSendingException {
		sendEmail(new String[] { to }, new String[] { cc }, new String[] { bcc }, subject, body, contentType, null);
	}

	/**
	 * Sends an email.
	 * @param to An array of email addresses to send to. May be null.
	 * @param cc An array of email addresses to send carbon copies to. May be null.
	 * @param bcc An array of email addresses to send blind carbon copies to. May be null.
	 * @param subject The subject of the email.
	 * @param body The content of the email.
	 * @param contentType The type of content that the email contains. See {@link MailContentTypes} for the values of some common ones.
	 * @param attachments An array of {@link File} attachments to include in the email. May be null.
	 * @throws MailSendingException If the email cannot be sent
	 */
	void sendEmail(String[] to, String[] cc, String[] bcc, String subject, String body, String contentType, File[] attachments) throws MailSendingException;
}