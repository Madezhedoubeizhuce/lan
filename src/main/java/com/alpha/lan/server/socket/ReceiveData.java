package com.alpha.lan.server.socket;

import java.nio.ByteBuffer;

public class ReceiveData {
	private byte id;
	private byte type;
	private byte typeId;
	private ByteBuffer data;

	public ReceiveData(byte id, byte type, byte typeId) {
		super();
		this.id = id;
		this.type = type;
		this.typeId = typeId;
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getTypeId() {
		return typeId;
	}

	public void setTypeId(byte typeId) {
		this.typeId = typeId;
	}

	public ByteBuffer getData() {
		return data;
	}

	public void setData(ByteBuffer data) {
		this.data = data;
	}

}
