package net.sf.zoftwhere.mule.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class InvoiceItem {

	private String date = "";

	private String detail = "";

	private String policy = "";

	private String total = "";

	public InvoiceItem() {
		this.date = "";
		this.detail = "";
		this.policy = "";
		this.total = "";
	}

	public InvoiceItem(String date, String detail, String policy, String total) {
		this.date = date;
		this.detail = detail;
		this.policy = policy;
		this.total = total;
	}
}
