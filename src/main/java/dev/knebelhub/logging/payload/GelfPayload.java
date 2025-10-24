package dev.knebelhub.logging.payload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author Luan Knebel
 * @since Oct 17, 2025
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
    	payload += '\0';
        byte[] messageBytes = getUTF8Bytes(payload);
        ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }
    
    public ByteBuffer[] getUDPPayload() {
        
    	byte[] messageBytes = gzipMessage(payload);
        int diagramsLength = messageBytes.length / MAXIMUM_CHUNK_SIZE;
        if (messageBytes.length % MAXIMUM_CHUNK_SIZE != 0) {
            diagramsLength++;
        }
        
        ByteBuffer[] datagrams = new ByteBuffer[diagramsLength];
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
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        	 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);){
            
            byte[] bytes = getUTF8Bytes(message);
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
    
    private byte[] getUTF8Bytes(String data) {
        try {
            return data.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("No UTF-8 support available.", e);
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
