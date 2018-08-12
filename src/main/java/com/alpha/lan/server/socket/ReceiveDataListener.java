package com.alpha.lan.server.socket;

import java.io.ByteArrayOutputStream;

public interface ReceiveDataListener {
	void onData(ByteArrayOutputStream data);
	
	void end();
}
