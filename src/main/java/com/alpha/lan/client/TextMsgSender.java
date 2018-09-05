package com.alpha.lan.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.alpha.lan.utils.Log;

public class TextMsgSender implements Sender<String, String> {
	private static final String TAG = TextMsgSender.class.getSimpleName();

	private ChannelReader msgReader;

	public TextMsgSender() {
		super();
		msgReader = new ChannelReader();
	}

	@Override
	public boolean send(SocketChannel channel, Attachment<String> attachment) throws IOException {
		String msg = attachment.getData();
		if (msg != null && !"".equals(msg)) {
			Message message = new Message(ByteBuffer.wrap(msg.getBytes()));
			channel.write(message.getBody());
			
//			msg = msg + "EOF!";
//			channel.write(ByteBuffer.wrap(msg.getBytes()));

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
