package com.ltpeacock.batchemailsender;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to create an {@link EmailTemplate} from an {@link InputStream}.
 * @author LieutenantPeacock
 *
 */
public class EmailTemplateParser {
	/**
	 * Parses the content from an InputStream as an EmailTemplate.
	 * Each section is indicated by {@code [SECTION_NAME]}, e.g. {@code [TO]}.
	 * Sections are separated by a line of at least 3 dashes (-).
	 * @param is The {@link InputStream} to read from.
	 * @return The {@link EmailTemplate} parsed from the InputStream.
	 */
	public static EmailTemplate parseTemplate(final InputStream is) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			List<String> to = new ArrayList<>(), cc = new ArrayList<>(), bcc = new ArrayList<>(),
					body = new ArrayList<>(), attachments = new ArrayList<>();
			String subject = null;
			for (String line; (line = br.readLine()) != null;) {
				final List<String> curr = new ArrayList<>();
				final String section = line;
				while ((line = br.readLine()) != null && !line.matches("---+")) {
					if (!(line.startsWith("[--") && line.endsWith("--]"))) {
						curr.add(line);
					}
				}
				if (section.equals("[TO]")) {
					to = curr;
				} else if (section.equals("[CC]")) {
					cc = curr;
				} else if (section.equals("[BCC]")) {
					bcc = curr;
				} else if (section.equals("[SUBJECT]")) {
					if (curr.isEmpty())
						throw new IllegalStateException("No subject");
					subject = curr.get(0);
				} else if (section.equals("[BODY]")) {
					body = curr;
				} else if(section.equals("[ATTACHMENTS]")) {
					attachments = curr;
				} else {
					throw new IllegalStateException("Unrecognized section");
				}
			}
			return EmailTemplate.builder().withSubject(subject).withTo(to)
					.withCc(cc).withBcc(bcc)
					.withBody(body).withAttachments(attachments)
					.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}