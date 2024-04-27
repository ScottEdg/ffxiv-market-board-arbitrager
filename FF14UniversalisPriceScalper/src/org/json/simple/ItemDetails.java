package org.json.simple;

import java.math.BigDecimal;

public class ItemDetails {
	protected Integer itemID = 0;
	protected String itemName = "";
	//protected Integer minPrice= 0;
	protected Integer minTotal = 0;
	public Integer getMinTotal() {
		return minTotal;
	}

	protected Long mostCommonStackSize = 0l;
	protected BigDecimal regularSaleVelocity = new BigDecimal(0);
	
	protected String worldName = "";

	public Integer getItemID() {
		return itemID;
	}
	
	public String getItemName() {
		return itemName;
	}

	public Long getMostCommonStackSize() {
		return mostCommonStackSize;
	}

	public BigDecimal getRegularSaleVelocity() {
		return regularSaleVelocity;
	}
	
	public String getWorldName() {
		return worldName;
	}
}
