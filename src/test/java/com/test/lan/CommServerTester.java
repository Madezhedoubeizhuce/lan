package com.test.lan;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.alpha.lan.server.socket.OnRequestListener;
import com.alpha.lan.server.socket.Server;
import com.alpha.lan.utils.Log;

public class CommServerTester {
	private static final String TAG = "CommServerTester->";
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.init(6969, new OnRequestListener() {
				
				@Override
				public void onReceive(String msg, SocketChannel channel) {
					// TODO Auto-generated method stub
					Log.d(TAG, msg);
					try {
						server.send("receive: " + msg, channel);
						channel.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
