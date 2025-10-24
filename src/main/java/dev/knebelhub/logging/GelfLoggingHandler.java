package dev.knebelhub.logging;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import dev.knebelhub.logging.payload.GelfPayload;
import dev.knebelhub.logging.payload.GelfPayloadFactory;
import dev.knebelhub.logging.payload.GelfPayloadResult;
import dev.knebelhub.logging.sender.GelfMessageFactory;
import dev.knebelhub.logging.sender.GelfMessageStrategy;
import dev.knebelhub.logging.util.LoggerUtil;

/**
 * 
 * @author Luan Knebel
 * @since Oct 17, 2025
 */
public class GelfLoggingHandler extends Handler{
	
	private int graylogPort = 0;
    private String graylogHost;
    private String instanceName;
    private String graylogProtocol;
    private Map<String, String> additonalFields;
    private final AtomicReference<GelfMessageStrategy> gelfMessageReference = new AtomicReference<>();

	public void setGraylogHost(String graylogHost) {
		this.graylogHost = graylogHost;
	}

	public String getGraylogHost() {
		return graylogHost;
	}
	
	public void setGraylogPort(int graylogPort) {
		this.graylogPort = graylogPort;
	}
	
	public int getGraylogPort() {
		return graylogPort;
	}
	
	public void setGraylogProtocol(String graylogProtocol) {
		this.graylogProtocol = graylogProtocol;
	}
	
    public String getGraylogProtocol() {
		return graylogProtocol;
	}
    
    public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public void setAdditionalFields(String additionalFields) {
		if (Objects.nonNull(additionalFields)) {
			this.additonalFields = Arrays.stream(additionalFields.split(","))
					.map(String::trim)
					.filter(entry -> !entry.isEmpty())
					.map(entry -> entry.split("=", 2))
					.filter(parts -> parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank())
					.collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim(), (v1, v2) -> v1));
		}
	}

	public Map<String, String> getAdditonalFields() {
		return additonalFields;
	}

	@Override
	public void publish(LogRecord record) {
		
        GelfMessageStrategy sender = gelfMessageReference.updateAndGet(existing -> {
            if (Objects.isNull(existing)) {
                try {
                	LoggerUtil.log("Creating graylog connection...");
                    return GelfMessageFactory.getSender(graylogProtocol, graylogHost, graylogPort);
                } catch (Exception exception) {
                    throw new RuntimeException("Failed to initialize sender", exception);
                }
            }
            return existing;
        });
		
        if(Objects.nonNull(sender)) {
        	
        	GelfPayload gelfPayload = GelfPayloadFactory.makeMessage(record, instanceName, additonalFields);
        	GelfPayloadResult payloadResult = sender.sendMessage(gelfPayload);
        	
        	if(!GelfPayloadResult.SUCCESS.equals(payloadResult)) {
        		reportError(payloadResult.getMessage(), payloadResult.getException(), ErrorManager.WRITE_FAILURE);
        	}
        }
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void close() throws SecurityException {
		LoggerUtil.log("Closing graylog connection...");
		GelfMessageStrategy gelfMessageStrategy = gelfMessageReference.get();
		if(Objects.nonNull(gelfMessageStrategy)) {
			try {
				gelfMessageStrategy.close();
			} catch (Exception exception) {
				reportError("Failed to close sender", exception, ErrorManager.CLOSE_FAILURE);
			}
		}
	}

}
