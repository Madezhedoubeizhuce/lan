package com.alpha.lan.server.socket;

import java.nio.channels.SocketChannel;

import com.alpha.lan.utils.Log;

public class TextMsgReceiver extends Receiver {
	private static final String TAG = TextMsgReceiver.class.getSimpleName();
	private StringBuilder receiveMsg = new StringBuilder();

	public TextMsgReceiver(ReceiveListener listener) {
		super(listener);
	}

	@Override
	public void onReceive(ReceiveData data) {
		if (data == null || data.getData() == null) {
			Log.e(TAG, "onReceive(): empty msg");
			return;
		}

		String msg = new String(data.getData().array()).trim();
		receiveMsg.append(msg);

		Log.d(TAG, "receiveMsg is: " + msg);
	}

	@Override
	public void onComplete(SocketChannel channel) {
		Log.d(TAG, "onComplete(): receiveMsg data complete");
		
		listener.onReceive(receiveMsg.toString(), channel);
	}

	@Override
	public void onError(Exception err) {
		Log.e(TAG, "onError(): " + err.getMessage());
	}
}
