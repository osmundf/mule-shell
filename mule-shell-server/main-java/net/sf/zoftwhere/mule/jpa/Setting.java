package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.util.UUID;

@Entity(name = "Setting")
@NamedQuery(name = "Setting.byKey", query = "select o from Setting o where o.key = :key and o.deletedAt is null")
@Accessors(chain = true)
public class Setting extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@Column(unique = true, nullable = false, length = 128)
	@Getter
	private String key;

	@Column(nullable = false, length = 1024)
	@Getter
	private String value;

	public Setting() {
	}

	public Setting(final String key, final String value) {
		this.key = key;
		this.value = value;
	}
}
