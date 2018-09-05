package com.alpha.lan.server.socket;

import java.nio.channels.SocketChannel;

public interface ReceiveListener {
	void onReceive(String msg, SocketChannel channel);
}
