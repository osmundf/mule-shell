package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.mule.model.SessionModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import java.time.Instant;
import java.util.UUID;

@Entity
@NamedQuery(name = "Session.All", query = "select o from Session o")
@Getter
@Setter
@Accessors(chain = true)
public class Session extends AbstractEntity<UUID> {

	@Id
	private UUID id;

	@Column(name = "name")
	private String name;

	public static SessionModel asSessionModel(Session session) {
		SessionModel model = new SessionModel();
		model.setCreatedAt(toUTCOffsetDateTime(session.getCreatedAt()));
		model.setClosedAt(toUTCOffsetDateTime(session.getDeletedAt()));
		model.setSuspendedAt(toUTCOffsetDateTime(Instant.now()));
		return model;
	}
}
