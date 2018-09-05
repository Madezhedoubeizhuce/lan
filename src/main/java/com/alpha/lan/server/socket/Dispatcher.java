package com.alpha.lan.server.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.alpha.lan.client.ChannelReader;
import com.alpha.lan.client.MessageType;
import com.alpha.lan.utils.Log;

public class Dispatcher {
	private static final String TAG = Dispatcher.class.getSimpleName();

	private static class SingleHolder {
		private static final Dispatcher INSTANCE = new Dispatcher();
	}

	private ChannelReader channelReader;

	private Dispatcher() {
		channelReader = new ChannelReader();
	}

	public static final Dispatcher getInstance() {
		return SingleHolder.INSTANCE;
	}

	public void dispatch(SocketChannel channel, ReceiveListener listener) {
		try {
			channelReader.read(channel, new ReceiveDataListener() {
				Receiver receiver;

				@Override
				public void onStart(MessageType type) {
					switch (type) {
					case FILE:

						break;
					case TEXT:
						receiver = new TextMsgReceiver(listener);
						break;
					case HEARTBEAT:
						receiver = new HeartbeatMsgReceiver(new ReceiveListener() {

							@Override
							public void onReceive(String msg, SocketChannel channel) {
								if (channel == null) {
									Log.w(TAG, "sendFile: channel is null");
									return;
								}

								try {
									msg = msg + "EOF!";
									channel.write(ByteBuffer.wrap(msg.getBytes()));
									Log.d(TAG, "---------------------------------------------------------");
									Log.i(TAG, "response: \"" + msg + "\" to client");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						break;
					default:
						Log.d(TAG, "unknown type");
					}
				}

				@Override
				public void onData(ReceiveData buf) {
					Log.d(TAG, "on data");
					if (receiver != null) {
						receiver.onReceive(buf);
					}
				}

				@Override
				public void end() {
					Log.d(TAG, "end");
					if (receiver != null) {
						receiver.onComplete(channel);
					}
				}

				@Override
				public void onError(Exception err) {
					Log.d(TAG, "on error");
					if (receiver != null) {
						receiver.onError(err);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
