package mal.lootbags.config;

import java.io.File;
import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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
		prop.setComment("This is the resolution of the bag drop chances.  Only change this if you want bags with rarity resolutions > 0.1%");
		LootBags.DROPRESOLUTION = prop.getInt();
		
		prop = config.get("Loot Categories", "ChestGenHooks Dropped", new String[]{LootTableList.CHESTS_SIMPLE_DUNGEON.toString(), LootTableList.CHESTS_ABANDONED_MINESHAFT.toString(), 
				LootTableList.CHESTS_DESERT_PYRAMID.toString(), LootTableList.CHESTS_JUNGLE_TEMPLE.toString(), LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER.toString(),
				LootTableList.CHESTS_STRONGHOLD_CORRIDOR.toString(), LootTableList.CHESTS_STRONGHOLD_CROSSING.toString(), LootTableList.CHESTS_STRONGHOLD_LIBRARY.toString(), 
				LootTableList.CHESTS_VILLAGE_BLACKSMITH.toString(), LootTableList.CHESTS_END_CITY_TREASURE.toString(), LootTableList.CHESTS_NETHER_BRIDGE.toString(), LootTableList.CHESTS_IGLOO_CHEST.toString()});
		prop.setComment("This is a list of the loot sources the bags pull from to generate the loot tables.  Probably a good idea to not mess with this unless you know what you're doing as entering in" +
				" a category that doesn't exist will simply make a new.");
		LootBags.LOOTCATEGORYLIST = prop.getStringList();
		
		prop = config.get("Blacklisted Items", "Global Blacklist", new String[]{""});
		prop.setComment("Adding a modid and internal item name to this list will remove the item from the general loot table.  " +
				"The entry must be in the form <modid>:<itemname>:<damage> on a single line or it won't work right.  Example to blacklist iron ingots: minecraft:iron_ingot:0.  An entire mod" +
				"can be blacklisted by just entering the modid and nothing else.");
		String[] blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			blacklistConfigData.add(blep[i]);
		
		prop = config.get("Whitelisted Items", "Global Whitelist", new String[]{});
		prop.setComment("Adding a modid and internal item name to this list will add the item to the Loot Bag drop table.  " +
				"The entry must be in the form <modid>:<itemname>:<damage>:<min stack size>:<max stack size>:<weighting>:[<nbt data (seriously don't try to make this by hand)> (optional)]" +
				"  Example to whitelist up to 16 iron ingots with a weight of 50: minecraft:iron_ingot:0:1:16:50.");
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			whitelistConfigData.add(blep[i]);
		
		prop = config.get("Recycler", "Item Blacklist", new String[]{});
		prop.setComment("Blacklist an item from being recyclable.  The entry must be in the form <modid>:<itemname>:<damage> on a single line or it won't work right.");
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			recyclerBlacklistConfigData.add(blep[i]);
		
		prop = config.get("Recycler", "Item Whitelist", new String[]{});
		prop.setComment("Whitelist an item to be recyclable.  The entry must be in the form <modid>:<itemname>:<damage>:<weighting>:[<nbt data (seriously don't try to make this by hand)> (optional)]  " +
				"The weight is as though the item was added to a bag, but the items whitelisted are not added to any loot bags.");
		blep = prop.getStringList();
		for(int i = 0; i < blep.length; i++)
			recyclerWhitelistConfigData.add(blep[i]);
		
/*		prop = config.get("Loot Categories", "Loot Bags in worldgen chests", new String[]{LootTableList.CHESTS_SIMPLE_DUNGEON.toString(), LootTableList.CHESTS_ABANDONED_MINESHAFT.toString(), 
				LootTableList.CHESTS_DESERT_PYRAMID.toString(), LootTableList.CHESTS_JUNGLE_TEMPLE.toString(), LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER.toString(),
				LootTableList.CHESTS_STRONGHOLD_CORRIDOR.toString(), LootTableList.CHESTS_STRONGHOLD_CROSSING.toString(), LootTableList.CHESTS_STRONGHOLD_LIBRARY.toString(), 
				LootTableList.CHESTS_VILLAGE_BLACKSMITH.toString(), LootTableList.CHESTS_END_CITY_TREASURE.toString(), LootTableList.CHESTS_NETHER_BRIDGE.toString(), LootTableList.CHESTS_IGLOO_CHEST.toString()});
		prop.setComment("This adds the loot bags to each of the loot tables listed.");
		LootBags.LOOTBAGINDUNGEONLOOT = prop.getStringList();*/
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Maximum Rerolls Allowed", 50);
		prop.setComment("If the bag encounters an item it cannot place in the bag for some reason, it will reroll until it gets an item" +
				" that will work, this sets a limit to the number of times the bag will" +
				" reroll before it just skips the slot.  Extremely high or low numbers may result in undesired performance of the mod.");
		LootBags.MAXREROLLCOUNT = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Bag Opener Cooldown", 100);
		prop.setComment("The cooldown in ticks that the bag opener needs before it'll process again.");
		LootBags.OPENERMAXCOOLDOWN = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Show Secret Bags", false);
		prop.setComment("This if true will show all the secret bags in creative inventory or item list mods.  Kind of ruins the fun if you ask me.");
		LootBags.SHOWSECRETBAGS = prop.getBoolean();

		prop = config.get(Configuration.CATEGORY_GENERAL, "Show Stored Bag Count", true);
		prop.setComment("This if true will show the number of stored bags in the Bag Storage to any method that queries items in slots. Certain mods may pull items out of blocks in such a way that causes dupe bugs, disabling this will remove them.");
		LootBags.STOREDCOUNT = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL,  "Total Loot Value to Create a New Bag", 1500);
		prop.setComment("This is kind of ambiguous, but essentially it's the value of loot stuff needed to fabricate new bags in the loot recycler.");
		LootBags.TOTALVALUE = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Disable Recycler Recipe", false);
		prop.setComment("Disables the loot recycler from being crafted.");
		LootBags.DISABLERECYCLER = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Disable Opener Recipe", false);
		prop.setComment("Disables the bag opener from being crafted.");
		LootBags.DISABLEOPENER = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Disable Storage Recipe", false);
		prop.setComment("Disables the bag storage from being crafted.");
		LootBags.DISABLESTORAGE = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Verbose Mode", false);
		prop.setComment("Setting this to false will disable many of the info messages, only showing errors in the log.");
		LootBags.VERBOSEMODE = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Debug Mode", false);
		prop.setComment("Setting this to true will display extra information about the contents of the bags and tables.");
		LootBags.DEBUGMODE = prop.getBoolean();

		prop = config.get(Configuration.CATEGORY_GENERAL, "Pipe Dupe Fix", false);
		prop.setComment("Setting this to true will enable a slightly inefficient fix for dupe bugs caused by certain naughty piping systems that show up when Show Stored Bag Count is enabled.");
		LootBags.MEKOVERRIDE = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Disable Enchantment Reduction", false);
		prop.setComment("Setting this to true will disable the weight reduction of enchanted items from imported tables.");
		LootBags.DISABLEENCHANTCUT = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Valid Kill Methods", "All");
		prop.setComment("Sources of entity death that are counted to determine if a bag can drop.  Allowable names: All, Player, Real.  All is any source of death, Player is any player entity including mod fake players, Real is only real players.");
		String method = prop.getString();
		if(method.equalsIgnoreCase("all"))
			LootBags.BAGFROMPLAYERKILL = 0;
		else if(method.equalsIgnoreCase("player"))
			LootBags.BAGFROMPLAYERKILL = 1;
		else if(method.equalsIgnoreCase("real"))
			LootBags.BAGFROMPLAYERKILL = 2;
		else
		{
			LootbagsUtil.LogError("Invalid death source: " + method + ".  Setting method to allow all sources.");
			LootBags.BAGFROMPLAYERKILL=0;
		}
		
/*		prop = config.get(Configuration.CATEGORY_GENERAL, "Bag Conversion Methods", "BOTH");
		prop.setComment("Sets the crafting recipes of bags into other bags.  Allowable names: UP, BOTH, DOWN, NONE.  UP only allows many bags into fewer bags, DOWN only allows few bags into many bags, BOTH allows for both, NONE disables bag conversion.");
		String craft = prop.getString();
		switch(craft.toUpperCase())
		{
			case "UP":
				LootBags.CRAFTTYPES=1;
				break;
			case "BOTH":
				LootBags.CRAFTTYPES=2;
				break;
			case "DOWN":
				LootBags.CRAFTTYPES=3;
				break;
			case "NONE":
				LootBags.CRAFTTYPES=0;
				break;
			default:
				LootbagsUtil.LogError("Invalid Conversion Name: " + craft + ".  Setting method to allow both types");
		}*/
		prop = config.get(Configuration.CATEGORY_GENERAL, "Limit bag drop to one bag per death", true);
		prop.setComment("This limits the loot bags to only drop one bag.  Bag weighting is dependant on drop chances.");
		LootBags.LIMITONEBAGPERDROP = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Bag ID used in Recycler", 0);
		prop.setComment("The bag ID (see the bag config) used in the recycler to fabricate new bags.");
		LootBags.RECYCLEDID = prop.getInt();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Prevent Merging Opened Bags", true);
		prop.setComment("If set to true, prevents bag storage from accepting opened bags.");
		LootBags.PREVENTMERGEDBAGS = prop.getBoolean();
		
		prop = config.get("Recycler", "Value Formula Multiplier", 0.75);
		prop.setComment("Multiplies the recycler value of an item in the Recycler, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the a variable.");
		LootBags.RECYCLERVALUENUMERATOR = prop.getDouble();
		
		prop = config.get("Recycler", "Stackable Formula Divider", 10.0);
		prop.setComment("Divides the recycler value of an item in the Recycler by this if the item stacks past one item, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the b variable.");
		LootBags.RECYCLERVALUESTACK = prop.getDouble();
		
		prop = config.get("Recycler", "Non-Stackable Formula Divider", 2.0);
		prop.setComment("Divides the recycler value of an item in the Recycler by this if the item does not stack past one item, in the formula (a*Total Value)/(Item Value*(isStackable)?(b):(c)), this is the c variable.");
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
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/