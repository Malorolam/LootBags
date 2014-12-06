package mal.lootbags;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import mal.lootbags.handler.ItemDumpCommand;
import mal.lootbags.handler.MobDropHandler;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = LootBags.MODID, version = LootBags.VERSION)
public class LootBags {
	public static final String MODID = "lootbags";
	public static final String VERSION = "1.2.0";

	public static int MONSTERDROPCHANCE = 40;
	public static int PASSIVEDROPCHANCE = 20;
	public static int PLAYERDROPCHANCE = 5;
	
	public static int MAXREROLLCOUNT = 50;
	
	public static String[] LOOTCATEGORYLIST = null;
	public static ArrayList<ItemStack> LOOTBLACKLIST = new ArrayList<ItemStack>();
	public static ArrayList<String> MODBLACKLIST = new ArrayList<String>();
	public static ArrayList<ItemStack> LOOTWHITELIST = new ArrayList<ItemStack>();
	public static ArrayList<Integer> WHITELISTCHANCE = new ArrayList<Integer>();
	
	public static String[] LOOTBAGINDUNGEONLOOT;
	
	private String[] blacklistlist;
	private String[] whitelistlist;
	
	@SidedProxy(clientSide="mal.lootbags.network.ClientProxy", serverSide="mal.lootbags.network.CommonProxy")
	public static CommonProxy prox;

	public static LootbagItem lootbag = new LootbagItem();

	@Instance(value = LootBags.MODID)
	public static LootBags LootBagsInstance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MobDropHandler handler = new MobDropHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		NetworkRegistry.INSTANCE.registerGuiHandler(LootBagsInstance, prox);
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		Property prop = config.get(Configuration.CATEGORY_GENERAL, "Monster Drop Chance 0-100", 40);
		prop.comment = "This controls the drop chance for monsters, passive mobs, and players.";
		MONSTERDROPCHANCE = prop.getInt();
		PASSIVEDROPCHANCE = config.get(Configuration.CATEGORY_GENERAL, "Passive Mob Drop Chance 0-100", 20).getInt();
		PLAYERDROPCHANCE = config.get(Configuration.CATEGORY_GENERAL, "Player Drop Chance 0-100", 10).getInt();
		
		Property prop2 = config.get("Loot Categories", "ChestGenHooks Dropped",  new String[]{ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, 
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER,
				ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH});
		prop2.comment = "This is a list of all Forge ChestGenHooks for different loot sources.  Probably a good idea to not mess with this unless you know what you're doing.";
		LOOTCATEGORYLIST = prop2.getStringList();
		
		Property prop3 = config.get("Blacklist", "Blacklisted Items", new String[]{"lootbags itemlootbag 0"});
		prop3.comment = "Adding a modid and internal item name or Ore Dictionary name to this list will prevent the bag from dropping the item.  Tries for Ore Dictionary before trying through the modlist." +
				"The modlist must be in the form <modid> <itemname> <damage> on a single line or it won't work right.  Example to blacklist iron ingots: minecraft iron_ingot 0 <OR> ingotIron.  An entire mod" +
				"can be blacklisted by just entering a modid.";
		blacklistlist = prop3.getStringList();
		
		Property prop4 = config.get("Whitelist", "Whitelisted Items", new String[]{});
		prop4.comment = "Adding a modid and internal item name or Ore Dictionary name to this list will add the item to the Loot Bag drop table.  Example to whitelist up to 16 iron ingots with a 50%" +
				" chance to spawn: minecraft iron_ingot 0 16 50 <OR> ingotIron 16 50";
		whitelistlist = prop4.getStringList();
		
		Property prop5 = config.get("Loot Categories", "Loot Bags in worldgen chests", new String[]{ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, 
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER,
				ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH});
		prop5.comment = "This adds the loot bags to each of the loot tables listed.";
		LOOTBAGINDUNGEONLOOT = prop5.getStringList();
		
		Property prop6 = config.get(Configuration.CATEGORY_GENERAL, "Maximum Rerolls Allowed", 50);
		prop6.comment = "If the bag encounters an item it cannot place in the bag it will reroll, this sets a limit to the number of times the bag will" +
				" reroll before it just skips the slot.  Extremely high or low numbers may result in undesired performance of the mod.";
		MAXREROLLCOUNT = prop6.getInt();
		
		config.save();
		
		if(MONSTERDROPCHANCE<0)
		{
			FMLLog.log(Level.WARN, "Monster drop chance cannot be below 0%, adjusting to 0%");
			MONSTERDROPCHANCE=0;
		}
		else if(MONSTERDROPCHANCE>100)
		{
			FMLLog.log(Level.WARN, "Monster drop chance cannot be above 100%, adjusting to 100%");
			MONSTERDROPCHANCE=100;
		}
		
		if(PASSIVEDROPCHANCE<0)
		{
			FMLLog.log(Level.WARN, "Passive Mob drop chance cannot be below 0%, adjusting to 0%");
			PASSIVEDROPCHANCE=0;
		}
		else if(PASSIVEDROPCHANCE>100)
		{
			FMLLog.log(Level.WARN, "Passive Mob drop chance cannot be above 100%, adjusting to 100%");
			PASSIVEDROPCHANCE=100;
		}
		
		if(PLAYERDROPCHANCE<0)
		{
			FMLLog.log(Level.WARN, "Player drop chance cannot be below 0%, adjusting to 0%");
			PLAYERDROPCHANCE=0;
		}
		else if(PLAYERDROPCHANCE>100)
		{
			FMLLog.log(Level.WARN, "Player drop chance cannot be above 100%, adjusting to 100%");
			PLAYERDROPCHANCE=100;
		}
		
		if(LOOTCATEGORYLIST.length<=0)
		{
			FMLLog.log(Level.WARN, "Drop tables must contain at least one ChestGenHook, adding DUNGEON_CHEST as a default.");
			LOOTCATEGORYLIST = new String[]{ChestGenHooks.DUNGEON_CHEST};
		}
		
		if(MAXREROLLCOUNT<=0)
		{
			FMLLog.log(Level.WARN, "Reroll count has to be at least 1 (fancy error prevention stuff)");
			MAXREROLLCOUNT=1;
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		GameRegistry.registerItem(lootbag, "itemlootbag");
		
		if(LOOTBAGINDUNGEONLOOT.length>0)
		{
			WeightedRandomChestContent con = new WeightedRandomChestContent(new ItemStack(lootbag), 0, 1, 45);
			for(String s:LOOTBAGINDUNGEONLOOT)
				ChestGenHooks.addItem(s, con);
		}
		
		for(String s: blacklistlist)
		{
			if(!OreDictionary.getOres(s).isEmpty())
			{
				FMLLog.log(Level.INFO, "Added Blacklist items from OreDictionary: " + s);
				LOOTBLACKLIST.addAll(OreDictionary.getOres(s));
			}
			else
			{
				String trim = s.trim();
				if(!trim.isEmpty())
				{
					String[] words = trim.split("\\s+");
					if(words.length == 1)
					{
						if(Loader.isModLoaded(words[0]) || words[0].equalsIgnoreCase("minecraft"))
						{
							MODBLACKLIST.add(words[0]);
							FMLLog.log(Level.INFO, "Blacklisted Mod with ID: " + words[0] + ".");
						}
					}
					if(words.length == 3)
					{
						ItemStack stack = null;
						//one of these should be not null
						Block block = GameRegistry.findBlock(words[0], words[1]);
						Item item = GameRegistry.findItem(words[0], words[1]);
						if(item != null)
							stack = new ItemStack(item,1,Integer.parseInt(words[2]));
						if(block != null)
							stack = new ItemStack(block,1,Integer.parseInt(words[2]));
						if(stack != null)
						{
							FMLLog.log(Level.INFO, "Added Blacklist item: " + stack.toString());
							LOOTBLACKLIST.add(stack);
						}
						
					}
				}
			}
		}

		for(String s: whitelistlist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] words = trim.split("\\s+");
				if(words.length == 3)
				{
					if(!OreDictionary.getOres(words[0]).isEmpty())
					{
						FMLLog.log(Level.INFO, "Added Whitelist item from OreDictionary: " + words[0] + "x" + words[1]);
						ItemStack is = OreDictionary.getOres(words[0]).get(0).copy();
						is.stackSize=Integer.parseInt(words[1]);
						LOOTWHITELIST.add(is);
						WHITELISTCHANCE.add(Integer.parseInt(words[2]));
					}
				}
				if(words.length == 5)
				{
					ItemStack stack = null;
					//one of these should be not null
					Block block = GameRegistry.findBlock(words[0], words[1]);
					Item item = GameRegistry.findItem(words[0], words[1]);
					if(item != null)
						stack = new ItemStack(item,Integer.parseInt(words[3]),Integer.parseInt(words[2]));
					if(block != null)
						stack = new ItemStack(block,Integer.parseInt(words[3]),Integer.parseInt(words[2]));
					if(stack != null)
					{
						FMLLog.log(Level.INFO, "Added Whitelist item: " + stack.toString());
						LOOTWHITELIST.add(stack);
						WHITELISTCHANCE.add(Integer.parseInt(words[4]));
					}
				}
			}
		}
			
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ItemDumpCommand());
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
