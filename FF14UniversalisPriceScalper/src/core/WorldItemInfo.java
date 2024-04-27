package core;

import java.math.BigDecimal;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WorldItemInfo extends JSONItemInfo {
	
	public WorldItemInfo(JSONObject currentlyShownView, JSONObject historyView){
		super(currentlyShownView);
		
		this.mostCommonStackSize = getMostCommonStackSize(historyView);
		
		if(this.mostCommonStackSize > 0) {
			this.minPrice = get7DayAvgPrice(historyView);
		}
		else {
			this.minPrice = parseMinPrice(currentlyShownView);
			this.mostCommonStackSize = getMostCommonStackSize(currentlyShownView);
		}
		
		this.regularSaleVelocity = parseRegularSaleVelocity(historyView);
	}
	
	@Override
	protected Integer parseMinPrice(JSONObject currentlyShownView) {
		return ((Long) currentlyShownView.get("minPriceNQ")).intValue();
	}
	
	@Override
	protected String parseWorldName(JSONObject currentlyShownView) {
		return currentlyShownView.get("worldName").toString();
	}
	public String getWorldName() {
		return worldName;
	}

	private Integer get7DayAvgPrice(JSONObject historyView) {
				
		JSONArray entries  = (JSONArray) historyView.get("entries");
		//HashMap<String, Integer> worldMinPriceHistogram = new HashMap<String, Integer>();
		int sum = 0;
		for(int i = 0; i < entries.size(); i++) {
			JSONObject entry = (JSONObject) entries.get(i);
			System.err.print("");
			Integer pricePerUnit = ((Long) entry.get("pricePerUnit")).intValue();
			sum += pricePerUnit;
		}
		
		int avgPricePerUnit = sum/entries.size();
		
		return avgPricePerUnit;
	}
	
	private BigDecimal parseRegularSaleVelocity(JSONObject view) {
		return getBigDecimalFromJSONObject(view.get("nqSaleVelocity"));
	}
}
