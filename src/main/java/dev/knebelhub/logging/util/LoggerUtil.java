package dev.knebelhub.logging.util;

import dev.knebelhub.logging.GelfLoggingHandler;

/**
 * Logging internal information without Java Logger API
 * @autor Luan Knebel
 * @since Oct 17, 2025
 */
public class LoggerUtil {

	public static void log(String message) {
		System.out.println(GelfLoggingHandler.class.getName() + " " + message);
	}
	
}
