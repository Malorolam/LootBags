package mal.lootbags;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import mal.lootbags.handler.MobDropHandler;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.CommonProxy;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
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
	public static final String VERSION = "0.5";

	public static int MONSTERDROPCHANCE = 40;
	public static int PASSIVEDROPCHANCE = 20;
	public static int PLAYERDROPCHANCE = 5;
	
	public static String[] LOOTCATEGORYLIST = null;
	
	
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
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
