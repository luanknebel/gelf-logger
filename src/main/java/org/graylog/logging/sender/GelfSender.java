package org.graylog.logging.sender;

import java.io.Closeable;

import org.graylog.logging.payload.GelfPayload;
import org.graylog.logging.payload.GelfPayloadResult;

/**
 * 
 * @author Luan Knebel
 * @date 13/10/2025
 */
public interface GelfSender extends Closeable{

	public GelfPayloadResult sendMessage(GelfPayload message);
	
}
