package net.sf.zoftwhere.mule.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class EmptyInterface {

	@SuppressWarnings("unchecked")
	public static <T> T create(ClassLoader classLoader, Class<?>[] interfaces, Throwable throwable) {
		TypedInvocationHandler<T> handler = (proxy, method, args) -> {
			throw throwable;
		};

		return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
	}

	private interface TypedInvocationHandler<T> extends InvocationHandler {
		@Override
		T invoke(Object proxy, Method method, Object[] args) throws Throwable;
	}
}
