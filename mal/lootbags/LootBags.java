package mal.lootbags;

import java.util.HashMap;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import mal.lootbags.blocks.BlockOpener;
import mal.lootbags.blocks.BlockRecycler;
import mal.lootbags.blocks.BlockStorage;
import mal.lootbags.config.BagConfigHandler;
import mal.lootbags.config.GeneralConfigHandler;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.handler.ConfigReloadCommand;
import mal.lootbags.handler.GUIHandler;
import mal.lootbags.handler.ItemDumpCommand;
import mal.lootbags.handler.LootSourceCommand;
import mal.lootbags.handler.MobDropHandler;
import mal.lootbags.handler.NBTPullCommand;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.item.RecyclerItemBlock;
import mal.lootbags.item.StorageItemBlock;
import mal.lootbags.jei.LootRegistry;
import mal.lootbags.loot.LootItem;
import mal.lootbags.loot.LootMap;
import mal.lootbags.network.CommonProxy;
import mal.lootbags.network.LootbagsPacketHandler;
import mal.lootbags.tileentity.TileEntityOpener;
import mal.lootbags.tileentity.TileEntityRecycler;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = LootBags.MODID, version = LootBags.VERSION, dependencies="after:mystcraft")
public class LootBags {
	public static final String MODID = "lootbags";
	public static final String VERSION = "2.5.3";
	
	public static int SPECIALDROPCHANCE = 250;
	
	public static int DROPRESOLUTION = 1000;
	
	public static int CHESTQUALITYWEIGHT = 20;
	
	public static Logger LOOTLOG;
	
	public static boolean REVERSEQUALITY = true;//reverses the quality to determine what can be dropped from a bag
	
	public static boolean SHOWSECRETBAGS = true;//shows the secret bags in NEI/creative inventory
	private static boolean HASLOADED = false;//if the table has been loaded or not yet
	public static boolean VERBOSEMODE = true;//controls a lot of the non-critical log messages
	public static boolean DEBUGMODE = false;//controls even more messages displaying in the log
	
	public static final int MINCHANCE = 0;
	public static final int MAXCHANCE = 1000;
	
	public static boolean LIMITONEBAGPERDROP = false;
	public static int BAGFROMPLAYERKILL = 2;//limit bag drops to only EntityPlayer kills, 0 is any source, 1 is EntityPlayer, 2 is forced real players
	public static int MINITEMSDROPPED = 1;//minimum number of items dropped by a bag
	public static int MAXITEMSDROPPED = 5;//maximum number of items dropped by a bag
	
	public static int MAXREROLLCOUNT = 50;
	public static int TOTALVALUE = 1000;//multiplier for the total value to fabricate the bag
	public static int RECYCLEDID = 0;//bag id of the bag the recycler creates
	
	public static double RECYCLERVALUENUMERATOR = 2.0;
	public static double RECYCLERVALUENONSTACK = 1.0;
	public static double RECYCLERVALUESTACK = 8.0;
	
	public static int OPENERMAXCOOLDOWN = 100;
	
	public static boolean PREVENTMERGEDBAGS = false;
	//public static byte CRAFTTYPES = 1;//crafting controlling, 1 is both types, 0 is only upconvert, 2 is only downconvert
	
	public static String[] LOOTCATEGORYLIST = null;
	
	public static String[] LOOTBAGINDUNGEONLOOT;
	
	private HashMap<String,Integer> totalvaluemap = new HashMap<String,Integer>();
	
	public static boolean DISABLERECYCLER = false;
	public static boolean DISABLEOPENER = false;
	public static boolean DISABLESTORAGE = false;
	
	public static LootMap LOOTMAP;
	
	public static BagConfigHandler bagconfig;
	
	private static Random random = new Random();
	
	@SidedProxy(clientSide="mal.lootbags.network.ClientProxy", serverSide="mal.lootbags.network.CommonProxy")
	public static CommonProxy prox;

	public static LootbagItem lootbagItem;
	public static BlockRecycler recyclerBlock;
	public static BlockOpener openerBlock;
	public static BlockStorage storageBlock;

	@Instance(value = LootBags.MODID)
	public static LootBags LootBagsInstance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MobDropHandler handler = new MobDropHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		NetworkRegistry.INSTANCE.registerGuiHandler(LootBagsInstance, new GUIHandler());
		LOOTLOG = event.getModLog();
		LOOTLOG.log(Level.INFO, "Your current LootBags version is: " + LootBags.VERSION);
		
		LootBags.recyclerBlock = new BlockRecycler();
		LootBags.openerBlock = new BlockOpener();
		LootBags.lootbagItem = new LootbagItem();
		LootBags.storageBlock = new BlockStorage();
		
		GeneralConfigHandler.loadConfig(event);
		bagconfig = new BagConfigHandler(event);
		bagconfig.initBagConfig();
		
		LootbagsPacketHandler.init();
		
		if(CHESTQUALITYWEIGHT <= 0)
		{
			LootbagsUtil.LogInfo("Chest Weighting < 1, this causes problems for everything and is terrible.  Setting it to 1 instead.");
			CHESTQUALITYWEIGHT = 1;
		}
		
		if(MAXREROLLCOUNT<=0)
		{
			LootbagsUtil.LogInfo("Reroll count has to be at least 1 (fancy error prevention stuff)");
			MAXREROLLCOUNT=1;
		}
		
		if(OPENERMAXCOOLDOWN<0)
		{
			LootbagsUtil.LogInfo("Negative cooldown is not possible, setting it to 0.");
			OPENERMAXCOOLDOWN=0;
		}
		
		if(TOTALVALUE<=1)
		{
			LootbagsUtil.LogInfo("Free or negative value required for lootbag creation is not a good thing.  Setting it to 1.");
			TOTALVALUE=1;
		}
		
		if(BagHandler.isIDFree(RECYCLEDID))
		{
			LootbagsUtil.LogInfo("The requested ID is not actually a bag, using the lowest used ID.");
			RECYCLEDID = BagHandler.getLowestUsedID();
		}
		
		if(RECYCLERVALUESTACK <= 0.0)
		{
			LootbagsUtil.LogInfo("Dividing by zero or having a negative loot item value is not good.  Setting it to 1.0.");
			RECYCLERVALUESTACK = 1.0;
		}
		
		if(RECYCLERVALUENONSTACK <= 0.0)
		{
			LootbagsUtil.LogInfo("Dividing by zero or having a negative loot item value is not good.  Setting it to 1.0.");
			RECYCLERVALUENONSTACK = 1.0;
		}
		
		if(RECYCLERVALUENUMERATOR <= 0.0)
		{
			LootbagsUtil.LogInfo("Free or negative loot item value is not good.  Setting it to 1.0.");
			RECYCLERVALUENUMERATOR = 1.0;
		}
		
		LootBags.prox.registerRenderersPreInit();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event) {
		
		LootBags.prox.registerRenderersInit();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "lootbags_tileentityrecycler");
		GameRegistry.registerTileEntity(TileEntityOpener.class, "lootbags_tileentityopener");
		GameRegistry.registerTileEntity(TileEntityStorage.class, "lootbags_tileentitystorage");
		
		//RecipeSorter.register("lootbags:lootrecipe", LootRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		
		LOOTMAP = new LootMap();
		LOOTMAP.populateGeneralBlacklist(GeneralConfigHandler.getBlacklistConfigData());
		LOOTMAP.populateGeneralWhitelist(GeneralConfigHandler.getWhitelistConfigData());
		LOOTMAP.setLootSources(LOOTCATEGORYLIST);
		
		LOOTMAP.setContext(null);
		LOOTMAP.populateGeneralMap(null);//FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0]);
		BagHandler.populateBagLists();
		LOOTMAP.setTotalListWeight();
		
		LOOTMAP.populateRecyclerBlacklist(GeneralConfigHandler.getRecyclerBlacklistConfigData());
		LOOTMAP.populateRecyclerWhitelist(GeneralConfigHandler.getRecyclerWhitelistConfigData());
		LootbagsUtil.LogInfo("Completed on-load tasks.");
		
		LootRegistry.getInstance();
		
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ItemDumpCommand());
		event.registerServerCommand(new LootSourceCommand());
		event.registerServerCommand(new NBTPullCommand());
		event.registerServerCommand(new ConfigReloadCommand());
		LootBags.LOOTMAP.setContext(FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0]);
	}
	
/*	@EventHandler
	public void serverLoaded(FMLServerStartedEvent event)
	{		
		if(!HASLOADED)
		{
			LOOTMAP.populateGeneralMap(FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0]);
			BagHandler.populateBagLists();
			LOOTMAP.setTotalListWeight();
			LootbagsUtil.LogInfo("Completed on-load tasks.");
			HASLOADED = true;
		}
	}*/
	
	/**
	 * Checks to see if an item can be dropped by a lootbag
	 */
	public static boolean isItemDroppable(ItemStack item)
	{
		for(LootItem loot: LOOTMAP.recyclerWhitelist)
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem(), item, true, false))
				return true;
		}
		for(LootItem loot: LOOTMAP.totalList.values())
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem(), item, true, false))
				return true;
			if(loot.getContentItem().getItem() instanceof ItemBook && item.getItem() instanceof ItemEnchantedBook)//fix for recycling enchanted books
				return true;
		}
		return false;
	}
	
	/**
	 * Check to see if an item is in the recycler blacklist
	 */
	public static boolean isItemRecyleBlacklisted(ItemStack item)
	{
		for(LootItem loot: LOOTMAP.recyclerBlacklist)
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem(), item, true, false))
				return true;
		}
		return false;
	}
	
	/**
	 * Check to see if an item is in the recycler whitelist
	 */
	public static boolean isItemRecycleWhitelisted(ItemStack item)
	{
		for(LootItem loot: LOOTMAP.recyclerWhitelist)
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem(), item, true, false))
				return true;
		}
		return false;
	}
	
	public static int getItemValue(ItemStack item)
	{
		for(LootItem c : LOOTMAP.recyclerWhitelist)
		{
			if(areItemStacksEqualItem(c.getContentItem(), item, true, false))
			{
				double value = Math.ceil(RECYCLERVALUENUMERATOR*LOOTMAP.getTotalListWeight()/(c.getItemWeight()*((item.getMaxStackSize()==1)?(RECYCLERVALUENONSTACK):(RECYCLERVALUESTACK))));
				//LootbagsUtil.LogInfo("Value: " + value);
				if(value <= 0)
					value = 1;
				return (int)value;
			}
		}
		for(LootItem c : LOOTMAP.totalList.values())
		{
			if(areItemStacksEqualItem(c.getContentItem(), item, true, false) || (c.getContentItem().getItem() instanceof ItemBook && item.getItem() instanceof ItemEnchantedBook))
			{
				double value = Math.ceil(RECYCLERVALUENUMERATOR*LOOTMAP.getTotalListWeight()/(c.getItemWeight()*((item.getMaxStackSize()==1)?(RECYCLERVALUENONSTACK):(RECYCLERVALUESTACK))));
				//LootbagsUtil.LogInfo("Value: " + value);
				if(value <= 0)
					value = 1;
				return (int)value;
			}
		}
		
		return 0;
	}
    
    public static boolean areItemStacksEqualItem(ItemStack is1, ItemStack is2, boolean alwaysUseDamage, boolean considerNBT)
    {
    	if(is1==null || is2==null)
    		return is1==is2;
    	if(is1.isEmpty() || is2.isEmpty())
    		return (is1.isEmpty()&&is2.isEmpty());
    	if(is1.getItem() != is2.getItem())
    		return false;
    	if((alwaysUseDamage && (is1.getHasSubtypes() && is2.getHasSubtypes())) && is1.getItemDamage() != is2.getItemDamage())
    		return false;
    	if(considerNBT && !ItemStack.areItemStackTagsEqual(is1, is2))
    		return false;
    	return true;
    }
    
    public static int getDefaultDropWeight()
    {
    	return DROPRESOLUTION/10;
    }
    
    public static Random getRandom()
    {
    	return random;
    }
    
    @Mod.EventBusSubscriber(modid = LootBags.MODID)
    public static class RegistrationHandler {

    	
    	@SubscribeEvent
    	public static void registerBlocks(RegistryEvent.Register<Block> event)
    	{
    		
    		event.getRegistry().registerAll(LootBags.openerBlock, LootBags.recyclerBlock, LootBags.storageBlock);
    	}
    	
    	@SubscribeEvent
    	public static void registerItems(final RegistryEvent.Register<Item> event)
    	{

    		event.getRegistry().register(LootBags.lootbagItem);
    		
    		event.getRegistry().register(new ItemBlock(LootBags.openerBlock).setRegistryName(LootBags.openerBlock.getRegistryName()));
    		event.getRegistry().register(new RecyclerItemBlock(LootBags.recyclerBlock).setRegistryName(LootBags.recyclerBlock.getRegistryName()));
    		event.getRegistry().register(new StorageItemBlock(LootBags.storageBlock).setRegistryName(LootBags.storageBlock.getRegistryName()));
    	}
    }
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
