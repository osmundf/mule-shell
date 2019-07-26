package net.sf.zoftwhere.mule.function;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlaceHolder<I> extends Consumer<I>, Supplier<I> {
}
