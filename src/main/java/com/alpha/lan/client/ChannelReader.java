package com.alpha.lan.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import com.alpha.lan.server.socket.ReceiveData;
import com.alpha.lan.server.socket.ReceiveDataListener;
import com.alpha.lan.utils.Log;

public class ChannelReader {
	private static final String TAG = ChannelReader.class.getSimpleName();
	private static final String STREAM_END = "EOF!";
	private final int maxTryTime = 10;
	private static final byte[] MSG_END = "EOF!".getBytes();

	private ByteBuffer mBuffer;

	public ChannelReader() {
		mBuffer = ByteBuffer.allocate(1024);
	}

	public String read(SocketChannel channel) throws IOException {
		StringBuilder result = new StringBuilder();
		int tryTime = 0;
		boolean isSuccess = false;

		// readStream方法有可能没有读取到完整的数据就返回了, 因此这里要多试几次
		while (tryTime++ < maxTryTime) {
			ByteArrayOutputStream outputStream = readStream(channel);
			if (outputStream == null) {
				throw new RuntimeException("some error when read data from SocketChannel!");
			}

			result.append(new String(outputStream.toByteArray()).trim());

			if (result.toString().endsWith(STREAM_END)) {
				result.delete(result.length() - STREAM_END.length(), result.length());
				isSuccess = true;
				break;
			}
			Log.w(TAG, "receive: " + result.toString() + ", not the end of the data, continue read.");

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!isSuccess) {
			Log.w(TAG, "miss some data in this message.");
		}

		Log.d(TAG, "read from channel: " + result);

		return result.toString();
	}

	private ByteArrayOutputStream readStream(SocketChannel channel) throws IOException {
		ByteArrayOutputStream outputStream;
		try {
			int count = 0;
			outputStream = new ByteArrayOutputStream();

			// 一般情况下读取的长度为零时说明到了流末尾，也有可能读取长度为0，但并未到接收流的结尾
			while ((count = channel.read(mBuffer)) > 0) {
				mBuffer.flip();
				byte[] data = new byte[count];
				mBuffer.get(data, 0, count);
				outputStream.write(data, 0, data.length);
				mBuffer.clear();
			}
			Log.d(TAG, "read completed.");
		} finally {
			mBuffer.clear();
		}
		return outputStream;
	}

	private byte[] readBuffer(SocketChannel channel) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		int count = 0;
		int tryTime = 0;
		while ((count = channel.read(buf)) <= 0 && tryTime++ < 3) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (count < 0) {
			return new byte[0];
		}

		buf.flip();
		byte[] bufArr = new byte[count];
		buf.get(bufArr, 0, count);
		buf.clear();
		return bufArr;
	}

	private ByteBuffer createBuffer(byte[] buf, int offset, int length) {
		ByteBuffer buffer = ByteBuffer.allocate(buf.length);
		buffer.put(buf, offset, length);
		buffer.flip();
		return buffer;
	}

	public void read(SocketChannel channel, ReceiveDataListener listener) throws IOException {
		byte[] buf = readBuffer(channel);

		int protocolFieldSize = MSG_END.length + 3;
		if (buf.length >= protocolFieldSize) {
			byte id = buf[0];
			byte type = buf[1];
			byte typeId = buf[2];

			byte[] end = Arrays.copyOfRange(buf, buf.length - MSG_END.length, buf.length);

			int offset = 3;
			int writeLen = buf.length - 3;
			while (!Arrays.equals(end, MSG_END)) {
				listener.onStart(MessageType.valueOf(type));

				ReceiveData data = new ReceiveData(id, type, typeId);
				data.setData(createBuffer(buf, offset, writeLen));
				listener.onData(data);

				byte[] tmpBuf = readBuffer(channel);
				if (tmpBuf.length < MSG_END.length) {
					buf = Arrays.copyOf(buf, buf.length + tmpBuf.length);
					for (int i = buf.length - tmpBuf.length; i < buf.length; i++) {
						buf[i] = tmpBuf[i];
					}
				}

				end = Arrays.copyOfRange(buf, buf.length - MSG_END.length, buf.length);
				offset = 0;
				writeLen = buf.length;
			}

			// 说明一次就读完了所有数据
			if (offset == 3) {
				listener.onStart(MessageType.valueOf(type));
			}

			writeLen = writeLen - MSG_END.length;
			ReceiveData data = new ReceiveData(id, type, typeId);
			data.setData(createBuffer(buf, offset, writeLen));

			listener.onData(data);
			listener.end();
		} else {
			listener.onError(new Exception("receive msg length less than protocol field size!"));
		}
	}
}
