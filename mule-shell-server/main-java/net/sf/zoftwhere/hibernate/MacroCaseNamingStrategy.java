package net.sf.zoftwhere.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class MacroCaseNamingStrategy extends SnakeCaseNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return convertToMacroCase(identifier);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return convertToMacroCase(identifier);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return convertToMacroCase(identifier);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return convertToMacroCase(identifier);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnv) {
		return convertToMacroCase(identifier);
	}

	@SuppressWarnings("WeakerAccess")
	protected Identifier convertToMacroCase(final Identifier identifier) {
		if (identifier == null) {
			return null;
		}

		return Identifier.toIdentifier(macroCase(identifier.getText()), identifier.isQuoted());
	}

	@SuppressWarnings("WeakerAccess")
	protected String macroCase(String input) {
		return snakeCase(input).toUpperCase();
	}
}
