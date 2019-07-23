package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.mule.model.SessionModel;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity(name = "ShellSession")
@NamedQuery(name = "ShellSession.All", query = "select o from ShellSession o")
@Accessors(chain = true)
public class ShellSession extends AbstractEntity<UUID> {

	@Id
	@Generated(value = GenerationTime.INSERT)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(insertable = false)
	@Getter
	private UUID id = null;

	@Column(name = "name", length = 20)
	@Getter
	@Setter
	private String name;

	@Column(name = "suspended_at")
	@Getter
	@Setter
	private Instant suspendedAt = getCreatedAt();

	public ShellSession() {
	}

	public static SessionModel asSessionModel(ShellSession session, ZoneOffset zoneOffset) {
		SessionModel model = new SessionModel();
		model.setId(session.getId());

		model.setCreatedAt(withZoneOffset(session.getCreatedAt(), zoneOffset));
		model.setClosedAt(withZoneOffset(session.getDeletedAt(), zoneOffset));
		model.setSuspendedAt(withZoneOffset(session.getSuspendedAt(), zoneOffset));

		return model;
	}
}
