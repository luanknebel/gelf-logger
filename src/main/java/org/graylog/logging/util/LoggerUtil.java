package org.graylog.logging.util;

import org.graylog.logging.GelfLoggingHandler;

/**
 * Logging internal information without Java Logger API
 * @author Luan Knebel
 * @date 13/10/2025
 */
public class LoggerUtil {

	public static void log(String message) {
		System.out.println(GelfLoggingHandler.class.getName() + " " + message);
	}
	
}
