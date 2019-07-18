package net.sf.zoftwhere.mule.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class Invoice {

	private String date;

	private String number;

	private String dueDate;

	private String subTotal;

	private String tax;

	private String total;

	private List<InvoiceItem> item;

	public Invoice() {
	}
}
