package net.sf.zoftwhere.dropwizard;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> {

	@Column(nullable = false, name = "created_at")
	private Instant createdAt = Instant.now();

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected AbstractEntity() {
	}

	protected AbstractEntity(Instant createdAt) {
		this.createdAt = requireNonNull(createdAt);
	}

	public static OffsetDateTime toUTCOffsetDateTime(Instant instant) {
		return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
	}

	public abstract ID getId();

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public void delete() {
		this.deletedAt = Instant.now();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractEntity)) return false;
		AbstractEntity<?> that = (AbstractEntity<?>) o;
		return getId().equals(that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
