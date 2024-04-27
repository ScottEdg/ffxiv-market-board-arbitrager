import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.MarketBoardListing;
import core.ProfitFinder;
import core.UniversalisConnectionManager;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.v85.browser.model.WindowID;

import com.google.common.base.Stopwatch;

public class Main {

	public Main() {
		//cleanAndFilterMaketableItems();
		//getMarketableItemsFromCSV();
		//parseFile();
		//getWorlds();
		new ProfitFinder().run();
	}//1

	public void cleanAndFilterMaketableItems() {
		//TODO: Make these project relative
	    File dirtyFile = new File("C:\\Users\\Obliv\\git\\ffxiv-market-board-arbitrager\\FF14UniversalisPriceScalper\\resource\\items\\items_dirty.csv");
	    File out = new File("C:\\Users\\Obliv\\git\\ffxiv-market-board-arbitrager\\FF14UniversalisPriceScalper\\resource\\items\\marketableItems.csv");

		ArrayList<String> lines = new ArrayList<String>();

	    try {
			FileReader fr = new FileReader(dirtyFile);
			BufferedReader br = new BufferedReader(fr);			
			//ArrayList<String> lines = new ArrayList<String>();
			String line = "";
			while((line = br.readLine()) != null) {
				lines.add(line);
			}
			//
			Pattern p = Pattern.compile("^\\D");
			for(int i = lines.size()-1; i > 2; i--) {
				line = lines.get(i);
				Matcher m = p.matcher(line);
				if(m.find()) {
					lines.set(i-1, lines.get(i-1) + line);
					lines.remove(i);
				}
			}

			FileWriter fw = new FileWriter(out);
			BufferedWriter bw = new BufferedWriter(fw);	
			ArrayList<String> marketableIdsLines = new ArrayList<String>();
		    UniversalisConnectionManager ucm = new UniversalisConnectionManager();
		    ArrayList<String> marketableItemIds = ucm.getMarketableItemIds();
		    marketableIdsLines.addAll(lines.subList(0, 3));
		    int i = 0;
		    for(String itemId : marketableItemIds) {
		    	for(; i < lines.size(); i++) {
		    		line = lines.get(i);
		    		if(line.split(",")[0].equals(itemId)) {
		    			marketableIdsLines.add(line);
		    			//System.err.println(line);
		    			break;
		    		}
		    		
		    	}
		    }
		    
		    br.close();
		    
		    for(i = 0; i<marketableIdsLines.size(); i++) {
		    	bw.write(marketableIdsLines.get(i) + System.lineSeparator());
		    	System.err.println(marketableIdsLines.get(i));
		    }
		    bw.flush();
		    bw.close();
			System.err.print("");
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
			// TODO: handle exception
		}
	}
	
	
	
	public void parseFile(){
	    File f = new File("C:\\Users\\Obliv\\FF14Universalis\\FF14UniversalisPriceScalper\\resource\\items.csv");
	    
	    try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			
			br.readLine();
			ArrayList<String> headerNames = new ArrayList<String>(Arrays.asList(br.readLine().split(",")));
			
			HashMap<String, Integer> headerMap = new HashMap<>();
			for(Integer i = 0; i< headerNames.size(); i++) {
				headerMap.put(headerNames.get(i), i);
			}
			String line = null;
			while((line = br.readLine()) != null) {
				System.err.println(line.split(",")[headerMap.get("Name")].replace("\"", ""));
			}
			System.err.println();
			
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
			// TODO: handle exception
		}
	}
	
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		new Main();
		long stopTime = System.currentTimeMillis();
	
		BigDecimal totalTime = new BigDecimal(stopTime - startTime);
		
		BigDecimal miniutes = totalTime.divide(new BigDecimal(1000 * 60), 0, RoundingMode.FLOOR);
		float seconds = totalTime.divide(new BigDecimal(1000)).floatValue() % 60;
		
		System.err.println("Completed in " + miniutes.toPlainString() + ":" + seconds);
		//new B
	}
	
	
	
	//public void getWorlds() {
	/*	 try {
	            URL url = new URL("https://universalis.app/api/v2/worlds");
	            HttpURLConnection con = (HttpURLConnection) url.openConnection();
	            con.setRequestMethod("GET");

	            int status = con.getResponseCode();
	            System.out.println("Status: " + status);

	            BufferedReader in = new BufferedReader(
	                new InputStreamReader(con.getInputStream()));
	            String inputLine;
	            StringBuffer content = new StringBuffer();
	            while ((inputLine = in.readLine()) != null) {
	                content.append(inputLine);
	            }
	            in.close();
	            JsonObject json = new JsonParser().parse(content.toString()).getAsJsonObject();
	            JsonArray worlds = json.getAsJsonArray("worlds");
	            String[] worldName = new String[worlds.size()];
	            int i = 0;
	            for (JsonElement world : worlds) {
	                worldName[i] = world.getAsJsonObject().get("name").getAsString();
	                i++;
	            }
	            Arrays.sort(worldName);
	            for (String name : worldName) {
	                System.out.println(name);
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	}*/
/*	System.setProperty("webdriver.chrome.driver","C:\\Users\\Obliv\\FF14Universalis\\FF14UniversalisPriceScalper\\resource\\chromedriver.exe");

	ChromeOptions options1 = new ChromeOptions();
	options1.addArguments("user-data-dir=C:\\Users\\Obliv\\AppData\\Local\\Google\\Chrome\\User Data\\Behemoth");
	
	ChromeOptions options2 = new ChromeOptions();
	options2.addArguments("user-data-dir=C:\\Users\\Obliv\\AppData\\Local\\Google\\Chrome\\User Data\\Default");

	ChromeDriver driver1 = new ChromeDriver(options1);
	ChromeDriver driver2 = new ChromeDriver(options2);

	String mainWindow = driver1.getWindowHandle();
	driver1.get("https://universalis.app/market/37373");

	driver1.switchTo().newWindow(WindowType.TAB);
	driver1.get("https://universalis.app/market/37374");

	
	//driver2.switchTo().newWindow(WindowType.WINDOW);
	driver2.get("https://universalis.app/market/37373");

//	driver2.switchTo().newWindow(WindowType.WINDOW);
	//String newWindow = driver2.getWindowHandle();
	//driver1.get("https://universalis.app/market/37373");


	driver2.switchTo().newWindow(WindowType.TAB);
	driver2.navigate().to("https://universalis.app/market/37376");
	

	
		// TODO Auto-generated method stub
		try {
			ArrayList<String> itemIds = new ArrayList<String>();
			itemIds.add("24514");
			itemIds.add("37373");
			//URL url = new URL("https://universalis.app/api/v2/primal/24514,37373");
			String dataCenter = "Primal";
			String world = "Behemoth";

			HashMap<String, JSONObject> dataCenterMarketBoardListingsJSON = getMarketBoardListingsJSON(dataCenter, itemIds);
			HashMap<String, JSONObject> worldMarketBoardListingsJSON = getMarketBoardListingsJSON(world, itemIds);
			
			for(String itemId : itemIds) {
				Number dataCenterMinPrice = (Number) dataCenterMarketBoardListingsJSON.get(itemId).get("minPrice");
				Number worldMinPrice = (Number) worldMarketBoardListingsJSON.get(itemId).get("minPrice");
				System.err.println(itemId + " - " + dataCenter + " data center min price: " + dataCenterMinPrice + " " + world + " min price: " + worldMinPrice);
			}
			
		
			System.err.println();

		}
		catch(IOException | ParseException e){
			e.printStackTrace();
		}
	}
	*/
	
	
}
