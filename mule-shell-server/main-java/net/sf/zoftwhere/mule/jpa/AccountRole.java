package net.sf.zoftwhere.mule.jpa;

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity(name = "AccountRole")
@NamedQuery(name = "AccountRole.all", query = "select o from AccountRole o")
@NamedQuery(name = "AccountRole.byIdId", query = "select o from AccountRole o where o.account.id = :accountId and o.role.id = :roleId and o.deletedAt is null")
@NamedQuery(name = "AccountRole.byAccountId", query = "select o from AccountRole o where o.account.id = :accountId and o.deletedAt is null order by o.role.priority")
@NamedQuery(name = "AccountRole.byAccountAndKey", query = "select o from AccountRole o where o.account.id = :accountId and o.role.key = :key and o.deletedAt is null")
@NamedQuery(name = "AccountRole.byAccountAndRoleName", query = "select o from AccountRole o where o.account.id = :accountId and o.role.name = :roleName and o.deletedAt is null")
@Accessors(chain = true)
public class AccountRole extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn
	@Getter
	private Account account;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn
	@Getter
	private Role role;

	@Column(nullable = false, length = 40)
	@Getter
	@Setter
	private String value;

	public AccountRole() {
	}

	public AccountRole(Account account, Role role, String value) {
		this.account = account;
		this.role = role;
		this.value = value;
	}
}
