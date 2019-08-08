package net.sf.zoftwhere.mule.server;

import com.google.common.cache.Cache;
import jdk.jshell.JShell;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;

import java.util.Optional;
import java.util.UUID;

public class JShellManager {

	private final Cache<UUID, JShell> shellCache;

	private final ShellSessionLocator shellSessionLocator;

	public JShellManager(Cache<UUID, JShell> shellCache, ShellSessionLocator shellSessionLocator) {
		this.shellCache = shellCache;
		this.shellSessionLocator = shellSessionLocator;
	}

	public Optional<JShell> getJShell(UUID id, Account account) {
		if (id == null || account == null) {
			return Optional.empty();
		}

		// final var shellSession = shellSessionLocator.getById(id).orElse(null);
		final var shellSession = shellSessionLocator.getForIdAndAccount(id, account).orElse(null);

		if (shellSession == null) {
			return Optional.empty();
		}

		final var shell = shellCache.getIfPresent(id);

		if (shell != null) {
			return Optional.of(shell);
		}

		// Load shell from database.
		final var loaded = JShell.builder().remoteVMOptions("-Xmx64m").build();

		// TODO: Run expressions for saved shell session.

		// Place in active cache.
		shellCache.put(id, loaded);

		return Optional.of(loaded);
	}
}
