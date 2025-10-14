package org.graylog.logging;

import java.util.logging.Logger;

public class Main {

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger(Main.class.getName());
		GelfLoggingHandler handler = new GelfLoggingHandler();
		handler.setGraylogHost("127.0.0.1");
		handler.setGraylogPort(12201);
		handler.setInstanceName("application-name");
		handler.setGraylogProtocol("udp");
		handler.setAdditionalFields("{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}");
		logger.addHandler(handler);
		logger.info("sending log message to graylog server");
	}
	
}
