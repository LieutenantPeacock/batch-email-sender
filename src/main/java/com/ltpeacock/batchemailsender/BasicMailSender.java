package com.ltpeacock.batchemailsender;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ltpeacock.batchemailsender.exception.ErrorCode;
import com.ltpeacock.batchemailsender.exception.MailSendingException;

/**
 * A simple implementation of {@link MailSender} using a {@link Properties} instance,
 * the username, and the password contained in a {@link MailServerInfo}.
 * @author LieutenantPeacock
 *
 */
public class BasicMailSender implements MailSender {
	private static final Logger LOG = LoggerFactory.getLogger(BasicMailSender.class);
	private final Session session;
	private final boolean dryRun;

	/**
	 * Constructs a {@link BasicMailSender}.
	 * @param serverInfo The {@link MailServerInfo} object containing the SMTP properties, the username, and the password.
	 */
	public BasicMailSender(final MailServerInfo serverInfo) {
		this(serverInfo, false);
	}
	
	/**
	 * Constructs a {@link BasicMailSender}.
	 * @param serverInfo The {@link MailServerInfo} object containing the SMTP properties, the username, and the password.
	 * @param dryRun If set to {@code true}, the emails will not actually be sent.
	 */
	public BasicMailSender(final MailServerInfo serverInfo, final boolean dryRun) {
		this.session = Session.getInstance(serverInfo.getProperties(), new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(serverInfo.getUsername(), serverInfo.getPassword());
			}
		});
		this.dryRun = dryRun;
	}

	@Override
	public void sendEmail(final String[] to, final String[] cc, final String[] bcc, final String subject,
			final String body, final String contentType, final File[] attachments) throws MailSendingException {
		try {
			if (nullOrEmpty(to) && nullOrEmpty(cc) && nullOrEmpty(bcc)) {
				throw new MailSendingException(ErrorCode.NO_RECIPIENTS);
			}
			final Message message = new MimeMessage(session);
			message.setSubject(subject);
			for (final String email : to) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			}
			if (cc != null) {
				for (final String email : cc) {
					message.addRecipient(Message.RecipientType.CC, new InternetAddress(email));
				}
			}
			if (bcc != null) {
				for (final String email : bcc) {
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
				}
			}
			final Multipart multipart = new MimeMultipart();
			final BodyPart messageBody = new MimeBodyPart();
			messageBody.setContent(body, contentType);
			multipart.addBodyPart(messageBody);
			if(attachments != null) {
				for(final File attachment: attachments) {
					final MimeBodyPart attachmentPart = new MimeBodyPart();
					attachmentPart.attachFile(attachment);
					multipart.addBodyPart(attachmentPart);
				}
			}
			message.setContent(multipart);
			if(!dryRun) {
				Transport.send(message);
			}
		} catch (AddressException e) {
			throw new MailSendingException(ErrorCode.INVALID_EMAIL_ADDRESS, e, true);
		} catch (MessagingException e) {
			throw new MailSendingException(ErrorCode.ERROR_SENDING, e, true);
		} catch (IOException e) {
			throw new MailSendingException(ErrorCode.IO_ERROR, e, true);
		}
	}
	
	private static boolean nullOrEmpty(final String[] arr) {
		return arr == null || arr.length == 0;
	}
}
