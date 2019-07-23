package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
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
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	private UUID id = null;

	@Column(nullable = false, length = 20)
	private String userName;

	@Column(nullable = false, length = 80)
	private String emailAddress;

	@Column(nullable = false, length = 16)
	private byte[] salt;

	@Column(nullable = false, length = 16)
	private byte[] hash;

	@OneToMany(mappedBy = "account")
	private List<AccessToken> accessTokenList = new ArrayList<>();
}
