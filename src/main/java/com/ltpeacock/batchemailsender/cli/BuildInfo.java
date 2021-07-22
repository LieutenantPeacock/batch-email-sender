package com.ltpeacock.batchemailsender.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author LieutenantPeacock
 *
 */
class BuildInfo {
	public static final String BUILD_VERSION = "git.build.version";
	public static final String BUILD_TIME = "git.build.time";
	private static Map<String, String> map;
	
	static {
		init();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void init(){
		if (map == null) {
			try (InputStream is = BuildInfo.class.getResourceAsStream("/git.properties")) {
				final Properties properties = new Properties();
				properties.load(is);
				map = new HashMap<>((Map) properties);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static String getProperty(final String propertyName) {
		return map.get(propertyName);
	}
}