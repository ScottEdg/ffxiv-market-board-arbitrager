package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONObject;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ProfitFinder {
	
	//Change these values depending on where you play
	private static final String HOME_DATA_CENTER = "Primal";
	private static final String HOME_WORLD = "Behemoth";

	//tuning variables for viable market listings
	private static final float MIN_SALE_VELOCITY = 180f;
	private static final float MIN_EXPECTED_PROFIT_MARGIN_PERCENT = 0.10f;
	private static final int MIN_EXPECTED_SALE_VALUE = 10000;
	
	
	//Universalis data processing
	private static final int ITEM_ID_BLOCK_SIZE = 350;

	
	//itemUICategory
	private static final int FISHING_TACKLES = 33;
	
	private static final int MEDICINES = 44;
	private static final int INGREDIENTS = 45;
	private static final int MEALS = 46;
	
	private static final int STONE = 48;
	private static final int METALS = 49;
	private static final int LUMBER = 50;
	private static final int CLOTH = 51;
	
	private static final int REAGENTS = 54;
	
	private static final int MATERIA = 58;

	
	// TODO Auto-generated constructor stub
	public ProfitFinder() {
	}

	public void run() {

	List<String> marketableItemIds = fetchMarketableItemIDs();
	//run a short test
	//List<String> marketableItemIds = fetchMarketableItemIDs().subList(1000, 1500);
	//List<String> marketableItemIds = new ArrayList<String>();
	//marketableItemIds.add("14");
	//marketableItemIds.add("20751");
	
	System.err.println("Collecting data on " + marketableItemIds.size() + " items");
	
	HashMap<String, List<MarketBoardListing>> analyzedMarketListings = analyzeMarketListings(marketableItemIds);
	
	List<MarketBoardListing> viableMarketBoardListings = analyzedMarketListings.get("viableMarketBoardListings");
	List<MarketBoardListing> filteredMarketBoardListings = analyzedMarketListings.get("filteredMarketBoardListings");
	
	File file = new File("C:\\Users\\Obliv\\FF14Universalis\\FF14UniversalisPriceScalper\\resource\\results_" + System.currentTimeMillis() + " .csv");
	FileWriter fw = null;
	BufferedWriter bw = null;
	try {
		fw = new FileWriter(file);
		bw = new BufferedWriter(fw);
		for(int i = 0; i < viableMarketBoardListings.size(); i++) {
			if(viableMarketBoardListings.get(i) != null) {
				bw.append(viableMarketBoardListings.get(i).toCSVLine());
			}
		}		
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	finally {
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	file = new File("C:\\Users\\Obliv\\FF14Universalis\\FF14UniversalisPriceScalper\\resource\\toRemove\\results_" + System.currentTimeMillis() + " .csv");

	try {
		fw = new FileWriter(file);
		bw = new BufferedWriter(fw);
		for(int i = 0; i < filteredMarketBoardListings.size(); i++) {
			if(filteredMarketBoardListings.get(i) != null) {
				bw.append(filteredMarketBoardListings.get(i).toCSVLine());
			}
		}		
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	finally {
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	try {
		Thread.currentThread().sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	};
	
	

	
	System.setProperty("webdriver.chrome.driver","C:\\Users\\Obliv\\git\\ffxiv-market-board-arbitrager\\FF14UniversalisPriceScalper\\resource\\Selenium\\chromedriver.exe");
	String root = "user-data-dir=C:\\\\Users\\\\Obliv\\\\AppData\\\\Local\\\\Google\\\\Chrome\\\\User Data\\";
	
	HashMap<String, ChromeDriver> driverMap = new HashMap<String, ChromeDriver>();
	HashMap<String, Integer> worldCounter = new HashMap<String, Integer>();
	
	ArrayList<String> worlds = new ArrayList<String>();
	worlds.add("Excalibur");
	worlds.add("Exodus");
	worlds.add("Famfrit");
	worlds.add("Hyperion");
	worlds.add("Lamia");
	worlds.add("Leviathan");
	worlds.add("Ultros");
	worlds.add("Behemoth");

	
	//create a class to manage each driver for each world server
	for(String world : worlds) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(root + world);
		options.addArguments("--remote-allow-origins=*");// arguments needed as of chromedriver v. 111.0.5563.65
		
		ChromeDriver chromeDriver =  new ChromeDriver(options);
		
		driverMap.put(world, chromeDriver);
		worldCounter.put(world, 0);	
	}
	
	
	String universalisRootURL = "https://universalis.app/market/";
	
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	for(int i = 0; i < Math.min(viableMarketBoardListings.size(), 250) ; i++) {
		if(viableMarketBoardListings.get(i) != null) {
			String world = viableMarketBoardListings.get(i).getWorldName();
				System.err.println(i+1 +
							       ". ItemId: " + viableMarketBoardListings.get(i).getItemID() +
								   " - Sales Velocity: " + viableMarketBoardListings.get(i).getRegularSaleVelocity().toPlainString() + 
								   " - Estimated Profit: " + viableMarketBoardListings.get(i).getExpectedProfitPercent().toPlainString() +
								   " - Estimated Profit Value: " + viableMarketBoardListings.get(i).getExpectedProfitValue().toPlainString() +
								   " - Profitablity Score: " + viableMarketBoardListings.get(i).getProfitablyScore().toPlainString() +  
								   " - World: " + world);
				
				if(worldCounter.get(world) > 0) {
					
					driverMap.get(world).switchTo().newWindow(WindowType.TAB);
					
					//driverMap.get(world).get(universalisRootURL + myWorldMBLs.get(i).getItemID());
				}
			//	else {
			//		driverMap.get(world).get(universalisRootURL + myWorldMBLs.get(i).getItemID());
		//		}
				driverMap.get(world).get(universalisRootURL + viableMarketBoardListings.get(i).getItemID());
				worldCounter.put(world, worldCounter.get(world) + 1);
				
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			

			}
		}
	System.err.println("Done.");
}

	
	/**
	 * Analyzes market listings for a given list of item IDs.
	 *
	 * @param itemIds The list of item IDs to analyze
	 * @return A HashMap containing two lists of MarketBoardListing objects:
	 *         "viableMarketBoardListings" - list of market board listings deemed viable and profitable for resell.
	 *         "filteredMarketBoardListings" - list of market board listings filtered out
	 */
	private HashMap<String, List<MarketBoardListing>> analyzeMarketListings(List<String> itemIds) {
		HashMap<String, List<MarketBoardListing>> result = new HashMap<String, List<MarketBoardListing>>();
		
		List<MarketBoardListing> viableMarketBoardListings = new ArrayList<MarketBoardListing>();
		List<MarketBoardListing> filteredMarketBoardListings = new ArrayList<MarketBoardListing>();
		
		//create threads to request data for each call (up to 8)
		for(int i = 0; i < itemIds.size(); i += ITEM_ID_BLOCK_SIZE) {

			UniversalisConnectionManager universalisConnectionManager = new UniversalisConnectionManager();

			List<String> itemIdBlock= processItemIdBlock(i, itemIds, universalisConnectionManager);

			for(String itemId : itemIdBlock){

				// Skip if the JSON data for the item is not valid
				if(!universalisConnectionManager.isValidJSON(itemId)) {
					continue;
				}
				
				// Retrieve current listings and sales history for the item
				JSONObject homeDataCenterCurrentlyShownView = universalisConnectionManager.getHomeDataCenterCurrentlyShownViewByItemId(itemId);
				JSONObject homeWorldCurrentlyShownView = universalisConnectionManager.getHomeWorldCurrentlyShownViewByItemId(itemId);
				JSONObject homeWorldHistoryView = universalisConnectionManager.getHomeWorldHistoryViewByItemId(itemId);
				
				DataCenterItemInfo homeDataCenterItemInfo = new DataCenterItemInfo(homeDataCenterCurrentlyShownView);
				WorldItemInfo homeWorldItemDataInfo = new WorldItemInfo(homeWorldCurrentlyShownView, homeWorldHistoryView);
				
				// Skip if the minimum market board listing is not valid
				if(!isDataCenterMinMarketBoardListingValid(homeDataCenterItemInfo)) {
					continue;
				}
				if(!isDataCenterMinMarketBoardListingValid(homeWorldItemDataInfo)) {
					continue;
				}
                // Create a MarketBoardListing object for the home world with additional data
				MarketBoardListing marketBoardListing = new MarketBoardListing(homeWorldItemDataInfo, homeDataCenterItemInfo);
				
                // Check if the market board listing is viable and add it to the appropriate list
				if(isViableListing(marketBoardListing)) {
					viableMarketBoardListings.add(marketBoardListing);
				}
				else {
					//create a list of items to write to CSV
					filteredMarketBoardListings.add(marketBoardListing);
				}
			}
		}
		
		// Sort both lists by profitability score.
		Collections.sort(viableMarketBoardListings);
		Collections.sort(filteredMarketBoardListings);
		
		result.put("viableMarketBoardListings", viableMarketBoardListings);
		result.put("filteredMarketBoardListings", filteredMarketBoardListings);

		return result;
	}
	
	
	//ToDo: need to update this as method names don't match with current functionality
	private List<String> fetchMarketableItemIDs(){
		UniversalisConnectionManager ucr = new UniversalisConnectionManager();
		ArrayList<String> itemIDs = ucr.getMarketableItemIds();
		itemIDs.removeAll(getMarketableItemsFromCSV());
		return itemIDs;
	}
	
	/**
	 * Processes a block of items within a specified range and sends a request to Universalis API for data retrieval.
	 *
	 * @param i The starting index of the block.
	 * @param items The list of items.
	 * @param ucr The UniversalisConnectionManager instance used for making API requests.
	 * @return The processed block of items.
	 */
	private List<String> processItemIdBlock(int i, List<String> items, UniversalisConnectionManager ucr) {
		int from = i;
		int to = Math.min(i + ITEM_ID_BLOCK_SIZE, items.size());
		System.err.println("\nBlock [" + from + ":" + (to-1) + "]");
		List<String> block = items.subList(from, to);
		long startTime = System.currentTimeMillis();
		
		ucr.requestData(HOME_DATA_CENTER, HOME_WORLD, block);
		
		long stopTime = System.currentTimeMillis();
		
		long blockProcessingTimeMSeconds = stopTime - startTime;

		BigDecimal blockProcessingTimeSeconds = new BigDecimal(blockProcessingTimeMSeconds).movePointLeft(3);
		int numWholeDigits = blockProcessingTimeSeconds.precision() - blockProcessingTimeSeconds.scale();
		blockProcessingTimeSeconds = blockProcessingTimeSeconds.round(new MathContext(numWholeDigits + 3));
		BigDecimal itemProcesstingTimeSeconds = blockProcessingTimeSeconds.divide(new BigDecimal(block.size()), 3, RoundingMode.HALF_UP);
		System.err.println("This block took " + blockProcessingTimeSeconds.toPlainString() + "s to process. (" + itemProcesstingTimeSeconds.toPlainString() + "s per item.)");
		
		return block;
	}
	
	/**
	 * Determines whether a given market board listing is viable based on certain criteria.
	 * A market board listing is considered viable if it meets the following conditions:
	 * - Regular sale velocity is greater than or equal to the minimum sale velocity.
	 * - Expected profit percentage is greater than or equal to the minimum expected profit margin percentage.
	 * - Expected sale value is greater than or equal to the minimum expected sale value.
	 *
	 * @param marketBoardListing The market board listing to evaluate for viability.
	 * @return {@code true} if the market board listing is viable according to the specified criteria, {@code false} otherwise.
	 */
	private boolean isViableListing(MarketBoardListing marketBoardListing) {
		
		if(marketBoardListing.getRegularSaleVelocity().floatValue() < MIN_SALE_VELOCITY) {
			return false;
		}
		
		if(marketBoardListing.getExpectedProfitPercent().floatValue() < MIN_EXPECTED_PROFIT_MARGIN_PERCENT) {
			return false;
		}
		
		if(marketBoardListing.getExpectedProfitValue().intValue() < MIN_EXPECTED_SALE_VALUE) {
			return false;
		}
		
		return true;
	}
	
	private HashSet<Integer> getInactiveItems(){
		HashSet<Integer> results = new HashSet<Integer>();
		File in = new File("C:\\Users\\Obliv\\FF14Universalis\\FF14UniversalisPriceScalper\\resource\\Filters\\inactiveItems.txt");
		try {
			FileReader fr = new FileReader(in);
			BufferedReader br = new BufferedReader(fr);
			
			br.readLine();
			
			String line = null;
			while((line = br.readLine()) != null) {
				results.add(Integer.parseInt(line));
			}
		}
		catch(IOException e) {
			
		}
		
		return results;
	}
	
	
	public ArrayList<String> getMarketableItemsFromCSV() {
		ArrayList<String> result = new ArrayList<String>();
		
		HashSet<Integer> inactiveItems = this.getInactiveItems();
		
		
		
		//ToDo: create method for this
		HashSet<Integer> blackListedCatagories = new HashSet<Integer>();
	    blackListedCatagories.add(47);
		blackListedCatagories.add(49);
		blackListedCatagories.add(52);

		blackListedCatagories.add(53);
		blackListedCatagories.add(56);
		blackListedCatagories.add(61);
		blackListedCatagories.add(64);
		//blackListedCatagories.add(76);
		//blackListedCatagories.add(81);
		blackListedCatagories.add(82);
		blackListedCatagories.add(85);
		blackListedCatagories.add(94);
		blackListedCatagories.add(95);
		
		//ToDo: create method for this
		HashSet<String> blackListedItems = new HashSet<String>();
		blackListedItems.add("Egg of Elpis");

		blackListedItems.add("Berkanan Sap");
		blackListedItems.add("Eblan Alumen");
		blackListedItems.add("Approved Grade 4 Skybuilders' Tortoise");
		blackListedItems.add("Snow Flax");
		//blackListedItems.add("Silver Ore");
		blackListedItems.add("Approved Grade 4 Skybuilders' White Cedar Log");
		blackListedItems.add("Yew Log");
		blackListedItems.add("Dark Hemp");
		blackListedItems.add("Aldgoat Leather");
		blackListedItems.add("Approved Grade 4 Artisanal Skybuilders' Cocoon");
		blackListedItems.add("Raptor Leather");
		blackListedItems.add("Almasty Fur");
		blackListedItems.add("Dark Chestnut Log");
		blackListedItems.add("Blue Landtrap Leaf");
		blackListedItems.add("Approved Grade 4 Artisanal Skybuilders' Caiman");
		blackListedItems.add("Phrygian Gold Ore");
		blackListedItems.add("Thavnairian Thread");
		blackListedItems.add("Electrum Ore");
		blackListedItems.add("Demimateria of the Inferno");
		blackListedItems.add("Riviera Armchair");
		blackListedItems.add("Growth Formula Gamma");
		blackListedItems.add("Fundamental Logogram");
		blackListedItems.add("Mahogany Aqueduct");
		blackListedItems.add("Honey Pot");
		blackListedItems.add("Zabuton Cushion");
		blackListedItems.add("Peacock Ore");
		//blackListedItems.add("Simple Curtain");
		//blackListedItems.add("Washbasin");
		blackListedItems.add("Jellyfish Humours");
		blackListedItems.add("Light-kissed Aethersand");
		//blackListedItems.add("Indirect Wall Lighting");
		//blackListedItems.add("False Classic Spectacles");
		//blackListedItems.add("Straight Stepping Stones");
		blackListedItems.add("Young Water Lily");
		blackListedItems.add("Stuffed Moogle");
		//blackListedItems.add("Grand Chair");
		//blackListedItems.add("Paper Partition");
		//blackListedItems.add("Curative Logogram");
		//blackListedItems.add("Dawnborne Aethersand");
		//blackListedItems.add("Hanging Planter Shelf");
		blackListedItems.add("Flawless Band");
		blackListedItems.add("Sophic Bead Fragment");
		blackListedItems.add("Mamook Pear");
		blackListedItems.add("Magicked Stable Broom");
		blackListedItems.add("Mamook Pear");
		blackListedItems.add("Persimmon Tannin");
		blackListedItems.add("Valfruit");
		blackListedItems.add("O'Ghomoro Berries");
		blackListedItems.add("Icetrap Leaf");
		blackListedItems.add("Approved Grade 3 Artisanal Skybuilders' Rice");
		blackListedItems.add("Ballroom Etiquette - Scholarly Certitude");
		blackListedItems.add("Elm Log");
		blackListedItems.add("Growth Formula Zeta");
		blackListedItems.add("Treated Camphorwood Lumber");
		blackListedItems.add("Mahogany Log");
		blackListedItems.add("Abalathian Mistletoe");
		blackListedItems.add("Cieldalaes Pineapple");
		blackListedItems.add("Xelphatol Apple");
		blackListedItems.add("Torreya Log");
		//blackListedItems.add("Torreya Log");
		blackListedItems.add("Blue Moon Phasmascape");
		blackListedItems.add("Cubus Flesh");
		blackListedItems.add("Distilled Water");
		blackListedItems.add("Shroud Seedling");
		blackListedItems.add("Grade 5 Strength Alkahest");
		blackListedItems.add("Grade 5 Dexterity Alkahest");
		//blackListedItems.add("Incensory");
		blackListedItems.add("Grade 5 Dexterity Alkahest");
		blackListedItems.add("Encyclopedia Eorzea");
		blackListedItems.add("Glade Partition Door");
		blackListedItems.add("Battle with the Four Fiends (Buried Memory) Orchestrion Roll");
		blackListedItems.add("Towel Hanger");
		blackListedItems.add("Cracked Arch Window");
		//blackListedItems.add("Bar Rack");
		//blackListedItems.add("Corner Counter");
		//blackListedItems.add("Summoning Bell");
		//blackListedItems.add("Brick Interior Wall");
		//blackListedItems.add("Hingan Sideboard");
		//blackListedItems.add("Oldrose Wall Planter");
		blackListedItems.add("Orchestrion");
		blackListedItems.add("Forgotten Fragment of Mastery");
		//blackListedItems.add("Indoor Oriental Waterfall");
		blackListedItems.add("Potted Oliphant's Ear");
		blackListedItems.add("Table Mat");
		//blackListedItems.add("Message Book Stand");
		//blackListedItems.add("Wooden Handrail");
		blackListedItems.add("Fish Oil");
		blackListedItems.add("Treated Spruce Lumber");
		blackListedItems.add("Hand-poured Coffee");
		//blackListedItems.add("Slanted Skylight");
		blackListedItems.add("Treated Spruce Lumber");
		blackListedItems.add("Black Alumen");
		//blackListedItems.add("Indoor Pond");
		blackListedItems.add("Torreya Lumber");
		blackListedItems.add("Wattle Petribark");
		//blackListedItems.add("Cream Yellow Dye");
		blackListedItems.add("Shroud Seedling");

		
/*		 
		blackListedItems.add("Hardsilver Nugget");
		blackListedItems.add("Volcanic Rock Salt");
		//blackListedItems.add("Timeworn Zonureskin Map");
		blackListedItems.add("Hippogryph Leather");
		blackListedItems.add("Palladium Nugget");
		blackListedItems.add("Manganese Ingot");
		blackListedItems.add("Deepgold Nugget");
		blackListedItems.add("Woolen Yarn");
		blackListedItems.add("Saiga Leather");
		blackListedItems.add("Silver Ingot");
		//blackListedItems.add("Mempisang Lumber");
	*/	
		
		//TODO: Make this project relative
		File in = new File("C:\\Users\\Obliv\\git\\ffxiv-market-board-arbitrager\\FF14UniversalisPriceScalper\\resource\\items\\marketableItems.csv");
		HashSet<Integer> found = new HashSet<Integer>();
		try {
		FileReader fr = new FileReader(in);
		BufferedReader br = new BufferedReader(fr);	
		
		String indexheader = br.readLine();
		String nameHeader = br.readLine();
		String typeHeader = br.readLine();
		
		HashMap<String, Integer> nameIndexMap = new HashMap<String, Integer>();
		
		ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList(nameHeader.split(",",-1)));
		for(Integer i = 0; i<columnNames.size();i++) {
			String columnName = columnNames.get(i);
			if(columnName.equals("#")) {
				columnName = "ItemID";
			}
			nameIndexMap.put(columnName, i);
		}
			int count = 0;
			String line = "";
			while((line = br.readLine()) != null) {
				ArrayList<String> values = new ArrayList<String>();
				Boolean isInsideDoubleQoutes = false;
				
				StringBuffer value = new StringBuffer();
				for(int i = 0; i<line.length(); i++) {
					char c = line.charAt(i);
					if(c == ',' && isInsideDoubleQoutes == false) {
						if(!value.isEmpty()) {
							values.add(value.toString());
							value = new StringBuffer();
						}
					}
					else if(c == '"'){
						//toggle
						if(isInsideDoubleQoutes == true) {
							isInsideDoubleQoutes = false;
							values.add(value.toString());
							value = new StringBuffer();
						}
						else {
							isInsideDoubleQoutes = true;
						}
					}
					else {
						value.append(c);
					}
				}
				
				Integer itemID = Integer.parseInt(values.get(nameIndexMap.get("ItemID")));
				String itemName = values.get(nameIndexMap.get("Name"));
				Short rarity = Short.parseShort(values.get(nameIndexMap.get("Rarity")));
				Integer itemLevel = Integer.parseInt(values.get(nameIndexMap.get("Level{Item}")));
				//ToDo: put this in its own class
				/* ItemUICategory
				 * 1 = Pugilist's Arms
				 * 2 = Gladiator's Arm
				 * 3 = Marauder's Arms
				 * 4 = Archer's Arms
				 * 5 = Lancer's Arms
				 * 6 = One–handed Thaumaturge's Arms
				 * 7 = Two–handed Thaumaturge's Arms
				 * 8 = One–handed Conjurer's Arms
				 * 9 = Two–handed Conjurer's Arms
				 * 10 = Arcanist's Grimoires
				 * 11 = Shields
				 * 12 = Carpenter's Primary Tools
				 * 13 = Carpenter's Secondary Tools
				 * 14 = Blacksmith's Primary Tools
				 * 15 = Blacksmith's Secondary Tools
				 * 16 = Armorer's Primary Tools
				 * 17 = Armorer's Secondary Tools
				 * 18 = Goldsmith's Primary Tools
				 * 19 = Goldsmith's Secondary Tools
				 * 20 = Leatherworker's Primary Tools
				 * 21 = Leatherworker's Secondary Tools
				 * 22 = Weaver's Primary Tools
				 * 23 = Weaver's Primary Tools
				 * 24 = Alchemist's Primary Tools
				 * 25 = Alchemist's Secondary Tools
				 * 26 = Culinarian's Primary Tools
				 * 27 = Culinarian's Secondary Tools
				 * 28 = Miner's Primary Tools
				 * 29 = Miner's Secondary Tools
				 * 30 = Botanist's Primary Tools
				 * 31 = Botanist's Secondary Tools
				 * 32 = Fisher's Primary Tools
				 * 33 = Fishing Tackles
				 * 34 = Head Armor - All Clasess
				 * 35 = Body Armor - All Classes
				 * 36 = Leg Armore - All Classes
				 * 37 = Hand Armor - All Classes
				 * 38 = Foot Armor - All Classes
				 * 40 = Necklace 
				 * 41 = Earrings
				 * 42 = Bracelets
				 * 43 = Rings
				 * 44 = Medicines
				 * 45 = Ingredients
				 * 46 = Meals
				 * 47 = Seafood
				 * 48 = Stone
				 * 49 = Metals
				 * 50 = Lumber
				 * 51 = Cloth
				 * 52 = Leather
				 * 53 = Bones
				 * 54 = Reagents
				 * 55 = Dyes
				 * 56 = Weapon Parts
				 * 57 = Furnishings
				 * 58 = Materia
				 * 59 = Crystals
				 * 60 = Catalysts
				 * 61 = Miscellany
				 * 64 = Construction Permits
				 * 65 = Roofs
				 * 66 = Exterior Walls
				 * 67 = Windows
				 * 68 = Doors
				 * 69 = Roof Decorations
				 * 70 = Exterior Wall Decorations
				 * 71 - Placards
				 * 72 = Fences
				 * 73 = Interior Walls
				 * 74 = Flooring
				 * 75 = Ceiling Lights
				 * 76 = Outdoor Furnishings
				 * 77 = Tables
				 * 78 = Tabletop Items
				 * 79 = Wall-mounted
				 * 80 = Rugs
				 * 81 = Minions
				 * 82 = Gardening Items
				 * 83 = Demimateria
				 * 85 = Seasonal Miscellany
				 * 84 = Rogue's Arms
				 * 87 = Dark Knight's Arms
				 * 88 = Machinist's Arms
				 * 89 = Astrologian's Arms
				 * 90 = Airship Hulls
				 * 91 = Airship Riggings
				 * 92 = Airship Aftcastles
				 * 93 = Airship Forecastles
				 * 94 = Orchestrion Components
				 * 95 = Paintings
				 * 96 = Samurai's Arms
				 * 97 - Red Mage's Arms
				 * 98 = Scholar's Arms
				 * 99 = Fisher's Secondary Tool
				 * 101 = Submersible Hulls
				 * 102 = Submersible Sterns
				 * 103 = Submersible Bows
				 * 104 = Submersible Bridges
				 * 105 = Dancer's Arms
				 * 106 = Gunbreaker's Arms
				 * 108 = Reaper's Arms
				 * 109 = Sage's Arms
				 */
				Integer itemUICategory = Integer.parseInt(values.get(nameIndexMap.get("ItemUICategory")));

				/* FilterGroup
				 * 1 = DoW Arms
				 * 2 = DoM Arms
				 * 3 = Shields
				 * 4 = Rings
				 * 5 = Meal
				 * 6 = Medicine
				 * 7 = Medicine
				 * 8 = Medicine
				 * 9 = Medicine (Ether)
				 */
				Integer filterGroup = Integer.parseInt(values.get(nameIndexMap.get("FilterGroup")));
				Integer itemSeries = Integer.parseInt(values.get(nameIndexMap.get("ItemSeries")));
				//ItemSeries
				Integer itemSearchCategory = Integer.parseInt(values.get(nameIndexMap.get("ItemSearchCategory")));
				Integer itemSortCategory = Integer.parseInt(values.get(nameIndexMap.get("ItemSortCategory")));

				/*
				 * 1 = grey
Bill Burr, Why does chat keep asking Dave Chappele to tell them the ghetto versions of things?				 * 2 = green
				 * 3 = blue
				 */
				Integer equipSlotCategory = Integer.parseInt(values.get(nameIndexMap.get("EquipSlotCategory")));

			
				
				// keep
				if(itemUICategory == MEDICINES) {
					if(itemLevel < 450 && !(itemName.equals("Cordial") || itemName.equals("Elixir"))) {
						result.add(itemID.toString());
					}
				}
				if(itemUICategory == INGREDIENTS) {
					if(itemLevel < 450 && !itemName.startsWith("Approved Grade")) {
						result.add(itemID.toString());
					}
				}
				if(itemUICategory == MEALS) {
					if(itemLevel < 450) {
						result.add(itemID.toString());
					}
				}
				
				if(itemUICategory == STONE) {
					if(itemLevel < 500 && !(itemName.startsWith("Approved Grade") || itemName.endsWith("Ore"))) {
						result.add(itemID.toString());
					}
				}
				
				if(itemUICategory == METALS) {
					if(itemLevel < 450 && !(itemName.equals("Steel Ingot") || itemName.equals("Mythril Ingot") || itemName.equals("Iron Ingot") || itemName.equals("Manganese Ingot") || itemName.equals("Dwarven Mythril Ingot"))) {
						result.add(itemID.toString());
					}
				}
				
				//remove low level cloth
				if(itemUICategory == CLOTH) {
					if(itemLevel < 450) {
						result.add(itemID.toString());
					}
				}

				//remove low level reagents
				if(itemUICategory == REAGENTS) {
					if(itemLevel < 450) {
						result.add(itemID.toString());
					}
				}
				
				//remove low level lumber
				if(itemUICategory == LUMBER) {
					if(itemLevel < 450) {
						result.add(itemID.toString());
					}
				}
				//remove low lever materia
				if(itemUICategory == MATERIA) {
					if(itemLevel < 560) {
						result.add(itemID.toString());
					}
				}

				if(inactiveItems.contains(itemID)) {
					result.add(itemID.toString());
				}
				
				if(blackListedItems.contains(itemName)) {
					result.add(itemID.toString());
				}

				if(blackListedCatagories.contains(itemUICategory)){
					result.add(itemID.toString());
				}
				if(itemUICategory == FISHING_TACKLES && itemLevel < 560 ) {
				//	System.err.println(itemName + " - " + filterGroup);
					result.add(itemID.toString());
				}
				
				if(equipSlotCategory > 0) {
					if(/*rarity < 4 || (rarity == 2 &&*/ itemLevel < 560) {
						count++;
						result.add(itemID.toString());
						//System.err.println(itemName);
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Blacklisted " + result.size() + " items.");
		return result;
	}
	
	private boolean isDataCenterMinMarketBoardListingValid(JSONItemInfo itemInfo) {
		if(itemInfo.getMinTotal() < 0) {
			return false;
		}
		if((itemInfo.getWorldName().equals("None"))) {
			return false;
		}
		if(itemInfo.getMostCommonStackSize() <= 0) {
			return false;
		}
		
		return true;
	}
}
