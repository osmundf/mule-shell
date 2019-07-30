package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.mule.model.AccessRoleModel;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.util.UUID;

@Entity(name = "AccessRole")
@NamedQuery(name = "AccessRole.all", query = "select o from AccessRole o")
@NamedQuery(name = "AccessRole.byKey", query = "select o from AccessRole o where o.key = :key and o.deletedAt is null")
@Accessors(chain = true)
public class AccessRole extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@Column(nullable = false, length = 80)
	@Getter
	private String key;

	@Column(nullable = false, length = 40)
	@Getter
	private String name;

	@Column(nullable = false, length = 40)
	@Getter
	private String value;

	@Column(nullable = false)
	@Getter
	private Integer priority;

	public AccessRole() {
	}

	public AccessRole(String key, String name, String value, Integer priority) {
		this.key = key;
		this.name = name;
		this.value = value;
		this.priority = priority;
	}

	public static String getKey(AccessRoleModel role) {
		final var packageName = role.getClass().getPackage().getName();
		final var enumName = role.name();
		return packageName + ":" + enumName;
	}
}
