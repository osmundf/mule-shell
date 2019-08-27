package net.sf.zoftwhere.mule.data;

import net.sf.zoftwhere.mule.function.PlaceHolder;

import java.util.Optional;

public class Variable<E> implements PlaceHolder<E> {

	private E value;

	public Variable() {
	}

	public Variable(E value) {
		this.value = value;
	}

	public Optional<E> optional() {
		return Optional.ofNullable(value);
	}

	@Override
	public E get() {
		return value;
	}

	@Override
	public void accept(E value) {
		this.value = value;
	}
}
