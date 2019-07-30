package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import java.util.UUID;

@Entity(name = "Token")
@NamedQuery(name = "AccessToken.all", query = "select o from Token o")
@NamedQuery(name = "Token.byAccountId", query = "select o from Token o where o.accountRole.account.id = :accountId")
@Accessors(chain = true)
public class Token extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	UUID id = UUID.randomUUID();

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn
	@Getter
	private AccountRole accountRole;

	public Token() {
	}

	public Token(AccountRole accountRole) {
		this.accountRole = accountRole;
	}
}
