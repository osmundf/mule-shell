package net.sf.zoftwhere.mule.jpa;

import java.time.Instant;
import java.time.ZoneOffset;
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
import net.sf.zoftwhere.mule.model.SessionModel;
import net.sf.zoftwhere.mule.model.SystemModel;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import static net.sf.zoftwhere.time.Instants.withZoneOffset;

@Entity(name = "ShellSession")
@NamedQuery(name = "ShellSession.All", query = "select o from ShellSession o")
@NamedQuery(name = "ShellSession.byAccountId", query = "select o from ShellSession o where o.owner.id = :accountId and o.deletedAt is null")
@NamedQuery(name = "ShellSession.byIdAndAccountId", query = "select o from ShellSession o where o.id = :id and o.owner.id = :accountId and o.deletedAt is null")
@Accessors(chain = true)
public class ShellSession extends AbstractEntity<UUID> {

	public static SessionModel asSessionModel(ShellSession session, ZoneOffset zoneOffset) {
		String jvmVersion = System.getProperty("java.version");
		SessionModel model = new SessionModel();
		model.setId(session.getId());

		model.setSystem(new SystemModel().type("Java").version(jvmVersion));
		model.setCreatedAt(withZoneOffset(session.getCreatedAt(), zoneOffset));
		model.setClosedAt(withZoneOffset(session.getDeletedAt(), zoneOffset));
		model.setSuspendedAt(withZoneOffset(session.getSuspendedAt(), zoneOffset));

		return model;
	}

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn
	@Getter
	private Account owner;

	@Column()
	@Getter
	@Setter
	private Instant suspendedAt = getCreatedAt();

	public ShellSession() {
	}

	public ShellSession(Account owner) {
		this.owner = owner;
	}
}
