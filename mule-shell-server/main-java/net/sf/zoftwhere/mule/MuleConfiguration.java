package net.sf.zoftwhere.mule;

import java.util.Collections;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import net.sf.zoftwhere.dropwizard.DatabaseConfiguration;
import net.sf.zoftwhere.dropwizard.MuleInfo;
import net.sf.zoftwhere.dropwizard.ViewAssetPath;

public class MuleConfiguration extends DatabaseConfiguration {

	@Valid
	@NotNull
	private DataSourceFactory database = new DataSourceFactory();

	@NotNull
	private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

	@NotNull
	private ViewAssetPath viewAssetPath = new ViewAssetPath();

	@NotNull
	private MuleInfo info = new MuleInfo();

	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}

	@JsonProperty("database")
	public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
		this.database = dataSourceFactory;
	}

	@JsonProperty("viewRendererConfiguration")
	public Map<String, Map<String, String>> getViewRendererConfiguration() {
		return viewRendererConfiguration;
	}

	@JsonProperty("viewRendererConfiguration")
	public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
		this.viewRendererConfiguration = viewRendererConfiguration;
	}

	@JsonProperty("viewAssetPath")
	public ViewAssetPath getViewAssetPath() {
		return viewAssetPath;
	}

	@JsonProperty("viewAssetPath")
	public void setViewAssetPath(Map<String, String> viewAssetPathMap) {
		this.viewAssetPath = new ViewAssetPath(viewAssetPathMap);
	}

	@JsonProperty("muleInfo")
	public MuleInfo getInfo() {
		return info;
	}

	@JsonProperty("muleInfo")
	public void setInfo(Map<String, String> info) {
		this.info = new MuleInfo(info);
	}
}
