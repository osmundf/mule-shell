package net.sf.zoftwhere.mule;

import com.google.inject.Provider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sf.zoftwhere.hibernate.TransactionalSession;
import net.sf.zoftwhere.mule.jpa.Account;
import net.sf.zoftwhere.mule.jpa.AccountLocator;
import net.sf.zoftwhere.mule.jpa.AccountRole;
import net.sf.zoftwhere.mule.jpa.Role;
import net.sf.zoftwhere.mule.jpa.RoleLocator;
import net.sf.zoftwhere.mule.jpa.Setting;
import net.sf.zoftwhere.mule.model.RoleModel;
import net.sf.zoftwhere.mule.security.AccountSigner;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MuleShellDemo extends MuleApplication {

	private static final Logger logger = LoggerFactory.getLogger(MuleShellDemo.class);

	public static void main(String[] args) throws Exception {
		long time = -System.nanoTime();
		final var arguments = args != null && args.length > 0 ? args : new String[]{"server", findDemoConfiguration()};
		new MuleShellDemo("mule-shell-demo").run(arguments);
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
	}

	public MuleShellDemo(String realm) {
		super(realm);
	}

	@Override
	public String getName() {
		return "mule-shell-demo";
	}

	@Override
	public void initialize(Bootstrap<MuleConfiguration> bootstrap) {
		super.initialize(bootstrap);
	}

	@Override
	public void run(MuleConfiguration configuration, Environment environment) {
		super.run(configuration, environment);
	}

	@Override
	protected void setupDatabaseData(SessionFactory sessionFactory) {
		super.setupDatabaseData(sessionFactory);

		final Provider<Session> sessionProvider = sessionFactory::openSession;

		TransactionalSession.wrapSession(sessionProvider, session -> {
			session.beginTransaction();
			session.save(new Setting("mule-shell-jwt-issuer", "mule-shell-demo"));
			session.getTransaction().commit();

			session.beginTransaction();
			session.save(new Setting("mule-shell-jwt-hash-key", UUID.randomUUID().toString()));
			session.getTransaction().commit();
		});

		final var roleLocator = new RoleLocator(sessionFactory::openSession);
		final var roleModels = RoleModel.values();
		for (var roleModel : roleModels) {
			if (roleLocator.getByKey(roleModel).isPresent()) {
				throw new RuntimeException("Role " + roleModel.name() + " should not exist in demo database already.");
			}

			TransactionalSession.wrapSession(sessionProvider, session -> {
				final var key = Role.getKey(roleModel);
				final var name = roleModel.name();
				final var value = name.toLowerCase();
				final var priority = 0;
				Role role = new Role(key, name, value, priority);

				session.beginTransaction();
				session.save(role);
				session.getTransaction().commit();
			});
		}

		final var accountLocator = new AccountLocator(sessionProvider);
		final var usernameArray = new String[]{"guest"};
		for (var username : usernameArray) {
			if (accountLocator.getByUsername(username).isPresent()) {
				continue;
			}

			final AccountSigner signer;
			try {
				signer = newAccountSigner();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			TransactionalSession.wrapSession(sessionProvider, session -> {
				final var email = String.format("%s-demo@mule-shell.test.test", username);
				final var data = "shell-demo".getBytes(StandardCharsets.UTF_8);
				final var account = new Account(username, email).updateHash(signer, data);

				session.beginTransaction();
				session.save(account);
				session.getTransaction().commit();

				final var role = roleLocator.getByKey(RoleModel.CLIENT).orElseThrow();
				final var accountRole = new AccountRole(account, role, role.getValue());

				session.beginTransaction();
				session.save(accountRole);
				session.getTransaction().commit();
			});
		}
	}

	private static String findDemoConfiguration() {
		final var files = new String[]{"demo.yaml", "mule-shell-universe/demo.yaml", "../demo.yaml"};

		for (var name : files) {
			File file = new File(name);
			if (file.exists() && file.isFile()) {
				logger.info("Running configuration: " + name);
				return name;
			}
		}

		return "demo.yaml";
	}
}
