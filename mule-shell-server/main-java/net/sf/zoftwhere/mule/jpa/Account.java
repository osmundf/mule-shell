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
import net.sf.zoftwhere.mule.security.AccountSigner;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity(name = "Account")
@NamedQuery(name = "Account.all", query = "select o from Account o")
@NamedQuery(name = "Account.byUsername", query = "select o from Account o where o.username = :username")
@Accessors(chain = true)
public class Account extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@Column(unique = true, nullable = false, length = 39)
	@Getter
	private String username;

	@Column(unique = true, nullable = false, length = 100)
	@Getter
	private String emailAddress;

	@Column(nullable = false, length = 64)
	@Getter
	private byte[] salt;

	@Column(nullable = false, length = 64)
	@Getter
	private byte[] hash;

	public Account() {
	}

	public Account(String username, String emailAddress) {
		this.username = username;
		this.emailAddress = emailAddress;
		this.salt = new byte[0];
		this.hash = new byte[0];
	}

	public Account updateHash(AccountSigner signer, byte[] data) {
		this.salt = signer.generateSalt(64);
		this.hash = signer.getHash(this.salt, data);
		return this;
	}
}
