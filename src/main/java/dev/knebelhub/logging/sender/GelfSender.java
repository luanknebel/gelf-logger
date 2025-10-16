package dev.knebelhub.logging.sender;

import java.io.Closeable;

import dev.knebelhub.logging.payload.GelfPayload;
import dev.knebelhub.logging.payload.GelfPayloadResult;

/**
 * 
 * @author Luan Knebel
 * @date 13/10/2025
 */
public interface GelfSender extends Closeable{

	public GelfPayloadResult sendMessage(GelfPayload message);
	
}
