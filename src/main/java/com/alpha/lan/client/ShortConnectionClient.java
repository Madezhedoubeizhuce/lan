package com.alpha.lan.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.alpha.lan.utils.Log;

public class ShortConnectionClient<T, K> extends Client<T, K> {
	private static final String TAG = ShortConnectionClient.class.getSimpleName();

	private Selector selector;
	private ChannelReader msgReader;
	private InetSocketAddress serverAddress;
	private boolean listening = true;
	private boolean isStarted = false;

	public ShortConnectionClient(Dispatcher<T, K> dispatcher) {
		super(dispatcher);
	}
	
	@Override
	// 重新启动客户端， 如果Address为空，则返回false，表示重启失败
	public boolean restart(String address, int port) throws IOException {
		try {
			stop();
			// 传入地址为空时不再启动Client
			if (address != null && !"".equals(address)) {
				Thread.sleep(100); // 确保上一次的回调监听关闭
				init(address, port);
				Log.d(TAG, "restart: success");
				return true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public void stop() throws IOException {
		isStarted = false;
		listening = false; // 关闭监听回调的线程

		selector.wakeup();
		if (selector.isOpen()) {
			selector.close();
		}

		serverAddress = null;

		Log.d(TAG, "stopClient");
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public void send(T content, OnResponseListener listener) throws IOException {
		if (serverAddress == null) {
			throw new RuntimeException("Client was closed");
		}

		if (selector == null) {
			throw new RuntimeException("Selector uninitialized error!");
		}
		Log.d(TAG, "ready send data to addr: " + serverAddress.getHostName() + ":" + serverAddress.getPort());

		SocketChannel channel = createSocketChannel(serverAddress);
		try {
			selector.wakeup();
			Log.d(TAG, "send: send start");

			Attachment<T> attachment = createAttachment(listener, content);
			channel.register(selector, SelectionKey.OP_READ, attachment);

			if (channel.isConnectionPending()) {
				boolean result = channel.finishConnect();
				Log.d(TAG, "connected: " + result);
			}

			boolean writeSuccess = mDispatcher.send(channel, attachment);
			if (!writeSuccess) {
				Log.w(TAG, "a empty msg to server was intercepted!");
				channel.close();
				throw new RuntimeException("couldn't send a empty msg!");
			}
		} catch (IOException e) {
			e.printStackTrace();
			channel.close();
			throw e;
		}
	}

	@Override
	public void init(String address, int port) throws IOException {
		selector = Selector.open();
		if (msgReader == null) {
			msgReader = new ChannelReader();
		}
		serverAddress = new InetSocketAddress(address, port);
		isStarted = true;
		listening = true;

		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "poll Thread name:" + Thread.currentThread().getName());
				poll();
			}
		}).start();
		Log.d(TAG, "init: success");
	}

	private void poll() {
		try {
			while (listening) {
				if (selector == null) {
					throw new RuntimeException("Selector is null!");
				}

				Log.d(TAG, "poll: enter");
				synchronized (selector) {
					Log.d(TAG, "poll: select start");
					selector.select();
					Log.d(TAG, "poll: select end");

					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();

						if (key.isReadable()) {
							Log.d(TAG, "current thread->" + Thread.currentThread().getName());

							iterator.remove();

							SocketChannel channel = (SocketChannel) key.channel();
							try {
								mDispatcher.receive(channel, getAttachment(key));
							} finally {
								channel.close();
							}
						}
					}
				}

				// 确保send函数wakeup时能第一时间拿到锁
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (selector.isOpen()) {
				selector.close();
			}
			Log.d(TAG, "close poll");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
