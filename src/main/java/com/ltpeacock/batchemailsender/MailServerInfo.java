package com.ltpeacock.batchemailsender;

import java.util.Properties;

/**
 * Class to store the SMTP properties, username, and password for sending an email.
 * It is used in {@link BasicMailSender}.
 * @author LieutenantPeacock
 *
 */
public class MailServerInfo {
	private final Properties properties;
	private final String username, password;

	private MailServerInfo(Builder builder) {
		this.properties = new Properties();
		properties.put("mail.smtp.host", builder.host);
		properties.put("mail.smtp.port", builder.port);
		properties.put("mail.smtp.auth", "true");
		if (builder.tls) {
			properties.put("mail.smtp.starttls.enable", "true");
		} else {
			properties.put("mail.smtp.socketFactory.port", builder.port);
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
		this.username = builder.username;
		this.password = builder.password;
	}

	public Properties getProperties() {
		return properties;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Creates builder to build {@link MailServerInfo}.
	 * 
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link MailServerInfo}.
	 */
	public static final class Builder {
		private String host;
		private String port;
		private String username;
		private String password;
		private boolean tls;

		private Builder() {
		}

		public Builder withHost(String host) {
			this.host = host;
			return this;
		}

		public Builder withPort(String port) {
			this.port = port;
			return this;
		}

		public Builder withUsername(String username) {
			this.username = username;
			return this;
		}

		public Builder withPassword(String password) {
			this.password = password;
			return this;
		}

		public Builder withTls(boolean tls) {
			this.tls = tls;
			return this;
		}

		public MailServerInfo build() {
			return new MailServerInfo(this);
		}
	}
}