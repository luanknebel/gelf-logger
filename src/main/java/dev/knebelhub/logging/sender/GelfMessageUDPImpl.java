package dev.knebelhub.logging.sender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.Objects;
import java.util.logging.ErrorManager;

import dev.knebelhub.logging.payload.GelfPayload;
import dev.knebelhub.logging.payload.GelfPayloadResult;

/**
 * 
 * @autor Luan Knebel
 * @since Oct 17, 2025
 */
public class GelfMessageUDPImpl implements GelfMessageStrategy {

	private String host;
	private int port;
	private DatagramChannel channel;
	private Date lastChannelRefresh = null;

	private static final int MAX_RETRIES = 5;
	private static final int REFRESH_CHANNEL_SECONDS = 300;

	public GelfMessageUDPImpl(String host, int port) {
		this.host = host;
		this.port = port;
		setChannel(initiateChannel());
	}

	private DatagramChannel initiateChannel() {
		try {
			DatagramChannel resultingChannel = DatagramChannel.open();
			resultingChannel.socket().bind(new InetSocketAddress(0));
			resultingChannel.connect(new InetSocketAddress(this.host, this.port));
			resultingChannel.configureBlocking(false);
			return resultingChannel;
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public GelfPayloadResult sendMessage(GelfPayload message) {
		return sendDatagrams(message.getUDPPayload());
	}

	private GelfPayloadResult sendDatagrams(ByteBuffer[] bytesList) {

		int tries = 0;
		Exception lastException = null;
		do {
			try {
				if (isChannelOld() ) {
					getChannel().close();
				}
				if (!getChannel().isOpen()) {
					setChannel(initiateChannel());
				}
				for (ByteBuffer buffer : bytesList) {
					getChannel().write(buffer);
				}
				return GelfPayloadResult.SUCCESS;
			} catch (Exception exception) {
				tries++;
				lastException = exception;
			}
		} while (tries <= MAX_RETRIES);
		
		return new GelfPayloadResult(ErrorManager.WRITE_FAILURE, lastException);
	}


    public DatagramChannel getChannel() {
        return channel;
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
        this.lastChannelRefresh = new Date();
    }
    
    private boolean isChannelOld() {
    	if (this.lastChannelRefresh == null)
    		return true;
    	
    	Date now = new Date();
    	if ((now.getTime() - REFRESH_CHANNEL_SECONDS * 1000) > this.lastChannelRefresh.getTime())
    		return true;
    	
    	return false;
    }

	@Override
	public void close() throws IOException {
		if(Objects.nonNull(getChannel()) && getChannel().isOpen()) {
			getChannel().close();
		}
	}
}
