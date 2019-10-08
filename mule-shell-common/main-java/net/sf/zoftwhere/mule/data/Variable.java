package net.sf.zoftwhere.mule.data;

import net.sf.zoftwhere.mule.function.PlaceHolder;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Variable.
 *
 * @param <E> data type
 */
public class Variable<E> implements PlaceHolder<E> {

	private E value;

	public Variable() {
		this.value = null;
	}

	public Variable(E value) {
		this.value = value;
	}

	public void set(E value) {
		this.value = value;
	}

	/**
	 * Retrieve variable value.
	 *
	 * @return Stored value.
	 * @throws NoSuchElementException if no value is present
	 */
	@Override
	public E get() {
		if (value == null) {
			throw new NoSuchElementException();
		}
		return value;
	}

	public Optional<E> optional() {
		return Optional.ofNullable(value);
	}

	@Override
	public boolean isPresent() {
		return value != null;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}
}
