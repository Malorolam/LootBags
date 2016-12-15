package mal.lootbags.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.Level;

public class LootMap {

	private HashMap<String, LootItem> generalMap;
	private ArrayList<String> generalLootSources;
	private ArrayList<String> generalModBlacklist;
	private ArrayList<LootItem> generalBlacklist;
	private ArrayList<LootItem> generalWhitelist;
	
	public HashMap<String, LootItem> totalList;
	public ArrayList<LootItem> recyclerBlacklist;
	public ArrayList<LootItem> recyclerWhitelist;
	
	private int generalTotalWeight;
	
	public LootMap()
	{
		clearMapData();
	}
	
	public void clearMapData()
	{
		generalMap = new HashMap<String, LootItem>();
		generalLootSources = new ArrayList<String>();
		generalModBlacklist = new ArrayList<String>();
		generalBlacklist = new ArrayList<LootItem>();
		generalWhitelist = new ArrayList<LootItem>();
		recyclerBlacklist = new ArrayList<LootItem>();
		recyclerWhitelist = new ArrayList<LootItem>();
		
		totalList = new HashMap<String, LootItem>();
		
		generalTotalWeight = 0;
	}
	
	/*
	 * Populate the recycler blacklist
	 */
	public void populateRecyclerBlacklist(ArrayList<String> configRecyclerBlacklist)
	{
		for(String s:configRecyclerBlacklist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] tempwords = trim.split("(?<!$):");
				if(tempwords.length==3)//correct length for a standard blacklist item
				{
					try {
						String modid = tempwords[0];
						String itemname = tempwords[1];
						ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
						
						for(Integer dam: itemdamage)
						{
							LootItem item = new LootItem(modid, itemname, dam, 1, 1, 1, false);
							recyclerBlacklist.add(item);
							LootbagsUtil.LogInfo("Added General Recycler Blacklist Item: " + item.toString());
						}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Recycler Blacklist Error: Line: " + s + " Improperly formed Blacklisted item causing exception.");
						e.printStackTrace();
					}
				}
				else
				{
					LootbagsUtil.LogError("General Recycler Blacklist Error: Line: " + s + " Improperly formed Blacklisted item.");
				}
			}
		}
	}
	
	/*
	 * Populate the recycler whitelist from the general config
	 */
	public void populateRecyclerWhitelist(ArrayList<String> configRecyclerWhitelist)
	{
		for(String s:configRecyclerWhitelist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] tempwords = trim.split("(?<!$):");
				if(tempwords.length==4)//correct length for a standard whitelist item
				{
					try {
					String modid = tempwords[0];
					String itemname = tempwords[1];
					ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
					int weight = Integer.parseInt(tempwords[3]);
					
						for(Integer dam: itemdamage)
						{
							LootItem item = new LootItem(modid, itemname, dam, 1, 1, weight, false);
							recyclerWhitelist.add(item);
							LootbagsUtil.LogInfo("Added General Recycler Whitelist Item: " + item.toString());
						}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Recycler Whitelist Error: Line: " + s + " Improperly formed Whitelisted item causing exception.");
						e.printStackTrace();
					}
				}
				else if(tempwords.length==5)//length including NBT data
				{
					try {
					String modid = tempwords[0];
					String itemname = tempwords[1];
					ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
					int weight = Integer.parseInt(tempwords[3]);
					byte[] nbt = LootbagsUtil.parseNBTArray(tempwords[4]);
					
					for(Integer dam: itemdamage)
					{
						LootItem item = new LootItem(modid, itemname, dam, 1, 1, weight, nbt, false);
						generalWhitelist.add(item);
						LootbagsUtil.LogInfo("Added General Recycler Whitelist Item with NBT: " + item.toString());
					}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Recycler Whitelist Error: Line: " + s + " Improperly formed NBT Whitelisted item causing exception.");
						e.printStackTrace();
					}
				}
				else
				{
					LootbagsUtil.LogError("General Recycler Whitelist Error: Line: " + s + " Improperly formed Whitelisted item.");
				}
			}
		}
	}
	
	/*
	 * Populates the general black list from the general config
	 */
	public void populateGeneralBlacklist(ArrayList<String> configBlacklist)
	{
		for(String s:configBlacklist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] tempwords = trim.split("(?<!$):");
				if(tempwords.length==1)//correct length for a mod blacklist
				{
					if(!generalModBlacklist.contains(tempwords[0]))
						generalModBlacklist.add(tempwords[0]);
				}
				else if(tempwords.length==3)//correct length for a standard blacklist item
				{
					try {
						String modid = tempwords[0];
						String itemname = tempwords[1];
						ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
						
						for(Integer dam: itemdamage)
						{
							LootItem item = new LootItem(modid, itemname, dam, 1, 1, 1, false);
							generalBlacklist.add(item);
							LootbagsUtil.LogInfo("Added General Blacklist Item: " + item.toString());
						}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Blacklist Error: Line: " + s + " Improperly formed Blacklisted item causing exception.");
						e.printStackTrace();
					}
				}
				else
				{
					LootbagsUtil.LogError("General Blacklist Error: Line: " + s + " Improperly formed Blacklisted item.");
				}
			}
		}
	}
	
	/*
	 * Populate the general whitelist
	 */
	public void populateGeneralWhitelist(ArrayList<String> configWhitelist)
	{
		for(String s:configWhitelist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] tempwords = trim.split("(?<!$):");
				if(tempwords.length==6)//correct length for a standard whitelist item
				{
					try {
					String modid = tempwords[0];
					String itemname = tempwords[1];
					ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
					int minstack = Integer.parseInt(tempwords[3]);
					int maxstack = Integer.parseInt(tempwords[4]);
					int weight = Integer.parseInt(tempwords[5]);
					
						for(Integer dam: itemdamage)
						{
							LootItem item = new LootItem(modid, itemname, dam, minstack, maxstack, weight, false);
							generalWhitelist.add(item);
							LootbagsUtil.LogInfo("Added General Whitelist Item: " + item.toString());
						}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Whitelist Error: Line: " + s + " Improperly formed Whitelisted item causing exception.");
						e.printStackTrace();
					}
				}
				else if(tempwords.length==7)//length including NBT data
				{
					try {
					String modid = tempwords[0];
					String itemname = tempwords[1];
					ArrayList<Integer> itemdamage = LootbagsUtil.constructDamageRange(tempwords[2]);
					int minstack = Integer.parseInt(tempwords[3]);
					int maxstack = Integer.parseInt(tempwords[4]);
					int weight = Integer.parseInt(tempwords[5]);
					byte[] nbt = LootbagsUtil.parseNBTArray(tempwords[6]);
					
					for(Integer dam: itemdamage)
					{
						LootItem item = new LootItem(modid, itemname, dam, minstack, maxstack, weight, nbt, false);
						generalWhitelist.add(item);
						LootbagsUtil.LogInfo("Added General Whitelist Item with NBT: " + item.toString());
					}
					} catch(Exception e) {
						LootbagsUtil.LogError("General Whitelist Error: Line: " + s + " Improperly formed NBT Whitelisted item causing exception.");
						e.printStackTrace();
					}
				}
				else
				{
					LootbagsUtil.LogError("General Whitelist Error: Line: " + s + " Improperly formed Whitelisted item.");
				}
			}
		}
	}
	
	/*
	 * Populates the general map from the global loot sources, whitelist, and blacklist
	 */
	public void populateGeneralMap()
	{
		//loot sources
		for(String source: generalLootSources)
		{
			addLootCategory(source);
		}
		
		//whitelist
		for(LootItem item: generalWhitelist)
		{
			if(item.getContentItem()==null)
				item.reinitializeLootItem();
			if(item.getContentItem()==null)
				LootbagsUtil.LogError("Loot Item with information: " + item.getItemModID() + ":" + item.getItemName() + " does not exist, even after reinitilizing it. This typically means the whitelist entry is wrong. This item will be skipped.");
			else
			{
				String key = item.getItemModID()+item.getItemName()+item.getContentItem().theItemId.getItemDamage();
				if(item.getContentItem().theItemId.getItem() instanceof ItemEnchantedBook && item.getContentItem().theItemId.hasTagCompound())//a specific enchanted book
					key += item.getContentItem().theItemId.getTagCompound().toString();
				if(generalMap.containsKey(key))
					generalMap.remove(key);//remove the existing entry to overwrite it with the whitelisted version
				
				generalMap.put(key, item);
				
				if(!totalList.containsKey(key))
					totalList.put(key, item);
			}
		}
	}
	
	/**
	 * Add every item in a category to the map calculating the drop chance
	 * @param categoryName
	 */
	public void addLootCategory(String categoryName)
	{
		LootbagsUtil.LogInfo("Starting adding items from loot category: " + categoryName + ".");
		WeightedRandomChestContent[] h = ChestGenHooks.getItems(categoryName, LootBags.getRandom());
		for(int i = 0; i < h.length; i++)
		{
			WeightedRandomChestContent c = h[i];
			
			LootItem item;
			boolean skip = false;
			try {
				item = new LootItem(c, true);
			} catch (Exception e) {
				item = null;
				skip = true;
				LootbagsUtil.LogError("Creation of Loot Item failed!  This is an issue with a different mod!");
			}
			if(!skip)
			{
				//Do some fixing to prevent <1 errors in the bags
				if(item.getContentItem().itemWeight < 1)
				{
					LootbagsUtil.LogError("Item " + item.getContentItem().theItemId.toString() + " has a weighting of " + item.getContentItem().itemWeight + ".  This is not a Lootbags error but an error in a different mod!  "
							+ "This item will be excluded from the Lootbags loot table.");
	//				ChestGenHooks.removeItem(categoryName, c.theItemId);
				}
				else
				{
				
					for(String modid:generalModBlacklist)
					{
						if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))//if(GameData.getItemRegistry().getNameForObject(c.theItemId.getItem()).getResourceDomain().equalsIgnoreCase(modid))
						{
							skip = true;
							LootbagsUtil.LogInfo("Found item to skip from Blacklisted mod: " + item.toString());
						}
					}
					for(LootItem entry:generalBlacklist)
					{
						String name = entry.getItemName();
						if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).name.equalsIgnoreCase(name))//if(GameData.getItemRegistry().getNameForObject(c.theItemId.getItem()).getResourcePath().equalsIgnoreCase(name))
						{
							skip = true;
							LootbagsUtil.LogInfo("Found Blacklisted item to skip: " + item.toString());
						}
					}
					if(!skip)
					{
						
						String key = item.getItemModID()+item.getItemName()+item.getContentItem().theItemId.getItemDamage();
						
						if(!generalMap.containsKey(key))
						{
							generalMap.put(key, item);
							//LootbagsUtil.LogInfo("Added new General Item: " + item.toString());
							if(!totalList.containsKey(key))
								totalList.put(key, item);
						}
						else
						{
							LootItem it = generalMap.get(key);
							int weight = it.getContentItem().itemWeight;
							weight = (weight+c.itemWeight)/2;
							it.getContentItem().itemWeight = weight;
							generalMap.put(key, it);
							//LootbagsUtil.LogInfo("Merged new General Item: " + item.toString());
							if(!totalList.containsKey(key))
								totalList.put(key, it);
						}
					}
				}
			}
		}
	}
	
	/*
	 * Get a subset of the map sorted by weight
	 */
	public ArrayList<LootItem> getMapByWeight(int minWeight, int maxWeight)
	{
		ArrayList<LootItem> ret = new ArrayList<LootItem>();
		
		//populate the list
		for(LootItem item:generalMap.values())
		{
			if((minWeight == -1 || item.getItemWeight() >= minWeight) && (maxWeight == -1 || item.getItemWeight() <= maxWeight))
			{
				ret.add(item);
			}
		}
		
		//sort the list
		Collections.sort(ret);
		
		return ret;
	}
	
	public HashMap<String, LootItem> getMap()
	{
		return generalMap;
	}
	
	public void setTotalListWeight()
	{
		generalTotalWeight = 0;
		for(LootItem item: totalList.values())
		{
			//LootbagsUtil.LogInfo(item.toString());
			generalTotalWeight += item.getItemWeight();
		}
		for(LootItem item: recyclerWhitelist)
			generalTotalWeight += item.getItemWeight();
	}
	
	public int getTotalListWeight()
	{
		//LootbagsUtil.LogInfo("weight: " + generalTotalWeight);
		return generalTotalWeight;
	}
	
	public void setLootSources(String[] sources)
	{
		for(int i = 0; i < sources.length; i++)
			generalLootSources.add(sources[i]);
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/