package org.walkframework.mq.queue.redis;

import java.io.Serializable;
import java.util.UUID;

public class ValueWrapper<E> implements Serializable {
	private static final long serialVersionUID = -7498273707190119887L;
	@SuppressWarnings("unused")
	private final String uuid;
	private final E value;

	public ValueWrapper(E value) {
		this.uuid = UUID.randomUUID().toString();
		this.value = value;
	}

	public E get() {
		return this.value;
	}

}
