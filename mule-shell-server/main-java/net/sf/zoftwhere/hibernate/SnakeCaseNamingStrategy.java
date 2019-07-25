package net.sf.zoftwhere.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCaseNamingStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
		return convertToSnakeCase(identifier);
	}

	@Override
	public Identifier toPhysicalColumnName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
		return convertToSnakeCase(identifier);
	}

	@Override
	public Identifier toPhysicalSchemaName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
		return convertToSnakeCase(identifier);
	}

	@Override
	public Identifier toPhysicalSequenceName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
		return convertToSnakeCase(identifier);
	}

	@Override
	public Identifier toPhysicalTableName(final Identifier identifier, final JdbcEnvironment jdbcEnv) {
		return convertToSnakeCase(identifier);
	}

	@SuppressWarnings("WeakerAccess")
	protected Identifier convertToSnakeCase(final Identifier identifier) {
		if (identifier == null) {
			return null;
		}

		return Identifier.toIdentifier(snakeCase(identifier.getText()), identifier.isQuoted());
	}

	@SuppressWarnings("WeakerAccess")
	protected String snakeCase(String input) {
		return input.replaceAll("([a-z])([A-Z])", "$1_$2")
				.replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
				.toLowerCase();
	}
}
