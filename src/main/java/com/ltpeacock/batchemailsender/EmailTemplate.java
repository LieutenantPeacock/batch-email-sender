package com.ltpeacock.batchemailsender;

import java.util.List;
import java.util.Collections;

/**
 * Class to store the template for an email used by {@link MailWorker}.
 * It contains a {@link List} of email addresses to directly send to, 
 * a List of email addresses to send carbon copies to, a List of email addresses to 
 * send blind carbon copies to, the subject, a List of lines for the body, 
 * and a List of file paths for attachments.
 * <br>
 * Properties to be replaced by data from the CSV file consist of 
 * uppercase letters and underscores enclosed in <code>${}</code>, 
 * e.g. <code>${NAME}</code>.
 * @author LieutenantPeacock
 *
 */
public class EmailTemplate {
	private final List<String> to, cc, bcc;
	private final String subject;
	private final List<String> body, attachments;

	private EmailTemplate(Builder builder) {
		this.to = builder.to;
		this.cc = builder.cc;
		this.bcc = builder.bcc;
		this.subject = builder.subject;
		this.body = builder.body;
		this.attachments = builder.attachments;
	}

	public List<String> getTo() {
		return to;
	}

	public List<String> getCc() {
		return cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public String getSubject() {
		return subject;
	}

	public List<String> getBody() {
		return body;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	/**
	 * Creates builder to build {@link EmailTemplate}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link EmailTemplate}.
	 */
	public static final class Builder {
		private List<String> to = Collections.emptyList();
		private List<String> cc = Collections.emptyList();
		private List<String> bcc = Collections.emptyList();
		private String subject;
		private List<String> body = Collections.emptyList();
		private List<String> attachments = Collections.emptyList();

		private Builder() {
		}

		public Builder withTo(List<String> to) {
			this.to = to;
			return this;
		}

		public Builder withCc(List<String> cc) {
			this.cc = cc;
			return this;
		}

		public Builder withBcc(List<String> bcc) {
			this.bcc = bcc;
			return this;
		}

		public Builder withSubject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder withBody(List<String> body) {
			this.body = body;
			return this;
		}

		public Builder withAttachments(List<String> attachments) {
			this.attachments = attachments;
			return this;
		}

		public EmailTemplate build() {
			return new EmailTemplate(this);
		}
	}
}