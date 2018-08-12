package com.alpha.lan.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.alpha.lan.utils.Log;

public class PersistentConnectionClient<T, K> extends Client<T, K> {
	private static final String TAG = PersistentConnectionClient.class.getSimpleName();

	private Selector selector;
	private ChannelReader msgReader;
	private boolean listening = true;
	private boolean isStarted = false;
	private SocketChannel writeChannel;
	private SelectionKey writeKey;

	public PersistentConnectionClient(Dispatcher<T, K> dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public void stop() throws IOException {
		listening = false;
		isStarted = false;
		selector.wakeup();

		writeChannel.close();
		writeKey.cancel();
		if (selector.isOpen()) {
			selector.close();
		}

		writeChannel = null;
		writeKey = null;
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public boolean restart(String address, int port) throws IOException {
		try {
			stop();
			Thread.sleep(500);
			init(address, port);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void send(T content, OnResponseListener listener) throws IOException {
		if (selector == null) {
			throw new RuntimeException("Selector uninitialized error!");
		}

		try {
			selector.wakeup();
			Log.d(TAG, "send: send start");

			if (writeChannel.isConnectionPending()) {
				boolean result = writeChannel.finishConnect();
				Log.d(TAG, "connected: " + result);
			}
			Attachment<T> attachment = createAttachment(listener, content);
			writeKey.attach(attachment);
			boolean writeSuccess = mDispatcher.send(writeChannel, attachment);
			if (!writeSuccess) {
				Log.w(TAG, "write: a empty msg to server was intercepted!");
				throw new RuntimeException("couldn't send a empty msg!");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void init(String address, int port) throws IOException {
		selector = Selector.open();
		if (msgReader == null) {
			msgReader = new ChannelReader();
		}
		InetSocketAddress serverAddr = new InetSocketAddress(address, port);
		writeChannel = createSocketChannel(serverAddr);
		writeKey = writeChannel.register(selector, SelectionKey.OP_READ);
		listening = true;
		isStarted = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "poll Thread name:" + Thread.currentThread().getName());
				try {
					poll();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		Log.d(TAG, "init: success");
	}

	private void poll() throws Exception {
		while (listening) {
			if (selector == null) {
				throw new RuntimeException("Selector is null!");
			}

			Log.d(TAG, "poll: enter");
			synchronized (selector) {
				Log.d(TAG, "poll: select start");
				if (selector.select() == 0) {
					Log.d(TAG, "select result is 0, continue");
					Log.d(TAG, "end----------------------------------");
					continue;
				}

				Log.d(TAG, "selectedKeys size: " + selector.selectedKeys().size());
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					Log.d(TAG, "iterator SelectionKey");
					SelectionKey key = iterator.next();

					if (key.isReadable()) {
						Log.d(TAG, "current key is readable");
						iterator.remove();

						mDispatcher.receive((SocketChannel) key.channel(), getAttachment(key));
					}
				}
				Log.d(TAG, "end----------------------------------");
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
		Log.d(TAG, "poll: close callback listen");
	}
}
