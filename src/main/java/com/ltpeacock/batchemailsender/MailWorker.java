package com.ltpeacock.batchemailsender;

import static com.ltpeacock.batchemailsender.LogMarkers.CONSOLE;
import static com.ltpeacock.batchemailsender.LogMarkers.DRY_RUN;
import static com.ltpeacock.batchemailsender.LogMarkers.EMAIL_ARCHIVE;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.ltpeacock.batchemailsender.exception.ErrorCode;
import com.ltpeacock.batchemailsender.exception.MailSendingException;

/**
 * Worker class for sending batch emails with replaceable parameters.
 * 
 * @author LieutenantPeacock
 *
 */
public class MailWorker {
	private static final Logger LOG = LoggerFactory.getLogger(MailWorker.class);
	private final MailSender mailSender;
	private static final Pattern templatePattern = Pattern.compile("\\$\\{([A-Z0-9_]+)\\}");
	private final boolean dryRun;

	/**
	 * Constructs a MailWorker.
	 * 
	 * @param mailSender The {@link MailSender} implementation to use for sending
	 *                   emails.
	 */
	public MailWorker(final MailSender mailSender) {
		this(mailSender, false);
	}

	/**
	 * Constructs a MailWorker.
	 * 
	 * @param mailSender The {@link MailSender} implementation to use for sending
	 *                   emails.
	 * @param dryRun     If set to {@code true}, the MailWorker will log emails, but
	 *                   not actually send them.
	 */
	public MailWorker(final MailSender mailSender, final boolean dryRun) {
		this.mailSender = mailSender;
		this.dryRun = dryRun;
	}

	/**
	 * Send batch templated emails. Sends one email for each row (apart from the
	 * header) in the CSV data.
	 * 
	 * @param is       The InputStream to read the CSV data from. This is the data
	 *                 that is used in substituting the actual values for
	 *                 replaceable parameters in the email template.
	 * @param template The {@link EmailTemplate} to use for each email.
	 * @throws MailSendingException If one particular email cannot be sent
	 */
	public void sendTemplatedEmails(final InputStream is, final EmailTemplate template) throws MailSendingException {
		sendTemplatedEmails(is, template, 1, Integer.MAX_VALUE);
	}

	/**
	 * Send batch templated emails.
	 * 
	 * @param is       The InputStream to read the CSV data from. This is the data
	 *                 that is used in substituting the actual values for
	 *                 replaceable parameters in the email template.
	 * @param template The {@link EmailTemplate} to use for each email.
	 * @param startIdx The index of the first email to send (one-indexed), counting
	 *                 from the first line of the CSV data.
	 * @param endIdx   The index of the last email to send (one-indexed). To send
	 *                 emails for each row from the {@code startIdx} to the end of
	 *                 the CSV data, it is valid to specify an index larger than the
	 *                 number of rows in the file. `Integer.MAX_VALUE` may be used
	 *                 to indicate setting the end point to the end of the data.
	 * @throws MailSendingException If one particular email cannot be sent
	 */
	public void sendTemplatedEmails(final InputStream is, final EmailTemplate template, final int startIdx,
			final int endIdx) throws MailSendingException {
		int count = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			LOG.info("Sending templated emails");
			final ProgressDotPrinter dotPrinter = new ProgressDotPrinter(50);
			final Marker messageMarker = this.dryRun ? DRY_RUN : EMAIL_ARCHIVE;
			final long start = System.currentTimeMillis();
			int sentCount = 0;
			for (CSVRecord record : CSVFormat.RFC4180.withFirstRecordAsHeader().parse(br)) {
				++count;
				if (startIdx <= count && count <= endIdx) {
					MDC.put(MdcKeys.CSV_LINE_NUM, String.valueOf(count));
					final Map<String, String> map = record.toMap();
					if (Boolean.parseBoolean(map.get("SKIP_RECORD"))) {
						LOG.info("Skipping record");
						dotPrinter.skip();
						continue;
					}
					final Function<String, String> replacer = str -> replaceTemplateParameters(str, map);
					final Predicate<String> notEmpty = str -> !str.trim().isEmpty();
					final String[] to = template.getTo().stream().map(replacer).filter(notEmpty).toArray(String[]::new);
					final String[] cc = template.getCc().stream().map(replacer).filter(notEmpty).toArray(String[]::new);
					final String[] bcc = template.getBcc().stream().map(replacer).filter(notEmpty)
							.toArray(String[]::new);
					final String subject = replacer.apply(template.getSubject());
					final String body = template.getBody().stream().map(replacer).collect(Collectors.joining("<br>"));
					final File[] attachments = template.getAttachments().stream().map(replacer).filter(notEmpty)
							.map(File::new).toArray(File[]::new);
					boolean invalid = false;
					for (final File attachment : attachments) {
						if (!attachment.exists()) {
							LOG.error("No attachment file found with path {}", attachment.getAbsolutePath());
							invalid = true;
						}
					}
					if (invalid) {
						throw new IllegalArgumentException("Invalid file path(s) specified");
					}
					LOG.info("Sending message. TO: [{}], CC: [{}], BCC: [{}]", Arrays.toString(to), Arrays.toString(cc),
							Arrays.toString(bcc));
					LOG.info(messageMarker, "Sending message. TO: [{}], CC: [{}], BCC: [{}], SUBJECT: [{}], BODY: [{}]",
							Arrays.toString(to), Arrays.toString(cc), Arrays.toString(bcc), subject, body);
					this.mailSender.sendEmail(to, cc, bcc, subject, body, MailContentTypes.HTML, attachments);
					dotPrinter.dot();
					++sentCount;
				} else if (count > endIdx) {
					break;
				}
			}
			dotPrinter.done();
			MDC.remove(MdcKeys.CSV_LINE_NUM);
			final long end = System.currentTimeMillis();
			final double totalSeconds = (end - start) / 1000d, secondsPerEmail = totalSeconds / sentCount,
					throughput = 60 / secondsPerEmail;
			LOG.info(CONSOLE,
					"Total time: [{}] seconds, Average time per email: [{}] seconds, Throughput: [{}] emails/minute",
					String.format("%.1f", totalSeconds), String.format("%.1f", secondsPerEmail),
					String.format("%.1f", throughput));
		} catch (IOException e) {
			throw new MailSendingException(ErrorCode.ERROR_READING_DATA, e);
		} catch (MailSendingException e) {
			LOG.warn(
					"Aborted operation due to {}. To resume sending emails from where the program left off,"
							+ " fix the issue (if applicable) and then run the program with -Dstart={}",
					e.getMessage(), count);
			throw e;
		} finally {
			MDC.remove(MdcKeys.CSV_LINE_NUM);
		}
	}

	private static String replaceTemplateParameters(final String str, final Map<String, String> map) {
		return replaceAll(str, templatePattern, mr -> {
			final String group = mr.group(1);
			final String value = map.get(group);
			if (value == null)
				throw new IllegalStateException("No value found for token " + group);
			return value;
		});
	}

	private static String replaceAll(final String str, final Pattern pattern,
			final Function<MatchResult, String> callback) {
		final Matcher matcher = pattern.matcher(str);
		final StringBuffer sb = new StringBuffer(str.length());
		while (matcher.find()) {
			matcher.appendReplacement(sb, callback.apply(matcher.toMatchResult()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
