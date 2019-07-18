package net.sf.zoftwhere.mule.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.zoftwhere.dropwizard.AbstractEntity;
import net.sf.zoftwhere.mule.model.LetterModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

@Entity
@NamedQuery(name = "Letter.All", query = "select o from Letter o")
@Getter
@Setter
@Accessors(chain = true)
public class Letter extends AbstractEntity<Integer> {

	@Id
	private Integer id;

	@Column(name = "name")
	private String name;

	public static LetterModel asLetterModel(Letter letter) {
		LetterModel model = new LetterModel();
		model.setId(letter.getId().toString());
		model.setName(letter.getName());
		return model;
	}
}
