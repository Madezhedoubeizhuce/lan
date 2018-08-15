package com.alpha.lan.server.socket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.alpha.lan.client.ChannelReader;
import com.alpha.lan.client.RequestType;
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

	public void dispatch(SocketChannel channel, OnRequestListener listener) {
		try {
			channelReader.read(channel, new ReceiveDataListener() {
				Receiver receiver;

				@Override
				public void onStart(RequestType type) {
					switch (type) {
					case FILE:

						break;
					case TEXT:
						receiver = new TextMsgReceiver(listener);
						break;
					default:
						Log.d(TAG, "unknown type");
					}
				}

				@Override
				public void onData(ReceiveData buf) {
					Log.d(TAG, "on data");
					receiver.onReceive(buf);
				}

				@Override
				public void end() {
					Log.d(TAG, "end");
					receiver.onComplete(channel);
				}

				@Override
				public void onError(Exception err) {
					Log.d(TAG, "on error");
					receiver.onError(err);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
