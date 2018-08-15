package com.alpha.lan.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.alpha.lan.utils.Log;

public abstract class Client<T, K> {
	private static final String TAG = Client.class.getSimpleName();
	protected Sender<T, K> mSender;

	public Client(Sender<T, K> sender) {
		mSender = sender;
	}

	public abstract void init(String address, int port) throws IOException;

	public abstract void send(T content, OnResponseListener listener) throws IOException;

	public abstract void stop() throws IOException;

	public abstract boolean restart(String address, int port) throws IOException;

	public abstract boolean isStarted();

	protected Attachment<K> getAttachment(SelectionKey key) {
		Object attachment = key.attachment();

		if (attachment == null) {
			throw new RuntimeException("getAttachment: attachment is null");
		}
		Log.d(TAG, "getAttachment: get attachment success");

		return (Attachment<K>) attachment;
	}

	protected Attachment<T> createAttachment(OnResponseListener callback, T data) {
		Attachment<T> attachment = new Attachment<T>();
		attachment.setListener(callback);
		attachment.setData(data);

		return attachment;
	}

	protected SocketChannel createSocketChannel(InetSocketAddress serverAddress) throws IOException {
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(serverAddress);

		return channel;
	}
}
