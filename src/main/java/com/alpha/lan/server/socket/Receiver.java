package com.alpha.lan.server.socket;

import java.nio.channels.SocketChannel;

public abstract class Receiver {
	protected OnRequestListener listener;

	public Receiver(OnRequestListener listener) {
		this.listener = listener;
	}

	public abstract void onReceive(ReceiveData data);

	public abstract void onComplete(SocketChannel channel);
	
	public abstract void onError(Exception err);
}
