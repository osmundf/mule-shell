package net.sf.zoftwhere.mule.shell;

import com.google.common.cache.Cache;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.ShellSessionLocator;

import java.util.Optional;
import java.util.UUID;

public class MuleShellManager {

	private final Cache<UUID, MuleShell> shellCache;

	private final ShellSessionLocator shellSessionLocator;

	public MuleShellManager(Cache<UUID, MuleShell> shellCache, ShellSessionLocator shellSessionLocator) {
		this.shellCache = shellCache;
		this.shellSessionLocator = shellSessionLocator;
	}

	public Optional<MuleShell> getMuleShell(UUID id, Account account) {
		if (id == null || account == null) {
			return Optional.empty();
		}

		// TODO: Extend to use session privileges (owner, viewer, visitor)
		final var shellSession = shellSessionLocator.getForIdAndAccount(id, account).orElse(null);

		if (shellSession == null) {
			return Optional.empty();
		}

		final var shell = shellCache.getIfPresent(id);

		if (shell != null) {
			return Optional.of(shell);
		}

		// Load shell from database.
		final var loaded = new MuleShell(builder -> builder.remoteVMOptions("-Xmx64m"));

		// TODO: Run expressions for saved shell session.

		// Place in active cache.
		shellCache.put(id, loaded);

		return Optional.of(loaded);
	}
}
