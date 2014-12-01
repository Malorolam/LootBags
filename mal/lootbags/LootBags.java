package mal.lootbags;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;

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
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = LootBags.MODID, version = LootBags.VERSION)
public class LootBags {
	public static final String MODID = "lootbags";
	public static final String VERSION = "1.0";

	public static int MONSTERDROPCHANCE = 40;
	public static int PASSIVEDROPCHANCE = 20;
	public static int PLAYERDROPCHANCE = 5;
	
	public static String[] LOOTCATEGORYLIST = null;
	public static ArrayList<ItemStack> LOOTBLACKLIST = new ArrayList<ItemStack>();
	public static ArrayList<ItemStack> LOOTWHITELIST = new ArrayList<ItemStack>();
	
	public static boolean LOOTBAGINDUNGEONLOOT;
	
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
		
		Property prop2 = config.get("Loot Categories", "ChestGenHooks",  new String[]{ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, 
				ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.PYRAMID_JUNGLE_DISPENSER,
				ChestGenHooks.STRONGHOLD_CORRIDOR, ChestGenHooks.STRONGHOLD_CROSSING, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.VILLAGE_BLACKSMITH});
		prop2.comment = "This is a list of all Forge ChestGenHooks for different loot sources.  Probably a good idea to not mess with this unless you know what you're doing.";
		LOOTCATEGORYLIST = prop2.getStringList();
		
		Property prop3 = config.get("Blacklist", "Blacklisted Items", new String[]{"lootbags itemlootbag 0"});
		prop3.comment = "Adding a modid and internal item name or Ore Dictionary name to this list will prevent the bag from dropping the item.  Tries for Ore Dictionary before trying through the modlist." +
				"The modlist must be in the form <modid> <itemname> <damage> on a single line or it won't work right.  Examples (iron ingot): minecraft iron_ingot 0 <OR> ingotIron";
		blacklistlist = prop3.getStringList();
		
		Property prop4 = config.get("Whitelist", "Whitelisted Items", new String[]{});
		prop4.comment = "Adding a modid and internal item name or Ore Dictionary name to this list will add the item to the Loot Bag drop table.  Examples (iron ingot): minecraft iron_ingot 0 <OR> ingotIron";
		whitelistlist = prop4.getStringList();
		
		Property prop5 = config.get(Configuration.CATEGORY_GENERAL, "Loot Bags in worldgen chests", true);
		prop5.comment = "This adds the loot bags to the vanilla chest loot lists if true.";
		LOOTBAGINDUNGEONLOOT = prop5.getBoolean();
		
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
	}

	@EventHandler
	public void Init(FMLInitializationEvent event) {
		GameRegistry.registerItem(lootbag, "itemlootbag");
		
		if(LOOTBAGINDUNGEONLOOT)
		{
			WeightedRandomChestContent con = new WeightedRandomChestContent(new ItemStack(lootbag), 0, 1, 65);
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, con);
			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, con);
			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, con);
			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, con);
			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER, con);
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, con);
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, con);
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, con);
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
			if(!OreDictionary.getOres(s).isEmpty())
			{
				FMLLog.log(Level.INFO, "Added Whitelist item from OreDictionary: " + s);
				LOOTWHITELIST.add(OreDictionary.getOres(s).get(0));
			}
			else
			{
				String trim = s.trim();
				if(!trim.isEmpty())
				{
					String[] words = trim.split("\\s+");
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
							FMLLog.log(Level.INFO, "Added Whitelist item: " + stack.toString());
							LOOTWHITELIST.add(stack);
						}
					}
				}
			}
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
