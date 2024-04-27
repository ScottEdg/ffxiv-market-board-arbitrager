package core;

import java.math.BigDecimal;
import java.math.MathContext;

import org.json.simple.ItemDetails;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class JSONItemInfo extends ItemDetails {

	protected Integer minPrice= 0;
	
	protected JSONItemInfo(JSONObject currentlyShownView) {
		
		if(currentlyShownView != null) {
			this.itemID = parseItemId(currentlyShownView);
			this.minPrice = parseMinPrice(currentlyShownView);
			//this.mostCommonStackSize = parseMostCommonStack(currentlyShownView);
			
			this.worldName = parseWorldName(currentlyShownView);
		}
	}
	
	private Integer parseItemId(JSONObject currentlyShownView) {
		return  ((Long) currentlyShownView.get("itemID")).intValue();
	}
	
	abstract Integer parseMinPrice(JSONObject currentlyShownView);
	abstract String parseWorldName(JSONObject currentlyShownView);
	//abstract Long parseMostCommonStack(JSONObject jsonObjectView);
	
	protected Long getMostCommonStackSize(JSONObject view) {
		Long result = 0l;
		Long maxStackSize = 0l;
		JSONObject stackSizeHistogramNQ  = (JSONObject) view.get("stackSizeHistogramNQ");
		
		for(Object key : stackSizeHistogramNQ.keySet()) {
			if((Long) stackSizeHistogramNQ.get(key) > maxStackSize) {
				result = Long.parseLong((String) key);
				maxStackSize = (Long) stackSizeHistogramNQ.get(key);
			}
		}
		
		return result;
	}
	
	protected BigDecimal getBigDecimalFromJSONObject(Object value) {

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
	
	public Integer getMinPrice() {
		return minPrice;
	}

}
