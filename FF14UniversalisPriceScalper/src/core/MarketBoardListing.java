package core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.ItemDetails;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MarketBoardListing extends ItemDetails implements Comparable<MarketBoardListing>{

	private Integer worldMinPrice = 0;	
	private Integer dataCenterMinPrice = 0;
	
	private Long purchasablePrice = 0l;
	private Long sellablePrice = 0l;
	
	private BigDecimal expectedProfitValue = new BigDecimal(0);
	private BigDecimal expectedProfitPercent = new BigDecimal(0);
	private BigDecimal profitablyScore = new BigDecimal(0);
	
	
	public MarketBoardListing(WorldItemInfo worldItemInfo, DataCenterItemInfo dataCenterItemInfo) {
				
		this.itemID = worldItemInfo.getItemID();
		this.itemName = worldItemInfo.getItemName();
		this.mostCommonStackSize = worldItemInfo.getMostCommonStackSize();
		
		this.dataCenterMinPrice = dataCenterItemInfo.getMinPrice();
		
		this.worldMinPrice = worldItemInfo.getMinPrice();
		this.regularSaleVelocity = worldItemInfo.getRegularSaleVelocity();
		
		this.purchasablePrice = calculatePurchasablePrice(dataCenterItemInfo.getListingViews());
		this.sellablePrice = calculateSellablePrice();
		
		this.expectedProfitPercent = calculateExpectedProfitPercent();
		this.expectedProfitValue = calculateExpectedProfitValue();
		this.profitablyScore = calculateProfitablyScore();

		
		this.worldName = dataCenterItemInfo.getWorldName();
		//this.worldName = getMostCommonWorldNameWithMinPrice(dataCenterItemInfo.getListingViews());
	}
	
	public void setExpectedProfitPercent(BigDecimal expectedProfitPercent) {
		this.expectedProfitPercent = expectedProfitPercent.min(new BigDecimal(4));
		//reclalculate profitablity score
		calculateProfitablyScore();
	}
	
	public BigDecimal getExpectedProfitPercent() {
		return this.expectedProfitPercent;
	}
	
	public void setExpectedProfitValue(BigDecimal expectedProfitValue) {
		this.expectedProfitValue = expectedProfitValue;
		//reclalculate profitablity score
		calculateProfitablyScore();
	}
	
	public BigDecimal getExpectedProfitValue() {
		return this.expectedProfitValue;
	}
	
	private BigDecimal calculateProfitablyScore() {
		return this.expectedProfitPercent.multiply(this.regularSaleVelocity).multiply(this.expectedProfitValue).movePointLeft(4);

	}

	public BigDecimal getProfitablyScore() {
		return profitablyScore;
	}

	private Long calculateSellablePrice() {
		return this.worldMinPrice * this.mostCommonStackSize;
	}
	
	private Long calculatePurchasablePrice(JSONArray listingViews) {
		return getMinPriceByAvaialbeQuantity(listingViews) * this.mostCommonStackSize;
	}
	
	public Long getMinPriceByAvaialbeQuantity(JSONArray listingViews) {
		Long pricePerUnit = 0l;
		if(this.mostCommonStackSize > 0) {
			Long totalQuantity = 0l;
			Long previousQuantity = 0l;
			Long pricePerStack = 0l;

			for(int i = 0; i < listingViews.size(); i++) {
				JSONObject listingView = (JSONObject) listingViews.get(i);
				previousQuantity = totalQuantity;
				totalQuantity += (Long) listingView.get("quantity");
				Long listingPricePerUnit = (Long) listingView.get("pricePerUnit");
				
				if(totalQuantity >= this.mostCommonStackSize) {
					totalQuantity =  this.mostCommonStackSize;
					pricePerStack += listingPricePerUnit * (totalQuantity - previousQuantity);
					//System.err.println(this.itemID + " - " + listingPricePerUnit + "x" + (totalQuantity - previousQuantity) + "=" + pricePerStack + " : " + this.minPrice);
					break;
				}
				else if(totalQuantity < this.mostCommonStackSize) {
					//totalQuantity += quantityToSell;
					pricePerStack += (totalQuantity - previousQuantity) * listingPricePerUnit;
					//System.err.println(this.itemID + " - " + listingPricePerUnit + "x" + (totalQuantity - previousQuantity) + "=" + pricePerStack + " : " + this.minPrice);
				}
			}
			BigDecimal temp =  new BigDecimal(pricePerStack).divide(new BigDecimal(this.mostCommonStackSize), 0, RoundingMode.HALF_UP);
			pricePerUnit = temp.longValue();
		}
	return pricePerUnit;
	}
	
	private BigDecimal calculateExpectedProfitPercent() {
		if(this.purchasablePrice.intValue() == 0) {
			System.err.print("");
		}
		return new BigDecimal(this.sellablePrice).divide(new BigDecimal(this.purchasablePrice), 2,RoundingMode.HALF_UP).subtract(new BigDecimal(1));
	}
	
	private BigDecimal calculateExpectedProfitValue() {
		return new BigDecimal(sellablePrice - purchasablePrice);
	}
	
	public String toCSVLine() {
		
		ArrayList<String> csvLineItems = new ArrayList<String>();
		csvLineItems.add(this.itemID.toString());
		csvLineItems.add(this.itemName);
		csvLineItems.add(this.worldName);
		//csvLineItems.add(this.minPrice.toString());
		csvLineItems.add(this.minTotal.toString());
		csvLineItems.add(regularSaleVelocity.toPlainString());
		csvLineItems.add(expectedProfitPercent.toPlainString());
		csvLineItems.add(profitablyScore.toPlainString());
		
		return String.join(",", csvLineItems) + System.lineSeparator();
	}

	
	@Override
	public int compareTo(MarketBoardListing o) {
		return this.profitablyScore.compareTo(o.getProfitablyScore()) * -1;
	}

}
