package com.alpha.lan.client;

public enum MessageType {
	TEXT((byte) 0), FILE((byte) 1), HEARTBEAT((byte) 2);

	private byte value;

	MessageType(byte value) {
		this.value = value;
	}

	public byte value() {
		return this.value;
	}

	public static MessageType valueOf(byte value) {
		switch (value) {
		case 0:
			return TEXT;
		case 1:
			return FILE;
		case 2:
			return HEARTBEAT;
		default:
			return null;
		}
	}
}
