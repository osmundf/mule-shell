package net.sf.zoftwhere.dropwizard;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;

import static java.util.Objects.requireNonNull;

@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> {

	@Column(nullable = false)
	@Getter
	private Instant createdAt = Instant.now();

	@Column()
	@Getter
	private Instant deletedAt;

	protected AbstractEntity() {
	}

	protected AbstractEntity(Instant createdAt) {
		this.createdAt = requireNonNull(createdAt);
	}

	public abstract ID getId();

	public void delete() {
		this.deletedAt = Instant.now().atOffset(ZoneOffset.UTC).toInstant();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AbstractEntity)) {
			return false;
		}
		AbstractEntity<?> that = (AbstractEntity<?>) o;
		return getId().equals(that.getId());
	}
}
