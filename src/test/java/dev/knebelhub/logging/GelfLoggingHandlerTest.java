package dev.knebelhub.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Integration test with a localhost UPD Server
 * @author Luan Knebel
 * @date 13/10/2025
 */
public class GelfLoggingHandlerTest {

	private static final int TEST_PORT = 12210;

	private Logger logger;
	private GelfLoggingHandler handler;
	private DatagramSocket udpServer;
	private ExecutorService executor;
	private Future<String> receivedMessageFuture;

	@BeforeEach
	void setup() throws Exception {
		logger = Logger.getLogger(GelfLoggingHandlerTest.class.getName());
		handler = new GelfLoggingHandler();
		handler.setGraylogHost("127.0.0.1");
		handler.setGraylogPort(TEST_PORT);
		handler.setInstanceName("test-instance");
		logger.addHandler(handler);

		udpServer = new DatagramSocket(TEST_PORT);
		executor = Executors.newSingleThreadExecutor();

		receivedMessageFuture = executor.submit(() -> {
			byte[] buffer = new byte[8192];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			udpServer.receive(packet);
			return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
		});
	}

	@AfterEach
	void tearDown() {
		if (Objects.nonNull(udpServer) && !udpServer.isClosed()) {
			udpServer.close();
		}
		if (Objects.nonNull(executor)) {
			executor.shutdownNow();
		}
	}

	@Test
	void testHandlerConfiguration() {
		assertEquals("127.0.0.1", handler.getGraylogHost(), "Graylog host should be 127.0.0.1");
		assertEquals(TEST_PORT, handler.getGraylogPort(), "Graylog port should match test port");
		assertEquals("test-instance", handler.getInstanceName(), "Instance name should match 'test-instance'");
	}

	@Test
	void testInfoLogMessageIsSentOverUdp() throws Exception {
		logger.info("Integration test message via UDP");
		
		String received = receivedMessageFuture.get(3, TimeUnit.SECONDS);
		assertNotNull(received, "The UDP server should receive a message");
	}

	@Test
	void testLogExceptionIsSentOverUdp() throws Exception {
		Exception exception = new RuntimeException("Simulated exception from test");
		logger.log(Level.SEVERE, "Logging an exception to Graylog", exception);
		
		String received = receivedMessageFuture.get(3, TimeUnit.SECONDS);
		assertNotNull(received, "UDP server should receive an exception log message");
	}
}