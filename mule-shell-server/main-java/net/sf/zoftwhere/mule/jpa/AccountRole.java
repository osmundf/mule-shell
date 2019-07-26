package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import java.util.UUID;

@Entity(name = "AccountRole")
@NamedQuery(name = "AccountRole.all", query = "select o from AccountRole o")
@NamedQuery(name = "AccountRole.byIdId", query = "select o from AccountRole o where o.account.id = :accountId and o.accessRole.id = :accessRoleId and o.deletedAt is null")
@NamedQuery(name = "AccountRole.byAccountId", query = "select o from AccountRole o where o.account.id = :accountId and o.deletedAt is null order by o.accessRole.priority")
@NamedQuery(name = "AccountRole.byAccountAndKey", query = "select o from AccountRole o where o.account.id = :accountId and o.accessRole.key = :key and o.deletedAt is null")
@NamedQuery(name = "AccountRole.byAccountAndRoleName", query = "select o from AccountRole o where o.account.id = :accountId and o.accessRole.name = :roleName and o.deletedAt is null")
@Getter
@Setter
@Accessors(chain = true)
public class AccountRole extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	private UUID id = null;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private Account account;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private AccessRole accessRole;

	@Column(nullable = false, length = 40)
	private String value;
}
