package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
@Accessors(chain = true)
public class AccessRole extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	private UUID id = null;

	@Column(nullable = false, length = 80)
	private String key;

	@Column(nullable = false, length = 40)
	private String name;

	@Column(nullable = false, length = 40)
	private String value;

	@Column(nullable = false)
	private Integer priority;

	public static String getKey(AccessRoleModel role) {
		final var packageName = role.getClass().getPackage().getName();
		final var enumName = role.name();
		return packageName + ":" + enumName;
	}
}
