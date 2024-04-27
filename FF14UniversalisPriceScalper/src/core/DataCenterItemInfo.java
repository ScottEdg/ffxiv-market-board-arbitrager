package core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DataCenterItemInfo extends JSONItemInfo {
	private String dataCenterName = "";

	private JSONArray listingViews = new JSONArray();

	public DataCenterItemInfo(JSONObject currentlyShownView){
		super(currentlyShownView);
		
		this.mostCommonStackSize = getMostCommonStackSize(currentlyShownView);

		this.dataCenterName = currentlyShownView.get("dcName").toString();
		this.listingViews  = (JSONArray) currentlyShownView.get("listings");
		
	}
	
	@Override
	protected Integer parseMinPrice(JSONObject currentlyShownView) {
		return ((Long) currentlyShownView.get("minPriceNQ")).intValue();
	}
	
	@Override
	protected String parseWorldName(JSONObject currentlyShownView) {
		return getMostCommonWorldNameWithMinPrice(currentlyShownView);
	}
	
	private String getMostCommonWorldNameWithMinPrice(JSONObject currentlyShownView) {
		String result = "None";
		
		JSONArray listingViews  = (JSONArray) currentlyShownView.get("listings");
		HashMap<String, Integer> worldMinPriceHistogram = new HashMap<String, Integer>();
		for(int i = 0; i < listingViews.size(); i++) {
			JSONObject listingView = (JSONObject) listingViews.get(i);
			Integer pricePerUnit = ((Long) listingView.get("pricePerUnit")).intValue();
			//System.err.println(pricePerUnit);
			if(pricePerUnit.equals(this.minPrice)) {
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
	
	public Long getMinPriceByAvailableQuantity(Long quantityToSell) {
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
	
	public Long getMinPriceByAvailableQuantityChatGPT(Long quantityToSell) {
	    Long pricePerUnit = 0L;

	    if (quantityToSell <= 0) {
	        return pricePerUnit;
	    }

	    Long totalQuantity = 0L;
	    Long pricePerStack = 0L;

	    for (int i = 0; i < this.listingViews.size(); i++) {
	        JSONObject listingView = (JSONObject) this.listingViews.get(i);
	        Long listingQuantity = (Long) listingView.get("quantity");
	        Long listingPricePerUnit = (Long) listingView.get("pricePerUnit");

	        if (totalQuantity + listingQuantity >= quantityToSell) {
	            Long remainingQuantity = quantityToSell - totalQuantity;
	            pricePerStack += listingPricePerUnit * remainingQuantity;
	            break;
	        } else {
	            totalQuantity += listingQuantity;
	            pricePerStack += listingPricePerUnit * listingQuantity;
	        }
	    }

	    if (totalQuantity > 0) {
	        BigDecimal averagePricePerUnit = new BigDecimal(pricePerStack)
	                .divide(new BigDecimal(quantityToSell), 0, RoundingMode.HALF_UP);
	        pricePerUnit = averagePricePerUnit.longValue();
	    }

	    return pricePerUnit;
	}
	
	public JSONArray getListingViews() {
		return listingViews;
	}
}

