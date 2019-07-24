package net.sf.zoftwhere.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class H2TestNamingStrategy extends SnakeCaseNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return uppercase(identifier);
//		return lowercase(identifier);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return uppercase(identifier);
//		return lowercase(identifier);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return uppercase(identifier);
//		return lowercase(identifier);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return uppercase(identifier);
//		return lowercase(identifier);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnv) {
//		return uppercase(identifier);
		return lowercase(identifier);
	}

	private static Identifier lowercase(Identifier identifier) {
		if (identifier == null) {
			return null;
		}

		final var name = identifier.getText();

		if (name.toLowerCase().contains("info")) {
			System.out.println(name);
		}

		return new Identifier(identifier.getText().toUpperCase(), true);
	}

	private static Identifier uppercase(Identifier identifier) {
		if (identifier == null) {
			return null;
		}

		final var name = identifier.getText();

		if (name.toLowerCase().contains("info")) {
			System.out.println(name);
		}

		return new Identifier(identifier.getText().toUpperCase(), true);
	}
}