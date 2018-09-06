package com.alpha.lan.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import static com.alpha.lan.client.MessageType.*;

import com.alpha.lan.utils.Log;

public class HeartbeatMsgSender implements Sender<String, String> {
	private static final String TAG = HeartbeatMsgSender.class.getSimpleName();

	private ChannelReader msgReader;

	public HeartbeatMsgSender() {
		super();
		msgReader = new ChannelReader();
	}

	@Override
	public boolean send(SocketChannel channel, Attachment<String> attachment) throws IOException {
		String msg = attachment.getData();
		if (msg != null && !"".equals(msg)) {
			Message message = new Message(HEARTBEAT, HEARTBEAT.value(), ByteBuffer.wrap(msg.getBytes()));
			channel.write(message.getBody());

			Log.d(TAG, "---------------------------------------------------------");
			Log.i(TAG, "write: \"" + msg + "\" to server");
			return true;
		}
		return false;
	}

	@Override
	public boolean receive(SocketChannel channel, Attachment<String> attachment) {
		if (channel == null) {
			throw new RuntimeException("socket channel is null");
		}
		Log.d(TAG, "receive: attachment: " + attachment.getData());

		OnResponseListener listener = attachment.getListener();
		String response;
		try {
			response = msgReader.read(channel);
			if (listener != null) {
				Log.i(TAG, "server onResponse:" + response);
				listener.onResponse(response);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
