package net.sf.zoftwhere.mule;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.google.inject.Provider;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sf.zoftwhere.hibernate.TransactionalSession;
import net.sf.zoftwhere.mule.data.Variable;
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

public class MuleShellDemo extends MuleApplication {

	public static void main(String[] arguments) throws Exception {
		long time = -System.nanoTime();
		MuleApplicationBuilder.create(MuleShellDemo::new)
			.realm("mule-shell-demo")
			.userCacheSize(10)
			.shellCacheSize(10)
			.run(arguments);
		time += System.nanoTime();
		logger.info("Started: " + ((time / 1_000) / 1e3) + " ms");
		new ServerSocket().bind(new InetSocketAddress("localhost", 0));
	}

	private static final Logger logger = LoggerFactory.getLogger(MuleShellDemo.class);
	private final Variable<ConfigurationSourceProvider> sourceProviderVariable = new Variable<>();

	public MuleShellDemo(MuleApplicationBuilder<MuleShellDemo> builder) {
		super(builder);
	}

	@Override
	public String getName() {
		return "mule-shell-demo";
	}

	@Override
	public void run(String... arguments) throws Exception {
		if (arguments != null && arguments.length > 0) {
			super.run(arguments);
			return;
		}

		sourceProviderVariable.set(new ResourceConfigurationSourceProvider());
		super.run("server", "demo.yaml");
	}

	@Override
	public void initialize(Bootstrap<MuleConfiguration> bootstrap) {
		if (sourceProviderVariable.optional().isPresent()) {
			bootstrap.setConfigurationSourceProvider(sourceProviderVariable.get());
		}
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
		final var usernameArray = new String[] {"guest"};
		for (var username : usernameArray) {
			if (accountLocator.getByUsername(username).isPresent()) {
				continue;
			}

			final AccountSigner signer;
			try {
				signer = newAccountSigner();
			}
			catch (NoSuchAlgorithmException e) {
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
}
