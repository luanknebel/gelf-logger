package org.graylog.logging.payload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author Luan Knebel
 * @date 13/10/2025
 */
public class GelfPayload {

	private static final int MAXIMUM_CHUNK_SIZE = 1420;
    private static final byte[] GELF_CHUNKED_ID = new byte[]{0x1e, 0x0f};

	private String payload;

	public GelfPayload(String payload) {
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}
	
    public ByteBuffer getTCPPayload() {
        byte[] messageBytes;
        try {
            payload += '\0';
            messageBytes = payload.getBytes("UTF-8");
        } catch (Exception exception) {
            throw new RuntimeException("No UTF-8 support available.", exception);
        }
        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }
    
    public ByteBuffer[] getUDPPayload() {
        byte[] messageBytes = gzipMessage(payload);
        int diagrams_length = messageBytes.length / MAXIMUM_CHUNK_SIZE;
        if (messageBytes.length % MAXIMUM_CHUNK_SIZE != 0) {
            diagrams_length++;
        }
        ByteBuffer[] datagrams = new ByteBuffer[diagrams_length];
        if (messageBytes.length > MAXIMUM_CHUNK_SIZE) {
            sliceDatagrams(messageBytes, datagrams);
        } else {
            datagrams[0] = ByteBuffer.allocate(messageBytes.length);
            datagrams[0].put(messageBytes);
            datagrams[0].flip();
        }
        return datagrams;
    }
    
    private byte[] gzipMessage(String message) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream stream = new GZIPOutputStream(bos);
            byte[] bytes;
            try {
                bytes = message.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("No UTF-8 support available.", e);
            }
            stream.write(bytes);
            stream.finish();
            stream.close();
            byte[] zipped = bos.toByteArray();
            bos.close();
            return zipped;
        } catch (IOException e) {
            return null;
        }
    }
    
    private void sliceDatagrams(byte[] messageBytes, ByteBuffer[] datagrams) {
    	
        int messageLength = messageBytes.length;
        byte[] messageId = new byte[8];
        new Random().nextBytes(messageId);

        int num = datagrams.length;
        for (int idx = 0; idx < num; idx++) {
            byte[] header = concatByteArray(GELF_CHUNKED_ID, concatByteArray(messageId, new byte[]{(byte) idx, (byte) num}));
            int from = idx * MAXIMUM_CHUNK_SIZE;
            int to = from + MAXIMUM_CHUNK_SIZE;
            if (to >= messageLength) {
                to = messageLength;
            }

            byte[] range = new byte[to - from];
            System.arraycopy(messageBytes, from, range, 0, range.length);
            
            byte[] datagram = concatByteArray(header, range);
            datagrams[idx] = ByteBuffer.allocate(datagram.length);
            datagrams[idx].put(datagram);
            datagrams[idx].flip();
        }
    }
    
    byte[] concatByteArray(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
}
