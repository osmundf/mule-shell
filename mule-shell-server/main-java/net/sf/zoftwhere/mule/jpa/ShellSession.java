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
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity(name = "Shell")
@NamedQuery(name = "Shell.All", query = "select o from Shell o")
@Getter
@Setter
@Accessors(chain = true)
public class ShellSession extends AbstractEntity<UUID> {

	@Id
	private UUID id = UUID.randomUUID();

	@Column(name = "name")
	private String name;

	public static SessionModel asSessionModel(ShellSession session) {
		final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		SessionModel model = new SessionModel();
		model.setId(session.getId());
		model.setCreatedAt(toUTCOffsetDateTime(session.getCreatedAt()));
		model.setClosedAt(toUTCOffsetDateTime(session.getDeletedAt()));
		model.setSuspendedAt(toUTCOffsetDateTime(Instant.now()));

//		model.setCreatedAt(new Date(session.getCreatedAt().getEpochSecond()));
//		model.setClosedAt(new Date(session.getDeletedAt().getEpochSecond()));
//		model.setSuspendedAt(new Date(Instant.now().getEpochSecond()));

//		model.setCreatedAt(DateTime.parse(session.getCreatedAt().toString()));

		return model;
	}

}
