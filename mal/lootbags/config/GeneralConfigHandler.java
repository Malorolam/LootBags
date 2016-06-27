package mal.lootbags.config;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import mal.lootbags.LootBags;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class GeneralConfigHandler {

	private static ArrayList<String> blacklistConfigData = new ArrayList<String>();
	private static ArrayList<String> whitelistConfigData = new ArrayList<String>();
	private static ArrayList<String> recyclerBlacklistConfigData = new ArrayList<String>();
	private static ArrayList<String> recyclerWhitelistConfigData = new ArrayList<String>();
	private static File configfile;
	
	public static void loadConfig(FMLPreInitializationEvent event)
	{
		configfile = event.getSuggestedConfigurationFile();
		reloadConfig();
	}
	
	public static void reloadConfig()
	{
		Configuration config = new Configuration(configfile);
		config.load();
		
		Property prop = config.get("Drop Chances", "1 Weighting Resolution", 1000);
		prop.comment = "This is the resolution of the bag drop chances.  Only change this if you want bags with rarity resolutions > 0.1%";
		LootBags.DROPRESOLUTION = prop.getInt();
		
		prop = config.get("Loot Categories", "ChestGenHooks Dropped",  new String[]{ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, 
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER,
				ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH});
		prop.comment = "This is a list of the loot sources the bags pull from to generate the loot tables.  Probably a good idea to not mess with this unless you know what you're doing as entering in" +
				" a category that doesn't exist will simply make a new.";
		LootBags.LOOTCATEGORYLIST = prop.getStringList();
		prop = config.get("Loot Categories", "Chest Drop Weight", 20);
		prop.comment = "This is the weighting of the bags in any of the worldgen chests.";
		LootBags.CHESTQUALITYWEIGHT = prop.getInt();
		
		prop = config.get("Blacklisted Items", "Global Blacklist", new String[]{""});
		prop.comment = "Adding a modid and internal item name to this list will remove the item from the general loot table.  " +
				"The entry must be in the form <modid>:<itemname>:<damage> on a single line or it won't work right.  Example to blacklist iron ingots: minecraft:iron_ingot:0.  An entire mod" +
				"can be blacklisted by just entering the modid and nothing else.";
		String[] blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			blacklistConfigData.add(blep[i]);
		
		prop = config.get("Whitelisted Items", "Global Whitelist", new String[]{});
		prop.comment = "Adding a modid and internal item name to this list will add the item to the Loot Bag drop table.  " +
				"The entry must be in the form <modid>:<itemname>:<damage>:<min stack size>:<max stack size>:<weighting>:[<nbt data (seriously don't try to make this by hand)> (optional)]" +
				"  Example to whitelist up to 16 iron ingots with a weight of 50: minecraft:iron_ingot:0:1:16:50.";
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			whitelistConfigData.add(blep[i]);
		
		prop = config.get("Recycler", "Item Blacklist", new String[]{});
		prop.comment = "Blacklist an item from being recyclable.  The entry must be in the form <modid>:<itemname>:<damage> on a single line or it won't work right.";
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			recyclerBlacklistConfigData.add(blep[i]);
		
		prop = config.get("Recycler", "Item Whitelist", new String[]{});
		prop.comment = "Whitelist an item to be recyclable.  The entry must be in the form <modid>:<itemname>:<damage>:<weighting>:[<nbt data (seriously don't try to make this by hand)> (optional)]  " +
				"The weight is as though the item was added to a bag, but the items whitelisted are not added to any loot bags.";
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			recyclerWhitelistConfigData.add(blep[i]);
		
		prop = config.get("Loot Categories", "Loot Bags in worldgen chests", new String[]{ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, 
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER,
				ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH});
		prop.comment = "This adds the loot bags to each of the loot tables listed.";
		LootBags.LOOTBAGINDUNGEONLOOT = prop.getStringList();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Maximum Rerolls Allowed", 50);
		prop.comment = "If the bag encounters an item it cannot place in the bag for some reason, it will reroll until it gets an item" +
				" that will work, this sets a limit to the number of times the bag will" +
				" reroll before it just skips the slot.  Extremely high or low numbers may result in undesired performance of the mod.";
		LootBags.MAXREROLLCOUNT = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Show Secret Bags", false);
		prop.comment = "This if true will show all the secret bags in creative inventory or item list mods.  Kind of ruins the fun if you ask me.";
		LootBags.SHOWSECRETBAGS = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL,  "Total Loot Value to Create a New Bag Multiplier", 1.0);
		prop.comment = "This is kind of ambiguous, but essentially it's a global multiplier to adjust the cost to fabricate new bags in the loot recycler.";
		LootBags.TOTALVALUEMULTIPLIER = prop.getDouble();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Disable Recycler Recipe", false);
		prop.comment = "Disables the loot recycler from being crafted.";
		LootBags.DISABLERECYCLER = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Valid Kill Methods", "All");
		prop.comment = "Sources of entity death that are counted to determine if a bag can drop.  Allowable names: All, Player, Real.  All is any source of death, Player is any player entity including mod fake players, Real is only real players.";
		String method = prop.getString();
		if(method.equalsIgnoreCase("all"))
			LootBags.BAGFROMPLAYERKILL = 0;
		else if(method.equalsIgnoreCase("player"))
			LootBags.BAGFROMPLAYERKILL = 1;
		else if(method.equalsIgnoreCase("real"))
			LootBags.BAGFROMPLAYERKILL = 2;
		else
		{
			FMLLog.log(Level.WARN, "Invalid death source: " + method + ".  Setting method to allow all sources.");
			LootBags.BAGFROMPLAYERKILL=0;
		}
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Limit bag drop to one bag per death", true);
		prop.comment = "This limits the loot bags to only drop one bag.  Bag weighting is dependant on drop chances.";
		LootBags.LIMITONEBAGPERDROP = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Bag ID used in Recycler", 0);
		prop.comment = "The bag ID (see the bag config) used in the recycler to fabricate new bags.";
		LootBags.RECYCLEDID = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Prevent Merging Opened Bags", false);
		prop.comment = "If set to true, prevents bag crafting from working if any of the bags have been opened.";
		LootBags.PREVENTMERGEDBAGS = prop.getBoolean();
		
		prop = config.get("Recycler", "Value Formula Multiplier", 2.0);
		prop.comment = "Multiplies the recycler value of an item in the Recycler, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the a variable.";
		LootBags.RECYCLERVALUENUMERATOR = prop.getDouble();
		
		prop = config.get("Recycler", "Stackable Formula Divider", 8.0);
		prop.comment = "Divides the recycler value of an item in the Recycler by this if the item stacks past one item, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the b variable.";
		LootBags.RECYCLERVALUESTACK = prop.getDouble();
		
		prop = config.get("Recycler", "Non-Stackable Formula Divider", 1.0);
		prop.comment = "Divides the recycler value of an item in the Recycler by this if the item does not stack past one item, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the c variable.";
		LootBags.RECYCLERVALUENONSTACK = prop.getDouble();
		
		config.save();
	}
	
	public static ArrayList<String> getBlacklistConfigData()
	{
		return blacklistConfigData;
	}
	
	public static ArrayList<String> getWhitelistConfigData()
	{
		return whitelistConfigData;
	}

	public static ArrayList<String> getRecyclerBlacklistConfigData() {
		return recyclerBlacklistConfigData;
	}

	public static ArrayList<String> getRecyclerWhitelistConfigData() {
		return recyclerWhitelistConfigData;
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/