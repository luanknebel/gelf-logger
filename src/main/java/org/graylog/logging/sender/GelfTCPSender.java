package org.graylog.logging.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.ErrorManager;

import org.graylog.logging.payload.GelfPayload;
import org.graylog.logging.payload.GelfPayloadResult;

public class GelfTCPSender implements GelfSender {
	
	private int port;
	private String host;
	private Socket socket;
    private OutputStream outputStream;
    private boolean shutdown = false;

    public GelfTCPSender() {
    }

	public GelfTCPSender(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			this.socket = new Socket(host, port);
			this.outputStream = socket.getOutputStream();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public GelfPayloadResult sendMessage(GelfPayload message) {
		if(shutdown) {
			return GelfPayloadResult.SHUTTING_DOWN;
		}
		try {
			if (Objects.isNull(socket) || Objects.isNull(outputStream)) {
				socket = new Socket(host, port);
                outputStream = socket.getOutputStream();
			}
            outputStream.write(message.getTCPPayload().array());
			return GelfPayloadResult.SUCCESS;
		} catch (IOException exception) {
			socket = null;
			return new GelfPayloadResult(ErrorManager.WRITE_FAILURE, exception);
		}
	}

	public void close() throws IOException {
		shutdown = true;
        if (Objects.nonNull(outputStream)){
            outputStream.close();
        }
		if (Objects.nonNull(socket)) {
			socket.close();
		}
	}
}
