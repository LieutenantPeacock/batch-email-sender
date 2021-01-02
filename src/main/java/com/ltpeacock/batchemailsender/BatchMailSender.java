package com.ltpeacock.batchemailsender;
import static com.ltpeacock.batchemailsender.LogMarkers.CONSOLE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ltpeacock.batchemailsender.exception.MailSendingException; 

/**
 * @author LieutenantPeacock
 */
public class BatchMailSender {
	private static final Logger LOG = LoggerFactory.getLogger(BatchMailSender.class);
	
	public static void main(final String[] args) {
		try {
			System.out.format("BatchEmailSender Version %s by LieutenantPeacock%n", 
					BuildInfo.getProperty(BuildInfo.BUILD_VERSION));
			LOG.info("BatchEmailSender Version {} by LieutenantPeacock", 
					BuildInfo.getProperty(BuildInfo.BUILD_VERSION));
			final Properties props = new Properties();
			boolean valid = true;
			if(args.length > 0) {
				final File file = new File(args[0]);
				if(!file.exists()) {
					valid = false;
					LOG.error("No properties file found with path: {}", file.getAbsolutePath());
				} else {
					props.load(new FileInputStream(args[0]));
				}
			}
			final String configFilename = getProperty("config", "Config.properties", props);
			final String dataFilename = getProperty("data", "Data.csv", props);
			final String templateFilename = getProperty("template", "Template.txt", props);
			final String start = getProperty("start", null, props);
			final String end = getProperty("end", null, props);
			LOG.info(CONSOLE, "Config filename: [{}]", configFilename);
			LOG.info(CONSOLE, "Data filename: [{}]", dataFilename);
			LOG.info(CONSOLE, "Template filename: [{}]", templateFilename);
			final File configFile = new File(configFilename);
			final File dataFile = new File(dataFilename);
			final File templateFile = new File(templateFilename);
			if (!configFile.exists()) {
				LOG.error("No config file found with path: {}", configFile.getAbsolutePath());
				valid = false;
			}
			if (!dataFile.exists()) {
				LOG.error("No data file found with path: {}", dataFile.getAbsolutePath());
				valid = false;
			}
			if (!templateFile.exists()) {
				LOG.error("No template file found with path: {}", templateFile.getAbsolutePath());
				valid = false;
			}
			int startIdx = 1, endIdx = Integer.MAX_VALUE;
			if(start != null) {
				try {
					startIdx = Integer.parseInt(start);
					if(startIdx <= 0) {
						LOG.error("start parameter [{}] must be positive.", start);
						valid = false;
					}
				} catch(NumberFormatException e) {
					LOG.error("Provided start parameter [{}] is not a valid integer.", start);
					valid = false;
				}
			} else {
				LOG.info(CONSOLE, "start parameter not provided; defaults to 1.");
			}
			if(end != null) {
				try {
					endIdx = Integer.parseInt(end);
					if(endIdx <= 0) {
						LOG.error("end parameter [{}] must be positive.", end);
						valid = false;
					}
				} catch(NumberFormatException e) {
					LOG.error("Provided end parameter [{}] is not a valid integer.", end);
					valid = false;
				}
			} else {
				LOG.info(CONSOLE, "end parameter not provided; defaults to the end of the data file.");
			}
			if(endIdx < startIdx) {
				LOG.error("end [{}] cannot be less than start [{}]", end, start);
				valid = false;
			}
			if (valid) {
				final Properties emailProps = new Properties();
				emailProps.load(new FileInputStream(configFile));
				final boolean dryRun = getProperty("dryRun", null, props) != null;
				final BasicMailSender mailSender = new BasicMailSender(MailServerInfo.builder()
						.withHost(emailProps.getProperty("host"))
						.withPort(emailProps.getProperty("port"))
						.withTls(Boolean.parseBoolean(emailProps.getProperty("tls")))
						.withUsername(emailProps.getProperty("username"))
						.withPassword(emailProps.getProperty("password"))
						.build(), dryRun);
				LOG.info(CONSOLE, "Sending batch emails");
				if (dryRun) {
					LOG.warn("<<<<< Dry run: not actually sending any emails. >>>>>");
				}
				new MailWorker(mailSender, dryRun)
					.sendTemplatedEmails(new FileInputStream(dataFile),
						EmailTemplateParser.parseTemplate(new FileInputStream(templateFile)),
						startIdx, endIdx);
				if (dryRun) {
					LOG.warn("Dry run completed.");
				} else {
					LOG.info(CONSOLE, "Emails sent!");
				}
			}
		} catch (IOException e) {
			LOG.error("IOException", e);
		} catch (MailSendingException e) {
			LOG.error("MailSendingException", e);
		}
	}
	
	private static String getProperty(final String key,
			final String defaultValue,
			final Properties props) {
		String value;
		if ((value = System.getProperty(key)) != null) {
			LOG.debug("Obtained value [{}] for key [{}] from System properties", value, key);
		} else if ((value = props.getProperty(key)) != null) {
			LOG.debug("Obtained value [{}] for key [{}] from provided properties file", value, key);
		} else {
			value = defaultValue;
			LOG.debug("Using default value [{}] for key [{}]", value, key);
		}
		return value;
	}
}
