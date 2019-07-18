package net.sf.zoftwhere.mule.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Customer {

	private String name;

	private String address;

	public Customer() {
	}
}
