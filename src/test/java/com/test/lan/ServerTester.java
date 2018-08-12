package com.test.lan;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.alpha.lan.server.jmdns.JmDnsServiceRegister;
import com.alpha.lan.server.socket.Server;
import com.alpha.lan.utils.Log;

public class ServerTester {
	private static final String TAG = "ServerTester";
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			int port = server.createPort();
			server.init(port, new Server.NioCallback() {
				
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
			JmDnsServiceRegister.getInstance().initJmDns();
			JmDnsServiceRegister.getInstance().serviceRegist(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
