package com.alpha.lan.client;

import java.nio.ByteBuffer;

public class Request {
	private static byte id = 0;
	private byte type;
	private byte typeId;
	private static final byte[] MSG_END = "EOF!".getBytes();
	private ByteBuffer body;

	public Request(ByteBuffer content) {
		this(RequestType.TEXT, (byte) 0, content);
	}

	public Request(RequestType type, byte typeId, ByteBuffer content) {
		this.id = ++this.id < 0 ? 0 : this.id;
		this.type = type.value();
		this.typeId = typeId;
		this.body = createBody(content);
	}

	public ByteBuffer getBody() {
		return body;
	}

	private ByteBuffer createBody(ByteBuffer content) {
		ByteBuffer body = ByteBuffer.allocate(content.capacity() + 20);
		body.put(id);
		body.put(type);
		body.put(typeId);
		body.put(content);
		body.put(MSG_END);
		body.flip();
		return body;
	}
}
