package dev.knebelhub.logging.sender;

import java.io.Closeable;

import dev.knebelhub.logging.payload.GelfPayload;
import dev.knebelhub.logging.payload.GelfPayloadResult;

/**
 * 
 * @author Luan Knebel
 * @since Oct 17, 2025
 */
public interface GelfMessageStrategy extends Closeable{

	public GelfPayloadResult sendMessage(GelfPayload message);
	
}
