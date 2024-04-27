package core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UniversalisConnectionManager {

	Map<String, JSONObject> homeDataCenterItemIdToCurrentlyShownViewMap;

	Map<String, JSONObject> homeWorldItemIdToCurrentlyShownViewMap;
	Map<String, JSONObject> homeWorldItemIdToHistoryViewMap;

	
	private static int MAX_CONNECTIONS = 8;
	private static int MAX_ITEMS_PER_QUERY = 50;
	private static int THREAD_CREATE_DELAY = 500;
	
	public UniversalisConnectionManager() {
		this.homeDataCenterItemIdToCurrentlyShownViewMap =  Collections.synchronizedMap(new HashMap<String, JSONObject>());
		
		this.homeWorldItemIdToCurrentlyShownViewMap = Collections.synchronizedMap(new HashMap<String, JSONObject>());
		this.homeWorldItemIdToHistoryViewMap = Collections.synchronizedMap(new HashMap<String, JSONObject>());
	}
	
	public void requestData(String dataCenter, String world, List<String> itemIDs) {
		
		List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());

		ArrayList<String> items = new ArrayList<String>(itemIDs);

		for(int i = 0; i < items.size(); i += MAX_ITEMS_PER_QUERY) {
			try {
				Thread.currentThread().sleep(THREAD_CREATE_DELAY);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int from = i;
			int to = Math.min(i + MAX_ITEMS_PER_QUERY, items.size());

			System.err.println("Getting " + (to) + "/" + items.size() + " Sales History from " + world);
			
			Thread thread = new Thread(getMarketBoardHistoryViewsRunnable(homeWorldItemIdToHistoryViewMap, world, items.subList(from, to)));
			
			queryUniversalisAPI(threads, thread);
		}
		
		
		for(int i = 0; i < items.size(); i+= MAX_ITEMS_PER_QUERY) {
			try {
				Thread.currentThread().sleep(THREAD_CREATE_DELAY);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int from = i;
			int to = Math.min(i+MAX_ITEMS_PER_QUERY, items.size());
			
			System.err.println("Getting " + (to) + "/" + items.size() + " Current Listings from " + dataCenter);
			
			Thread thread = new Thread(getMarketBoardCurrentlyShownViewsRunnable(homeDataCenterItemIdToCurrentlyShownViewMap, dataCenter, items.subList(from, to)));
			queryUniversalisAPI(threads, thread);
		}
		
		
		for(int i = 0; i < items.size(); i+= MAX_ITEMS_PER_QUERY) {
			try {
				Thread.currentThread().sleep(THREAD_CREATE_DELAY);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int from = i;
			int to = Math.min(i+MAX_ITEMS_PER_QUERY, items.size());

			System.err.println("Getting " + (to) + "/" + items.size() + " Current Listings from " + world);

			Thread thread = new Thread(getMarketBoardCurrentlyShownViewsRunnable(homeWorldItemIdToCurrentlyShownViewMap, world, items.subList(from, to)));
			
			queryUniversalisAPI(threads, thread);

			//this.worldCurrentListings.putAll(getMarketBoardListingsJSON(world, items.subList(from, to)));
		}
		
		for(int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.err.println("number of items in current listings: " + this.homeDataCenterItemIdToCurrentlyShownViewMap.size());
		System.err.println("number of items in sales history: " + this.homeWorldItemIdToHistoryViewMap.size());

	}

	private void queryUniversalisAPI(List<Thread> threads, Thread thread) {
		if(threads.size() < MAX_CONNECTIONS) {//start immediately
			threads.add(thread);
			thread.start();
			//System.err.println(thread.getName() + " STARTED");
		}
		else {
			//find any dead thread, remove it and start a new thread
			boolean isThreadAvaiable = false;
			while(isThreadAvaiable == false) {
				for(int j = 0; j<threads.size(); j++) {
					Thread currentThread = threads.get(j);
						if(currentThread.getState().toString().equals("TERMINATED")) {
							//System.err.println(threads.get(j).getName() + " " + threads.get(j).getState());
							threads.remove(j);
							threads.add(thread);
							thread.start();
							//System.err.println(thread.getName() + " STARTED");
							isThreadAvaiable = true;
							break;
						}
					}
					try {
						Thread.currentThread().sleep(2500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					
				}
			}
		}
	}
	
	private Runnable getMarketBoardHistoryViewsRunnable(Map<String, JSONObject> itemIdToHistoryViewMap, String worldOrDataCenter, List<String> itemIds) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				HashMap<String, JSONObject> itemIdToHistoryViewSubMap = getMarketBoardHistoryViews(worldOrDataCenter, itemIds);
				synchronized (itemIdToHistoryViewMap) {
					itemIdToHistoryViewMap.putAll(itemIdToHistoryViewSubMap);
				}
			}
		};
		
		return runnable;
	}
	private Runnable getMarketBoardCurrentlyShownViewsRunnable(Map<String, JSONObject> itemIdToCurrentlyShownViewMap, String worldOrDataCenter, List<String> itemIds) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				HashMap<String, JSONObject> itemIdToCurrentlyShownViewSubMap = getMarketBoardCurrentlyShownViews(worldOrDataCenter, itemIds);
				synchronized (itemIdToCurrentlyShownViewMap) {
					itemIdToCurrentlyShownViewMap.putAll(itemIdToCurrentlyShownViewSubMap);
				}
			}
		};
		
		return runnable;
	}
	
	public HashMap<String, JSONObject> getMarketBoardCurrentlyShownViews(String worldOrDataCenter, List<String> itemIds) {
		return getMarketBoardJsonEntity("", worldOrDataCenter, itemIds, 0);
	}
	
	public HashMap<String, JSONObject> getMarketBoardHistoryViews(String worldOrDataCenter, List<String> itemIds) {
		return getMarketBoardJsonEntity("history/", worldOrDataCenter, itemIds, 0);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, JSONObject> getMarketBoardJsonEntity(String type, String worldOrDataCenter, List<String> itemIds, Integer reconnectAttampts) {
		HashMap<String, JSONObject> result = new JSONObject();
		HttpURLConnection conn = null;
		if(reconnectAttampts < 5) {
			try {
				URL url = new URL("https://universalis.app/api/v2/" + type + worldOrDataCenter + "/" + String.join(",", itemIds));
				System.err.println(url.toString());
				if(!itemIds.isEmpty()) {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.connect();
					int responseCode = conn.getResponseCode();
					
					if(responseCode >= 500) {
						//wait 5 seconds and try again until response code is not 500
						while(responseCode >= 500) {
							conn.disconnect();
							System.err.println("Response code was " + responseCode + ". Waiting 5 seconds and trying again... (reconnect attemps: " + reconnectAttampts + ")");
							Thread.sleep(5000);
							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");			
							conn.connect();
							responseCode = conn.getResponseCode();
							if(responseCode == 200) {
								System.err.println("Succssesfully reconnected");
							}
						}
					}
						
					if(responseCode!=200) {
						throw new RuntimeException("HttpResponseCode: " + responseCode);
					}
					else {
						String line = "";
						Scanner scanner = null;
						try {
							scanner = new Scanner(url.openStream());
						}
						catch(IOException e) {
							Pattern p = Pattern.compile("(5\\d+)");
							Matcher m = p.matcher(e.getMessage());
							if(m.find()) {
								conn.disconnect();
								System.err.println("Response code was " + m.group(1) + ". Waiting 5 seconds and trying again... (reconnect attemps: " + reconnectAttampts + ")");
								Thread.sleep(5000);
								result = getMarketBoardJsonEntity(type, worldOrDataCenter, itemIds, reconnectAttampts + 1);
								return result;
							}
							else if(e.getMessage().contains("429")){
								conn.disconnect();
								System.err.println("Response code was 429. Waiting 5 seconds and trying again... (reconnect attemps: " + reconnectAttampts + ")");
								Thread.sleep(5000);
								result = getMarketBoardJsonEntity(type, worldOrDataCenter, itemIds , reconnectAttampts + 1);
								return result;
							}
							else {
								e.printStackTrace();
							}
						}
						while(scanner.hasNext()) {
							line += scanner.nextLine();
						}
	
						scanner.close();
						
						JSONParser jsonParser = new JSONParser();
						if(itemIds.size() == 1) {
							JSONObject dataObj = (JSONObject) jsonParser.parse(line);
							result.put(String.join(",", itemIds), dataObj);
						}
						else {
							JSONObject dataObj = (JSONObject) jsonParser.parse(line);
							JSONObject test1 = (JSONObject) dataObj.get("items");
							result = test1;
						}
					}
					
					conn.disconnect();
				}
			}
			catch(IOException | ParseException | RuntimeException | InterruptedException e) {
				conn.disconnect();
				e.printStackTrace();
			}
			finally {
				if(conn!=null) {
					conn.disconnect();
				}
			}
		}
		else {
			System.err.print("Maximum reconnect attempts reached, canceling");
		}
		return result;
	}
	
	public ArrayList<String> getMarketableItemIds(){
		ArrayList<String> result = new ArrayList<String>();
		
		for(Object itemId : getMarketableItemIdsJSON()) {
			result.add(String.valueOf(itemId));
		}
		
		return result;
	}
	
	public JSONArray getMarketableItemIdsJSON() {
		System.err.println("Getting All Marketable Item Ids");
		HttpURLConnection conn = null;

		JSONArray result = new JSONArray();
		try {
			URL url = new URL("https://universalis.app/api/marketable");
	
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			int responseCode = conn.getResponseCode();
						
			if(responseCode == 500) {
				//wait 5 seconds and try again until response code is not 500
				while(responseCode == 500) {
					conn.disconnect();
					System.err.println("Response code was 500. Waiting 5 seconds and trying again...");
					Thread.sleep(5000);
					conn.connect();
					responseCode = conn.getResponseCode();
				}
			}
			
			if(responseCode!=200) {
				throw new RuntimeException("HttpResponseCode: " + responseCode);
			}
			else {
				String line = "";
				Scanner scanner = new Scanner(url.openStream());
				
				while(scanner.hasNext()) {
					line += scanner.nextLine();
				}
				
				scanner.close();
				
				JSONParser jsonParser = new JSONParser();
				result = new JSONArray((List) jsonParser.parse(line));
			}
			
			conn.disconnect();
			
		}
		catch(IOException | ParseException | RuntimeException | InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			if(conn!=null) {
				conn.disconnect();
			}
		}
		return result;
	}

	
	public Map<String, JSONObject> getHomeDataCenterItemIdToCurrentlyShownViewMap() {
		return homeDataCenterItemIdToCurrentlyShownViewMap;
	}
	
	public JSONObject getHomeDataCenterCurrentlyShownViewByItemId(String itemId) {
		return homeDataCenterItemIdToCurrentlyShownViewMap.get(itemId);
	}
	
	
	public Map<String, JSONObject> getHomeWorldItemIdToCurrentlyShownViewMap() {
		return homeWorldItemIdToCurrentlyShownViewMap;
	}
	
	public JSONObject getHomeWorldCurrentlyShownViewByItemId(String itemId) {
		return homeWorldItemIdToCurrentlyShownViewMap.get(itemId);
	}

	
	public Map<String, JSONObject> getHomeWorldItemIdToHistoryViewMap() {
		return homeWorldItemIdToHistoryViewMap;
	}

	public JSONObject getHomeWorldHistoryViewByItemId(String itemId) {
		return homeWorldItemIdToHistoryViewMap.get(itemId);
	}

	
	public boolean isValidJSON(String itemId){
		if(getHomeDataCenterCurrentlyShownViewByItemId(itemId) == null) {
			return false;
		}
		
		if(getHomeWorldItemIdToHistoryViewMap().get(itemId) == null) {
			 return false;
		}
		
		return true;
	}
}
