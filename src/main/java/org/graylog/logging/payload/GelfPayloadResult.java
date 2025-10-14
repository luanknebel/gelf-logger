package org.graylog.logging.payload;

import java.util.Objects;

/**
 * 
 * @author Luan Knebel
 * @date 13/10/2025
 */
public class GelfPayloadResult {

	public static final GelfPayloadResult SUCCESS = new GelfPayloadResult(-1);
    public static final GelfPayloadResult SHUTTING_DOWN = new GelfPayloadResult(-2, new Exception("Shutting down"));

	private int code;
	private Exception exception;

	public GelfPayloadResult(int code) {
		this.code = code;
	}

	public GelfPayloadResult(int code, Exception exception) {
		this.code = code;
		this.exception = exception;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public Exception getException() {
		return exception;
	}
	
	public String getMessage() {
		if(Objects.isNull(exception)) return "";
		
		String message = exception.getMessage();
		Throwable cause = exception.getCause();
		if (Objects.isNull(message) && Objects.nonNull(cause)) {
			message = cause.getMessage();
		}
		if (Objects.isNull(message)) {
			message = exception.toString();
		}
		return message;
	}

	@Override
	public boolean equals(Object object) {
		if(object == this) return true;
		if(object instanceof GelfPayloadResult) {
			GelfPayloadResult other = (GelfPayloadResult)object;
			return other.getCode() == this.getCode();
		}
		return false;
	}

	@Override
	public String toString() {
		return "GelfPayloadResult{code=" + code + ", exception=" + exception + "}";
	}

	@Override
	public int hashCode() {
		return code;
	}

}
