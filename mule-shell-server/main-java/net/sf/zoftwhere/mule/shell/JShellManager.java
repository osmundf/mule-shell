package net.sf.zoftwhere.mule.shell;

import jdk.jshell.JShell;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class JShellManager {

	private final Map<UUID, JShell> shellMap;
	private final Supplier<JShell> supplier;

	public JShellManager() {
		this.shellMap = new HashMap<>(1024);
		this.supplier = JShell::create;
	}

	public JShellManager(Map<UUID, JShell> shellMap) {
		this.shellMap = shellMap;
		this.supplier = JShell::create;
	}

	public JShellManager(Supplier<JShell> supplier) {
		this.shellMap = new HashMap<>(1024);
		this.supplier = supplier;
	}

	public JShellManager(Map<UUID, JShell> shellMap, Supplier<JShell> supplier) {
		this.shellMap = shellMap;
		this.supplier = supplier;
	}

	public JShell getJShell(UUID key) {
		return shellMap.get(key);
	}

	public Optional<Map.Entry<UUID, JShell>> newJShell(Supplier<UUID> generator) {
		final var key = generator.get();

		if (shellMap.containsKey(key)) {
			return Optional.empty();
		}

		final var shell = supplier.get();

		if (!putJShell(key, shell)) {
			return Optional.empty();
		}

		return Optional.of(new AbstractMap.SimpleEntry<>(key, shell));
	}

	public boolean removeJShell(String key) {
		if (key == null) {
			return false;
		}

		if (!shellMap.containsKey(key)) {
			return false;
		}

		return shellMap.remove(key) != null;
	}

	synchronized public boolean putJShell(UUID key, JShell shell) {
		if (key == null) {
			return false;
		}

		if (shell == null) {
			return false;
		}

		if (shellMap.containsKey(key)) {
			return false;
		}

		shellMap.put(key, shell);
		return true;
	}

	public int size() {
		return shellMap.size();
	}
}
