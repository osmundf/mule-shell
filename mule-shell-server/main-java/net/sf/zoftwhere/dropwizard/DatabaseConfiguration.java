package net.sf.zoftwhere.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public abstract class DatabaseConfiguration extends Configuration {

	@JsonProperty("database")
	public abstract DataSourceFactory getDataSourceFactory();

	@JsonProperty("database")
	public abstract void setDataSourceFactory(DataSourceFactory dataSourceFactory);
}
