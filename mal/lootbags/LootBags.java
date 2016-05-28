package mal.lootbags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.logging.log4j.Level;

import mal.lootbags.blocks.BlockRecycler;
import mal.lootbags.config.BagConfigHandler;
import mal.lootbags.config.GeneralConfigHandler;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.handler.ConfigReloadCommand;
import mal.lootbags.handler.ItemDumpCommand;
import mal.lootbags.handler.LootSourceCommand;
import mal.lootbags.handler.MobDropHandler;
import mal.lootbags.handler.NBTPullCommand;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.loot.LootItem;
import mal.lootbags.loot.LootMap;
import mal.lootbags.network.CommonProxy;
import mal.lootbags.network.LootbagsPacketHandler;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
//import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
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
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = LootBags.MODID, version = LootBags.VERSION)
public class LootBags {
	public static final String MODID = "lootbags";
	public static final String VERSION = "2.0.7";
	
	public static int SPECIALDROPCHANCE = 250;
	
	public static int DROPRESOLUTION = 1000;
	
	public static int CHESTQUALITYWEIGHT = 20;
	
	public static int CPERCENTILE = 100;
	public static int UPERCENTILE = 75;
	public static int RPERCENTILE = 50;
	public static int EPERCENTILE = 25;
	public static int LPERCENTILE = 5;
	
	public static boolean REVERSEQUALITY = true;//reverses the quality to determine what can be dropped from a bag
	
	public static boolean SHOWSECRETBAGS = true;//shows the secret bags in NEI/creative inventory
	
	public static final int MINCHANCE = 0;
	public static final int MAXCHANCE = 1000;
	
	public static boolean LIMITONEBAGPERDROP = false;
	public static int BAGFROMPLAYERKILL = 2;//limit bag drops to only EntityPlayer kills, 0 is any source, 1 is EntityPlayer, 2 is forced real players
	public static int PREVENTDUPLICATELOOT = 0;//prevents the same item from showing up twice in a bag, 0 is not at all, 1 is if item and damage are the same, 2 is if item is the same
	public static int MINITEMSDROPPED = 1;//minimum number of items dropped by a bag
	public static int MAXITEMSDROPPED = 5;//maximum number of items dropped by a bag
	
	public static int MAXREROLLCOUNT = 50;
	public static double TOTALVALUEMULTIPLIER = 1.0;//multiplier for the total value to fabricate the bag
	public static int RECYCLEDID = 0;//bag id of the bag the recycler creates
	
	public static boolean PREVENTMERGEDBAGS = false;
	
	public static String[] LOOTCATEGORYLIST = null;
	
	public static String[] LOOTBAGINDUNGEONLOOT;
	
	private HashMap<String,Integer> totalvaluemap = new HashMap<String,Integer>();
	
	public static boolean DISABLERECYCLER = false;
	
	public static LootMap LOOTMAP = new LootMap();
	
	public static BagConfigHandler bagconfig;
	
	private static Random random = new Random();
	
	@SidedProxy(clientSide="mal.lootbags.network.ClientProxy", serverSide="mal.lootbags.network.CommonProxy")
	public static CommonProxy prox;

	public static LootbagItem lootbagItem;
	public static BlockRecycler recyclerBlock;

	@Instance(value = LootBags.MODID)
	public static LootBags LootBagsInstance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MobDropHandler handler = new MobDropHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		NetworkRegistry.INSTANCE.registerGuiHandler(LootBagsInstance, prox);
		
		FMLLog.log(Level.INFO, "Your current LootBags version is: " + this.VERSION);
		
		GeneralConfigHandler.loadConfig(event);
		bagconfig = new BagConfigHandler(event);
		bagconfig.initBagConfig();
		
		LootbagsPacketHandler.init();
		
		lootbagItem = new LootbagItem();
		recyclerBlock = new BlockRecycler();
		
		if(CHESTQUALITYWEIGHT <= 0)
		{
			FMLLog.log(Level.INFO, "Chest Weighting < 1, this causes problems for everything and is terrible.  Setting it to 1 instead.");
			CHESTQUALITYWEIGHT = 1;
		}
		
		if(MAXREROLLCOUNT<=0)
		{
			LootbagsUtil.LogInfo("Reroll count has to be at least 1 (fancy error prevention stuff)");
			MAXREROLLCOUNT=1;
		}
		
		if(TOTALVALUEMULTIPLIER<=0.0)
		{
			LootbagsUtil.LogInfo("Free or negative value required for lootbag creation is not a good thing.  Setting it to 1.");
			TOTALVALUEMULTIPLIER=1.0;
		}
		
		if(BagHandler.isIDFree(RECYCLEDID))
		{
			LootbagsUtil.LogInfo("The requested ID is not actually a bag, using the lowest used ID.");
			RECYCLEDID = BagHandler.getLowestUsedID();
		}
		
		this.prox.registerRenderers();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event) {
		
		/*if(event.getSide() == Side.CLIENT)
		{
			RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			
			renderItem.getItemModelMesher().register(Item.getItemFromBlock(recyclerBlock), 0, new ModelResourceLocation(LootBags.MODID + ":" + recyclerBlock.getName(), "inventory"));
		}*/
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		GameRegistry.registerTileEntity(TileEntityRecycler.class, "tileentityrecycler");
		
		LOOTMAP.populateGeneralBlacklist(GeneralConfigHandler.getBlacklistConfigData());
		LOOTMAP.populateGeneralWhitelist(GeneralConfigHandler.getWhitelistConfigData());
		LOOTMAP.populateRecyclerBlacklist(GeneralConfigHandler.getRecyclerBlacklistConfigData());
		LOOTMAP.populateRecyclerWhitelist(GeneralConfigHandler.getRecyclerWhitelistConfigData());
		LOOTMAP.setLootSources(LOOTCATEGORYLIST);
		LOOTMAP.populateGeneralMap();
		LOOTMAP.setTotalListWeight();
		
		BagHandler.populateBagLists();
		
		if(!DISABLERECYCLER)
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(recyclerBlock), new Object[]{"SSS", "SCS", "SIS", 'S', "stone", 'C', new ItemStack(Blocks.chest), 'I', "ingotIron"}));
		
		BagHandler.generateBagRecipes(CraftingManager.getInstance().getRecipeList());
		
		//TODO: fix
		if(LOOTBAGINDUNGEONLOOT.length>0)
		{
			WeightedRandomChestContent con = new WeightedRandomChestContent(new ItemStack(lootbagItem, 1, 0), 1, 1, CHESTQUALITYWEIGHT);
			for(String s:LOOTBAGINDUNGEONLOOT)
			{
				ChestGenHooks.addItem(s, con);
			}
		}
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ItemDumpCommand());
		event.registerServerCommand(new LootSourceCommand());
		event.registerServerCommand(new NBTPullCommand());
		event.registerServerCommand(new ConfigReloadCommand());
	}
	
/*	public static ArrayList<ItemStack> getLootbagDropList()
	{
		return LOOTMAP.getMapAsList();
	}*/
	
	/**
	 * Checks to see if an item can be dropped by a lootbag
	 */
	public static boolean isItemDroppable(ItemStack item)
	{
		for(LootItem loot: LOOTMAP.recyclerWhitelist)
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem().theItemId, item, false, false))
				return true;
		}
		for(LootItem loot: LOOTMAP.totalList.values())
		{
			if(LootBags.areItemStacksEqualItem(loot.getContentItem().theItemId, item, false, false))
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
			if(LootBags.areItemStacksEqualItem(loot.getContentItem().theItemId, item, false, false))
				return true;
		}
		return false;
	}
	
	public static int getItemValue(ItemStack item)
	{
		for(LootItem c : LOOTMAP.recyclerWhitelist)
		{
			if(areItemStacksEqualItem(c.getContentItem().theItemId, item, false, false))
			{
				double value = Math.ceil(2*LOOTMAP.getTotalListWeight()/(c.getItemWeight()*((item.getMaxStackSize()==1)?(1):(8))));
				if(value <= 0)
					value = 1;
				return (int)value;
			}
		}
		for(LootItem c : LOOTMAP.totalList.values())
		{
			if(areItemStacksEqualItem(c.getContentItem().theItemId, item, false, false))
			{
				double value = Math.ceil(2*LOOTMAP.getTotalListWeight()/(c.getItemWeight()*((item.getMaxStackSize()==1)?(1):(8))));
				if(value <= 0)
					value = 1;
				return (int)value;
			}
		}
		
		return 0;
	}
    
    public static boolean areItemStacksEqualItem(ItemStack is1, ItemStack is2, boolean alwaysUseDamage, boolean considerNBT)
    {
    	if(is1==null ^ is2==null)
    		return false;
    	if(Item.getIdFromItem(is1.getItem()) != Item.getIdFromItem(is2.getItem()))
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
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
