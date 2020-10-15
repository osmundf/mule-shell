package net.sf.zoftwhere.mule.jpa;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.mule.model.RoleModel;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity(name = "Role")
@NamedQuery(name = "Role.all", query = "select o from Role o")
@NamedQuery(name = "Role.byKey", query = "select o from Role o where o.key = :key and o.deletedAt is null")
@Accessors(chain = true)
public class Role extends AbstractEntity<UUID> {

	public static String getKey(RoleModel roleModel) {
		final var packageName = roleModel.getClass().getPackage().getName();
		final var enumName = roleModel.name();
		return packageName + ":" + enumName;
	}

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

	public Role() {
	}

	public Role(String key, String name, String value, Integer priority) {
		this.key = key;
		this.name = name;
		this.value = value;
		this.priority = priority;
	}
}
