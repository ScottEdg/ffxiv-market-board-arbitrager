package core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MarketBoardListing2 implements Comparable<MarketBoardListing2>{
	private Integer itemID = 0;
	private String itemName = "";
	//private Integer worldID;
	private String worldName = "";
	private Integer minPrice= 0;
	private Long mostCommonStackSize = 0l;
	private Integer minTotal = 0;
	//private Integer minPriceNQ;
	//private Integer minPriceHQ;
	private BigDecimal regularSaleVelocity = new BigDecimal(0);
	
	private BigDecimal expectedProfitValue = new BigDecimal(0);
	private BigDecimal expectedProfitPercent = new BigDecimal(0);
	private BigDecimal profitablyScore = new BigDecimal(0);
	
	private JSONArray listingViews = new JSONArray();
	
	public MarketBoardListing2() {
		super();

	}
	
	public MarketBoardListing2(String csvLine) {
		super();
		
	}

	public MarketBoardListing2(JSONObject currentlyShownView, JSONObject salesHistoryView, String worldName, Integer dataCenterMinPrice, BigDecimal dataCenterRegularSaleVelocity) {
		super();
		if(currentlyShownView != null) {
			this.itemID = ((Long) currentlyShownView.get("itemID")).intValue();
			
			this.minPrice = ((Long) currentlyShownView.get("minPriceNQ")).intValue();
			JSONObject stackSizeHistogramNQ  = (JSONObject) currentlyShownView.get("stackSizeHistogramNQ");
			Long maxStackSize = 0l;
			for(Object key : stackSizeHistogramNQ.keySet()) {
				if((Long) stackSizeHistogramNQ.get(key) > maxStackSize) {
					this.mostCommonStackSize = Long.parseLong((String) key);
					maxStackSize = (Long) stackSizeHistogramNQ.get(key);
				}
			}
			
			this.regularSaleVelocity = getBigDecimalFromJSONObject(currentlyShownView.get("nqSaleVelocity"));
			
		//	this.minPriceNQ = ((Long) currentlyShownView.get("minPriceNQ")).intValue();
		//	this.minPriceHQ = ((Long) currentlyShownView.get("minPriceHQ")).intValue();
			if(dataCenterMinPrice == 0) {
				this.expectedProfitPercent = new BigDecimal(0);
				this.expectedProfitValue = new BigDecimal(0);
			}
			else {
				this.expectedProfitPercent = new BigDecimal(this.minPrice).divide(new BigDecimal(dataCenterMinPrice), 2, RoundingMode.HALF_UP);
				this.expectedProfitPercent = this.expectedProfitPercent.subtract(new BigDecimal(1));
				this.expectedProfitValue = new BigDecimal(this.minPrice).subtract(this.regularSaleVelocity);
			}
			//this.expectedProfit = new BigDecimal(6).min(this.expectedProfit).subtract(new BigDecimal(1));
			//this.regularSaleVelocity = dataCenterRegularSaleVelocity.multiply(new BigDecimal(5));
			
			//this.profitablyScore = this.expectedProfit.multiply(dataCenterRegularSaleVelocity).movePointLeft(1);
			//this.regularSaleVelocity =  dataCenterRegularSaleVelocity;
			this.profitablyScore = this.expectedProfitPercent.multiply(this.regularSaleVelocity).multiply(this.expectedProfitValue).movePointLeft(4);

			this.worldName = worldName;
		}
	}
	public MarketBoardListing2(JSONObject currentlyShownView, JSONObject historyView) {
		super();

		this.itemID = ((Long) currentlyShownView.get("itemID")).intValue();
		
		this.minPrice = ((Long) currentlyShownView.get("minPriceNQ")).intValue();
	//	this.minPriceNQ = ((Long) currentlyShownView.get("minPriceNQ")).intValue();
	//	this.minPriceHQ = ((Long) currentlyShownView.get("minPriceHQ")).intValue();

		this.regularSaleVelocity = getBigDecimalFromJSONObject(historyView.get("nqSaleVelocity"));
		this.worldName = getMostCommonWorldNameWithMinPrice(currentlyShownView);

	}
	
	public MarketBoardListing2(JSONObject dataCenterCurrentlyShownView) {
		super();

		this.itemID = ((Long) dataCenterCurrentlyShownView.get("itemID")).intValue();
		
		this.minPrice = ((Long) dataCenterCurrentlyShownView.get("minPriceNQ")).intValue();
	//	this.minPriceNQ = ((Long) currentlyShownView.get("minPriceNQ")).intValue();
	//	this.minPriceHQ = ((Long) currentlyShownView.get("minPriceHQ")).intValue();

		this.regularSaleVelocity = getBigDecimalFromJSONObject(dataCenterCurrentlyShownView.get("nqSaleVelocity"));
		this.worldName = getMostCommonWorldNameWithMinPrice(dataCenterCurrentlyShownView);

	}
	
	private BigDecimal getBigDecimalFromJSONObject(Object value) {

		BigDecimal result = null;
		if (value instanceof Long) {
			result = new BigDecimal((Long) value);
		}
		
		if (value instanceof Double) {
			result = new BigDecimal((Double) value);
		}
		
		int numWholeDigits = result.precision() - result.scale();
		result = result.round(new MathContext(numWholeDigits + 8));
		return result.stripTrailingZeros();
	}
	
	private String getMostCommonWorldNameWithMinPrice(JSONObject currentlyShownView) {
		String result = "None";
		
		this.listingViews  = (JSONArray) currentlyShownView.get("listings");
		HashMap<String, Integer> worldMinPriceHistogram = new HashMap<String, Integer>();
		for(int i = 0; i < this.listingViews.size(); i++) {
			JSONObject listingView = (JSONObject) this.listingViews.get(i);
			Integer pricePerUnit = ((Long) listingView.get("pricePerUnit")).intValue();
			//System.err.println(pricePerUnit);
			if(pricePerUnit.equals(minPrice)) {
				//Keep track of quantity to determine which world to purchase items from if minimum price is the same.
				Integer quantity = ((Long) listingView.get("quantity")).intValue();
				String key = (String) listingView.get("worldName");
				this.minTotal = ((Long) listingView.get("total")).intValue();

				//create new entry
				if(!worldMinPriceHistogram.containsKey(key)) {
					worldMinPriceHistogram.put(key, quantity);
				}
				else {//update total quantity
					worldMinPriceHistogram.replace(key, quantity + worldMinPriceHistogram.get(key));
				}
			}
		}
		//now set the worldname with the highest total quantity
		Integer highestTotalQuantity = 0;
		for(String worldName : worldMinPriceHistogram.keySet()) {
			if(highestTotalQuantity < worldMinPriceHistogram.get(worldName)) {
				result = worldName;
				highestTotalQuantity = worldMinPriceHistogram.get(worldName);
			}
		}
		return result;

}

	public Integer getItemID() {
		return itemID;
	}

	public void setItemID(Integer itemID) {
		this.itemID = itemID;
	}
/*
	public Integer getWorldID() {
		return worldID;
	}

	public void setWorldID(Integer worldID) {
		this.worldID = worldID;
	}
*/
	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public Integer getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Integer minPrice) {
		this.minPrice = minPrice;
	}

	public Long getMostCommonStackSize() {
		return mostCommonStackSize;
	}

	public Integer getMinTotal() {
		return this.minTotal;
	}


	public BigDecimal getRegularSaleVelocity() {
		return regularSaleVelocity;
	}

	public void setRegularSaleVelocity(BigDecimal regularSaleVelocity) {
		this.regularSaleVelocity = regularSaleVelocity;
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
	
	public BigDecimal getExpectedValue() {
		return this.expectedProfitValue;
	}
	
	private void calculateProfitablyScore() {
		this.profitablyScore = this.expectedProfitPercent.multiply(this.regularSaleVelocity).multiply(this.expectedProfitValue).movePointLeft(4);

	}

	public BigDecimal getProfitablyScore() {
		return profitablyScore;
	}

	public String toCSVLine() {
	
		ArrayList<String> csvLineItems = new ArrayList<String>();
		csvLineItems.add(this.itemID.toString());
		csvLineItems.add(this.itemName);
		csvLineItems.add(this.worldName);
		csvLineItems.add(this.minPrice.toString());
		csvLineItems.add(this.minTotal.toString());
		csvLineItems.add(regularSaleVelocity.toPlainString());
		csvLineItems.add(expectedProfitPercent.toPlainString());
		csvLineItems.add(profitablyScore.toPlainString());
		
		return String.join(",", csvLineItems) + System.lineSeparator();
	}

	@Override
	public int compareTo(MarketBoardListing2 o) {
		return this.profitablyScore.compareTo(o.getProfitablyScore()) * -1;
	}

	public JSONArray getListingViews() {
		return listingViews;
	}


	
	public Long getMinPriceByAvaialbeQuantity(Long quantityToSell) {
		Long pricePerUnit = 0l;
		if(quantityToSell > 0) {
			Long totalQuantity = 0l;
			Long previousQuantity = 0l;
			Long pricePerStack = 0l;

			for(int i = 0; i < this.listingViews.size(); i++) {
				JSONObject listingView = (JSONObject) this.listingViews.get(i);
				previousQuantity = totalQuantity;
				totalQuantity += (Long) listingView.get("quantity");
				Long listingPricePerUnit = (Long) listingView.get("pricePerUnit");
				
				if(totalQuantity >= quantityToSell) {
					totalQuantity =  quantityToSell;
					pricePerStack += listingPricePerUnit * (totalQuantity - previousQuantity);
					//System.err.println(this.itemID + " - " + listingPricePerUnit + "x" + (totalQuantity - previousQuantity) + "=" + pricePerStack + " : " + this.minPrice);
					break;
				}
				else if(totalQuantity < quantityToSell) {
					//totalQuantity += quantityToSell;
					pricePerStack += (totalQuantity - previousQuantity) * listingPricePerUnit;
					//System.err.println(this.itemID + " - " + listingPricePerUnit + "x" + (totalQuantity - previousQuantity) + "=" + pricePerStack + " : " + this.minPrice);
				}
			}
			BigDecimal temp =  new BigDecimal(pricePerStack).divide(new BigDecimal(quantityToSell), 0, RoundingMode.HALF_UP);
			pricePerUnit = temp.longValue();
		}
	return pricePerUnit;
	}
}
