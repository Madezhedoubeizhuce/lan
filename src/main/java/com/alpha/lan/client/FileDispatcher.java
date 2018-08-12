package com.alpha.lan.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import com.alpha.lan.utils.Log;

public class FileDispatcher implements Dispatcher<File, String> {
	private static final String TAG = FileDispatcher.class.getSimpleName();

	private ByteBuffer sendBuf = ByteBuffer.allocate(1024 * 10);

	@Override
	public boolean send(SocketChannel channel, Attachment<File> attachment) throws IOException {
		FileChannel fileChannel = null;
		FileInputStream inputStream = null;
		File file = attachment.getData();
		try {
			inputStream = new FileInputStream(file);
			fileChannel = inputStream.getChannel();
			int count = 0;
			while ((count = fileChannel.read(sendBuf)) != -1) {
				sendBuf.flip();
				int sendLen = channel.write(sendBuf);
				Log.d(TAG, "send: send length is: " + sendLen);
				// 服务端可能因为缓冲区满，导致发送失败，故需重新发送
				int tryTime = 0;
				while (sendLen == 0 && tryTime++ < 10) {
					Thread.sleep(10);
					sendLen = channel.write(sendBuf);
					Log.w(TAG, "send: try to send again");
				}
				sendBuf.clear();
			}
			channel.write(ByteBuffer.wrap("EOF!".getBytes()));
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public boolean receive(SocketChannel channel, Attachment<String> attachment) {
		return false;
	}

}
