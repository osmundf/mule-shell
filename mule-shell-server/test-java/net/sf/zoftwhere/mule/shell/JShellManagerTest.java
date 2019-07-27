package net.sf.zoftwhere.mule.shell;

import jdk.jshell.JShell;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

class JShellManagerTest {

	private final Consumer<JShell> shutdownListener = jShell -> {
		String id = jShell.toString();
		System.out.println("Shutdown: " + id);
	};

	@Disabled
	@Test
	void newJShell() {
		// Ensure that this is Thread safe.
		final var manager = new JShellManager(JShell::create);

//		final var single = JShell.create();
//		final var manager = new JShellManager(() -> single);

		final var newRunnable = new Function<String, Runnable>() {
			@Override
			public Runnable apply(String s) {
				return new Runnable() {
					final String id = s;
					UUIDBuffer buffer = new UUIDBuffer(new Random(0));

					@Override
					public void run() {
						int i = 0;
						while (i < 10) {
							try {
								final var option = manager.newJShell(buffer);
								if (option.isEmpty()) {
									continue;
								}

								final var uuid = option.get().getKey();
								final var shell = option.get().getValue();

								shell.onShutdown(shutdownListener);
								shell.close();
								++i;
								buffer = new UUIDBuffer(new Random(0));
								System.out.println(id + ": " + uuid);
								try {
									Thread.sleep(i * 100);
								} catch (InterruptedException ex) {
									ex.printStackTrace();
								}
							} catch (RuntimeException e) {
//								System.out.println(id + ": xxx");
								// e.getMessage();
							}
						}
					}
				};
			}
		};

		final var thread1 = new Thread(newRunnable.apply("1"));
		final var thread2 = new Thread(newRunnable.apply("2"));

		thread1.start();
		thread2.start();

		while (true) {
			if (!thread1.isAlive()) {
				if (!thread2.isAlive()) {
					break;
				}
			}

			if (!thread2.isAlive()) {
				continue;
			}

			try {
				Thread.sleep(10);
			} catch (IllegalStateException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println(manager.size());
	}

	@Disabled
	@Test
	public void storeEval() {
		JShell shell = JShell.create();
		shell.onShutdown(shutdownListener);
	}
}