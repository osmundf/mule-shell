package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "Account")
@NamedQuery(name = "Account.All", query = "select o from Account o")
@Getter
@Setter
@Accessors(chain = true)
public class Account extends AbstractEntity<UUID> {

	@Id
	private UUID id = UUID.randomUUID();

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private String emailAddress;

	@OneToMany(mappedBy = "account")
	private List<AccessToken> accessTokenList = new ArrayList<>();
}
