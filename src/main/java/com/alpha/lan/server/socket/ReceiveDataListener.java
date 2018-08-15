package com.alpha.lan.server.socket;

import com.alpha.lan.client.RequestType;

public interface ReceiveDataListener {
	void onStart(RequestType type);
	
	void onData(ReceiveData buf);
	
	void end();
	
	void onError(Exception err);
}
