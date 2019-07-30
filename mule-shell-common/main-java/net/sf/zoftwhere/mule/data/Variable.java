package net.sf.zoftwhere.mule.data;

import net.sf.zoftwhere.mule.function.PlaceHolder;

public class Variable<E> implements PlaceHolder<E> {

	private E internal;

	public Variable() {
	}

	@Override
	public E get() {
		return internal;
	}

	@Override
	public void accept(E securityContext) {
		internal = securityContext;
	}
}