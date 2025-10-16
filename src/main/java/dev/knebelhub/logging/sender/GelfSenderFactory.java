package dev.knebelhub.logging.sender;

import java.util.Objects;

/**
 * 
 * @author Luan Knebel
 * @date 13/10/2025
 */
public class GelfSenderFactory {

	public static GelfSender getSender(String graylogProtocol, String graylogHost, int graylogPort) {
		
		validateSenderProperties(graylogHost, graylogPort);
		
		if(Objects.equals("tcp", graylogProtocol)) {
			return new GelfTCPSender(graylogHost, graylogPort);
		}
		return new GelfUDPSender(graylogHost, graylogPort);
	}

	private static void validateSenderProperties(String graylogHost, int graylogPort) {
		if(Objects.isNull(graylogHost) || graylogHost.isEmpty()) {
			throw new IllegalArgumentException("Invalid graylogHost property");
		}
		if(graylogPort <= 0) {
			throw new IllegalArgumentException("Invalid graylogPort property");
		}
	}
	
}
