package com.alpha.lan.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Sender<T, K> {
	boolean send(SocketChannel channel, Attachment<T> attachment) throws IOException;
	
	boolean receive(SocketChannel channel, Attachment<K> attachment);
}
