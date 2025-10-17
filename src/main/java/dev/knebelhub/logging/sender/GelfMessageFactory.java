package dev.knebelhub.logging.sender;

import java.util.Objects;

/**
 * 
 * @autor Luan Knebel
 * @since Oct 17, 2025
 */
public class GelfMessageFactory {

	public static GelfMessageStrategy getSender(String protocol, String host, int port) {

		validateSenderProperties(host, port);

		return switch (protocol.toLowerCase()) {
			case "udp" -> new GelfMessageUDPImpl(host, port);
			case "tcp" -> new GelfMessageUDPImpl(host, port);
			default -> new GelfMessageUDPImpl(host, port);
		};

	}

	private static void validateSenderProperties(String graylogHost, int graylogPort) {
		if (Objects.isNull(graylogHost) || graylogHost.isEmpty()) {
			throw new IllegalArgumentException("Invalid graylogHost property");
		}
		if (graylogPort <= 0) {
			throw new IllegalArgumentException("Invalid graylogPort property");
		}
	}

}
