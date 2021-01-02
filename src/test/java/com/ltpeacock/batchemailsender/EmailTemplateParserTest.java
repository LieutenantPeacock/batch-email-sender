package com.ltpeacock.batchemailsender;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link EmailTemplateParser}.
 * @author LieutenantPeacock
 *
 */
public class EmailTemplateParserTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test if a template string can be parsed properly by {@link EmailTemplateParser#parseTemplate(java.io.InputStream)}.
	 */
	@Test
	void testParseTemplate() {
		String templateStr = "[TO]\r\n"
				+ "${EMAIL}\r\n"
				+ "--------\r\n"
				+ "[CC]\r\n"
				+ "someone@email.com\r\n"
				+ "--------\r\n"
				+ "[BCC]\r\n"
				+ "--------\r\n"
				+ "[SUBJECT]\r\n"
				+ "Some Subject...\r\n"
				+ "--------\r\n"
				+ "[BODY]\r\n"
				+ "Dear ${NAME},\r\n"
				+ "\r\n"
				+ "This is some content.\r\n"
				+ "[-- This is a comment. --]\r\n"
				+ "--------\r\n"
				+ "[ATTACHMENTS]\r\n"
				+ "path\\to\\Image.png\r\n"
				+ "--------";
		EmailTemplate template = EmailTemplateParser
				.parseTemplate(new ByteArrayInputStream(templateStr.getBytes(StandardCharsets.UTF_8)));
		assertIterableEquals(Arrays.asList("${EMAIL}"), template.getTo());
		assertIterableEquals(Arrays.asList("someone@email.com"), template.getCc());
		assertIterableEquals(Collections.emptyList(), template.getBcc());
		assertEquals("Some Subject...", template.getSubject());
		assertIterableEquals(Arrays.asList("Dear ${NAME},", "", "This is some content."), template.getBody());
		assertIterableEquals(Arrays.asList("path\\to\\Image.png"), template.getAttachments());
	}

}
