package dev.knebelhub.logging.payload;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.google.gson.Gson;

/**
 * 
 * @author Luan Knebel
 * @since Oct 17, 2025
 */
public class GelfPayloadFactory {

    private static final String VERSION = "1.1";
    private static final String FACILITY = "gelf-java";
    private static final int MAX_SHORT_MESSAGE_LENGTH = 250;
    private static final BigDecimal TIME_DIVISOR = new BigDecimal(1000);
    private static final String LOCALHOSTNAME = getLocalHostName();
    private static final Gson GSON = new Gson();

	public static GelfPayload makeMessage(LogRecord record, String instanceName, Map<String, String> additonalFields) {

		try {
			Map<String, Object> payloadMap = new HashMap<>();
			String formattedMessage = getFormattedMessage(record);
			
			payloadMap.put("version", VERSION);
			payloadMap.put("facility", FACILITY);
			payloadMap.put("host", LOCALHOSTNAME);
			payloadMap.put("timestamp", getTimestamp(record));
			payloadMap.put("level", mapLevel(record.getLevel()));
			
			payloadMap.put("_logger", record.getLoggerName());
			payloadMap.put("_thread", Thread.currentThread().getName());
			if(Objects.nonNull(instanceName)) {
				payloadMap.put("_instanceName", instanceName);
			}
			
			String fullMessage = formattedMessage;
			String shortMessage = getShortMessage(formattedMessage);
			
            if (Objects.nonNull(record.getThrown())) {
            	fullMessage += "\n\r" + extractStacktrace(record.getThrown());
            }
				
            payloadMap.put("short_message", shortMessage);
			payloadMap.put("full_message", fullMessage);
			
			if(Objects.nonNull(additonalFields)) {
				for (Map.Entry<String, String> additionalField : additonalFields.entrySet()) {
					payloadMap.put("_" + additionalField.getKey(), additionalField.getValue());
				}
			}
			
			String payload = GSON.toJson(payloadMap);
			return new GelfPayload(payload);
		} catch (Exception exception) {
			throw new RuntimeException("Failed to build GELF Payload", exception);
		}
	}

    private static BigDecimal getTimestamp(LogRecord record) {
        return new BigDecimal(record.getMillis()).divide(TIME_DIVISOR);
    }
    
	private static String getShortMessage(String formattedMessage) {
		if (formattedMessage.length() > MAX_SHORT_MESSAGE_LENGTH) {
		    return formattedMessage.substring(0, MAX_SHORT_MESSAGE_LENGTH - 1);
		} else {
		    return formattedMessage;
		}
	}
	
	private static String getFormattedMessage(LogRecord record) {
		String message = record.getMessage();
        Object[] parameters = record.getParameters();
        if (Objects.isNull(message)) message = "";
        
        if (Objects.nonNull(parameters) && parameters.length > 0) {
        	//Using MessageFormat to format {0} {1}:
            message = MessageFormat.format(message, parameters);
            
            //Using MessageFormat to format {%s} {%d}:
            if (message.equals(record.getMessage())) {
                try {
                    message = String.format(message, parameters);
                } catch (Exception exception) {}
            }
        }
		return message;
	}
	
    private static int mapLevel(Level level) {
        if (level == Level.SEVERE) return 3;
        if (level == Level.WARNING) return 4;
        if (level == Level.INFO) return 6;
        if (level == Level.FINE || level == Level.FINER || level == Level.FINEST) return 7;
        return 6;
    }

    private static String extractStacktrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
    
}
