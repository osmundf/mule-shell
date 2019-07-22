package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import java.util.UUID;

@Entity(name = "Token")
@NamedQuery(name = "Token.All", query = "select o from Token o")
@Getter
@Setter
@Accessors(chain = true)
public class AccessToken extends AbstractEntity<UUID> {

	@Id
	UUID id = UUID.randomUUID();

	@ManyToOne(cascade = CascadeType.ALL)
	private Account account;
}
