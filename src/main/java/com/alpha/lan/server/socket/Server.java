package com.alpha.lan.server.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.alpha.lan.client.ChannelReader;
import com.alpha.lan.utils.Log;

public class Server {
	public static final String TAG = Server.class.getSimpleName();
	private static final int DEFAULT_PORT = 4000;
	private static final String STREAM_END = "EOF!";

	private Selector selector;
	private ChannelReader channelReader;
	private boolean isServerRun = true;

	public static int createPort() {
		int port = DEFAULT_PORT;
		try {
			ServerSocket sock = new ServerSocket(0);
			port = sock.getLocalPort();
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return port;
	}

	public void init(int port, ReceiveListener listener) throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));

		selector = Selector.open();

		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		channelReader = new ChannelReader();
		poll(listener);
		Log.d(TAG, "initServer: init success");
	}

	public void send(String msg, final SocketChannel channel) {
		if (selector == null) {
			throw new ExceptionInInitializerError("Selector uninitialized error!");
		}
		if (channel == null) {
			Log.w(TAG, "sendFile: channel is null");
			return;
		}

		try {
			msg = msg + STREAM_END;
			channel.write(ByteBuffer.wrap(msg.getBytes()));
			Log.d(TAG, "---------------------------------------------------------");
			Log.i(TAG, "response: \"" + msg + "\" to client");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		isServerRun = false;
	}

	private void poll(final ReceiveListener listener) throws IOException {
		Log.d(TAG, "NioServer start succeeded");

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (selector == null) {
					throw new RuntimeException("Selector is null!");
				}

				while (isServerRun) {
					try {
						Log.d(TAG, "checkEvent: wait a event");
						selector.select();
						Log.d(TAG, "checkEvent: reveive a event");

						Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
						while (iterator.hasNext()) {
							SelectionKey key = iterator.next();
							iterator.remove();

							processSelectionKey(key, listener);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (CancelledKeyException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void processSelectionKey(SelectionKey key, ReceiveListener listener) throws IOException {
		Log.d(TAG, "processSelectionKey");
		if (key.isAcceptable()) {
			Log.d(TAG, "processSelectionKey: acceptable");
			ServerSocketChannel server = (ServerSocketChannel) key.channel();

			SocketChannel channel = server.accept();

			if (channel == null) {
				Log.w(TAG, "processSelectionKey: accept channel is null");
				return;
			}

			try {
				channel.configureBlocking(false);
				channel.register(selector, SelectionKey.OP_READ);
			} catch (IOException e) {
				e.printStackTrace();
				channel.close();
			}

		} else if (key.isReadable()) {
			Log.d(TAG, "processSelectionKey: readable");

			SocketChannel channel = (SocketChannel) key.channel();

			if (channel == null) {
				Log.w(TAG, "processSelectionKey: channel is null");
				return;
			}
			
			Dispatcher.getInstance().dispatch(channel, listener);//

//			try {
//				String receiveMsg = channelReader.read(channel);
//				Log.i(TAG, "processSelectionKey: client msg before parse -> " + receiveMsg);
//				Log.i(TAG, "processSelectionKey: client msg is -> " + receiveMsg);
//
//				if (receiveMsg == null || "".equals(receiveMsg)) {
//					channel.close();
//					Log.w(TAG, "processSelectionKey: client sendFile  empty msg");
//				} else if (receiveMsg.startsWith("heartbeat msg")) {
////                    HeartbeatChecker.getInstance().onReceive();
//					channel.write(ByteBuffer.wrap((receiveMsg + STREAM_END).getBytes()));
//				} else {
//					listener.onReceive(receiveMsg, channel);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//				channel.close();
//			}
		}
	}
}
