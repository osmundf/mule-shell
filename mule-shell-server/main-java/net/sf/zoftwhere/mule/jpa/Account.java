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
import java.util.UUID;

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
	@Setter
	private byte[] salt;

	@Column(nullable = false, length = 64)
	@Getter
	@Setter
	private byte[] hash;

	public Account() {
	}

	public Account(String username, String emailAddress) {
		this.username = username;
		this.emailAddress = emailAddress;
	}
}
