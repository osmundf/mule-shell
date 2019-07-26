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
@Getter
@Setter
@Accessors(chain = true)
public class Account extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	private UUID id = null;

	@Column(unique = true, nullable = false, length = 39)
	private String username;

	@Column(unique = true, nullable = false, length = 100)
	private String emailAddress;

	@Column(nullable = false, length = 64)
	private byte[] salt;

	@Column(nullable = false, length = 64)
	private byte[] hash;
}
