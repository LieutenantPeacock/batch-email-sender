package com.ltpeacock.batchemailsender;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public abstract class LogMarkers {
	public static final Marker CONSOLE = MarkerFactory.getMarker("CONSOLE");
	public static final Marker EMAIL_ARCHIVE = MarkerFactory.getMarker("EMAIL_ARCHIVE");
	public static final Marker DRY_RUN = MarkerFactory.getMarker("DRY_RUN");
	public static final Marker NOT_SENT = MarkerFactory.getMarker("NOT_SENT");

	static {
		NOT_SENT.add(CONSOLE);
		DRY_RUN.add(EMAIL_ARCHIVE);
	}

	private LogMarkers() {
	}
}