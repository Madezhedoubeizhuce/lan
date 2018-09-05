package com.alpha.lan.server.socket;

import com.alpha.lan.client.MessageType;

public interface ReceiveDataListener {
	void onStart(MessageType type);
	
	void onData(ReceiveData buf);
	
	void end();
	
	void onError(Exception err);
}
