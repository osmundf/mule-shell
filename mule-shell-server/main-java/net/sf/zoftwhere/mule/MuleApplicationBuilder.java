package net.sf.zoftwhere.mule;

import java.util.function.Function;
import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class MuleApplicationBuilder<T extends MuleApplication> {

	public static <B extends MuleApplication> MuleApplicationBuilder<B> create(
		@Nonnull Function<MuleApplicationBuilder<B>, B> function)
	{
		return new MuleApplicationBuilder<>(function);
	}

	private final Function<MuleApplicationBuilder<T>, T> factory;
	@Getter
	@Setter
	private String realm = "realm";
	@Getter
	@Setter
	private int userCacheSize = 1;
	@Getter
	@Setter
	private int shellCacheSize = 1;

	private MuleApplicationBuilder(Function<MuleApplicationBuilder<T>, T> function) {
		this.factory = function;
	}

	public T build() {
		return factory.apply(this);
	}

	public void run(String... arguments) throws Exception {
		factory.apply(this).run(arguments);
	}
}
