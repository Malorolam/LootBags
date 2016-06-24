package mal.lootbags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mal.lootbags.config.BagEntitySource;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.loot.LootItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

/*
 * A single bag, contains ALL the information about it
 */
public class Bag {

	private String bagName="Default";//unlocalized name of the bag
	private String bagNameColor="";//color that the localized name shows up as in the tooltip
	private int bagIndex=-1;//an internal reference to the index linking the bag to the item
	private List<String> bagTextUnopened = new ArrayList<String>();//the unopened text of the bag, is run through the localization method
	private List<String> bagTextOpened = new ArrayList<String>();//the opened text of the bag
	private List<String> bagTextShift = new ArrayList<String>();//the shift text of the bag
	private int bagTextureColorBase = 16777215;
	private int bagTextureColorString = 16777215;
	private boolean useAltJsonFile = true;
	private int bagWeight=LootBags.getDefaultDropWeight();//the weight of the bag, used for crafting, recycling weights, and appearances in dungeon chests
	private int[] spawnChances = new int[4];//chance of the bag spawning from player, passive, monster, and boss
	private int maxItems=5;//maximum items
	private int minItems=1;//minimum items
	private int maxGeneralWeight=-1;//maximum weight pulled from the general table (if enabled), negative value is no maximum
	private int minGeneralWeight=-1;//minimum weight pulled from the general table (if enabled), negative value is no minimum
	//private List<Entity> entityList;//list of entities to either blacklist or whitelist, depending on the next boolean
	private boolean entityExclusionToggle=false;//false has the list act as a blacklist, true is as a whitelist
	private boolean useGeneralLootTable=false;//use the general table or not
	private boolean isSecret = false;//determines if the bag shows up in creative inventory or not
	private int preventItemRepeats=0;//prevent an item from showing up multiple times, 1 to block the same damage, 2 to block same item class
	private int bagMapWeight = 0;//total weight of the items in the bag
	private boolean bagIsEmpty = false;//should never be true
	private String sourceBagName=null;//the unlocalized name of the bag that crafts into this one, the number is determined when the recipe is added
	private HashMap<String, LootItem> map = new HashMap<String, LootItem>();//a map of the items in the bag
	private ArrayList<LootItem> BagWhitelist = new ArrayList<LootItem>();
	private ArrayList<LootItem> BagBlacklist = new ArrayList<LootItem>();
	private ArrayList<String> BagModBlacklist = new ArrayList<String>();
	private ArrayList<BagEntitySource> EntityList = new ArrayList<BagEntitySource>();
	
	public Bag(String name, int index)
	{
		bagName = name;
		bagIndex = index;
	}
	
	public void populateBag()
	{
		//pull a loot item list based off of the minimum and maximum weights
		ArrayList<LootItem> list;
		if(useGeneralLootTable)
			list = LootBags.LOOTMAP.getMapByWeight(minGeneralWeight, maxGeneralWeight);
		else
			list = new ArrayList<LootItem>();
		
		//refresh the blacklist to cover for null items
		ArrayList<LootItem> nullClear = new ArrayList<LootItem>();
		for(LootItem item: BagBlacklist)
		{
			if(item.getContentItem()==null)
				item.reinitializeLootItem();
			if(item.getContentItem()==null)//if it's still null, it's not my fault now
			{
				LootbagsUtil.LogError("Blacklisted Content Item " + item.getItemModID() + ":" + item.getItemName() + " is NULL.  This is a major problem, probably caused by the item not being initilized and added to the Forge registry before the PostInit event when this code runs.");
				nullClear.add(item);
			}
		}
		BagBlacklist.removeAll(nullClear);
		
		//get rid of the blacklisted items and mods and add to the proper map
		for(LootItem item:list)
		{
			if(!LootbagsUtil.listContainsItem(BagBlacklist, item) && !BagModBlacklist.contains(item.getItemModID()))
			{
				String key = item.getItemModID()+item.getItemName()+item.getContentItem().theItemId.getItemDamage();
				map.put(key, item);
				bagMapWeight += item.getItemWeight();
			}
		}
		
		//add in whitelisted items
		for(LootItem item: BagWhitelist)
		{
			if(item.getContentItem()==null)
				item.reinitializeLootItem();
			if(item.getContentItem()!=null)//if it's still null, something is really wrong with the item
			{
				String key = item.getItemModID()+item.getItemName()+item.getContentItem().theItemId.getItemDamage();
				if(item.getContentItem().theItemId.getItem() instanceof ItemEnchantedBook && item.getContentItem().theItemId.hasTagCompound())//a specific enchanted book
					key += item.getContentItem().theItemId.getTagCompound().toString();
				if(map.containsKey(key))
				{
					bagMapWeight -= map.get(key).getItemWeight();
					map.remove(key);//remove the existing entry to overwrite it with the whitelisted version
				}
				
				map.put(key, item);
				bagMapWeight += item.getItemWeight();
				
				//key += this.bagName;
				if(!LootBags.LOOTMAP.totalList.containsKey(key))
				{
					//LootbagsUtil.LogInfo("Listing: " + key + ":" + item.getItemWeight());
					LootBags.LOOTMAP.totalList.put(key,  item);
				}

			}
			else
			{
				LootbagsUtil.LogError("Whitelisted Content Item " + item.getItemModID() + ":" + item.getItemName() + " is NULL.  This is a major problem, probably caused by the item not being initilized and added to the Forge registry before the PostInit event when this code runs.");
			}
		}
		
/*		System.out.println("Bag ID: " + bagIndex);
		System.out.println(bagMapWeight);
		for(LootItem item: map.values())
		{
			System.out.println(item.toString());
		}*/
	}
	
	public ItemStack getRandomItem()
	{
		ArrayList<WeightedRandomChestContent> content = BagHandler.generateContent(map.values());
		
		if(content.size() > 0 && bagMapWeight > 0)
		{	
			WeightedRandomChestContent item = (WeightedRandomChestContent) WeightedRandom.getRandomItem(LootBags.getRandom(), content, bagMapWeight);
			int r = 0;
			while (item == null && r < LootBags.MAXREROLLCOUNT)
			{
				LootbagsUtil.LogInfo("Rerolling null item: Reroll count " + r + ".");
				item = (WeightedRandomChestContent) WeightedRandom.getRandomItem(LootBags.getRandom(), content, bagMapWeight);
				r++;
			}
			if(item == null)
				return null;
			
			ItemStack[] stacks = ChestGenHooks.generateStacks(LootBags.getRandom(), item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
			return (stacks.length > 0 ? stacks[0] : null);
		}
		LootbagsUtil.LogError("Failed to get random item: Bag loot table or total weight <= 0.  This probably means this bag's config information is messed up somehow.");
		return null;
	}
	
	public ItemStack getSpecificItem(int index)
	{
		if(index < map.values().size())
			return ((LootItem)map.values().toArray()[index]).getContentItem().theItemId;
		return null;
	}
	
	public void addWhitelistItem(LootItem item)
	{
		BagWhitelist.add(item);
	}
	
	public void addWhitelistItem(String modid, String itemname, ArrayList<Integer> damage, int minstack, int maxstack, int weight)
	{
		for(Integer dam: damage)
		{
			LootItem item = new LootItem(modid, itemname, dam, minstack, maxstack, weight, false);
			BagWhitelist.add(item);
			//System.out.println(item.toString());
		}
	}
	
	public void addWhitelistItem(String modid, String itemname, ArrayList<Integer> damage, int minstack, int maxstack, int weight, byte[] nbt)
	{
		for(Integer dam: damage)
		{
			LootItem item = new LootItem(modid, itemname, dam, minstack, maxstack, weight, nbt, false);
			BagWhitelist.add(item);
		}
	}
	
	public void addBlacklistItem(LootItem item)
	{
		BagBlacklist.add(item);
	}
	
	public void addBlacklistItem(String modid, String itemname, ArrayList<Integer> damage)
	{
		for(Integer dam: damage)
		{
			LootItem item = new LootItem(modid, itemname, dam, 1, 1, 1, false);
			BagBlacklist.add(item);
		}
	}
	
	public void addBlacklistItem(String modid)
	{
		if(!BagModBlacklist.contains(modid))
			BagModBlacklist.add(modid);
	}
	
	public void setWeight(int weight)
	{
		bagWeight = weight;
	}
	
	public void setSpawnChancePlayer(int spawnchance)
	{
		spawnChances[0] = spawnchance;
	}
	
	public void setSpawnChancePassive(int spawnchance)
	{
		spawnChances[1] = spawnchance;
	}
	
	public void setSpawnChanceMonster(int spawnchance)
	{
		spawnChances[2] = spawnchance;
	}
	
	public void setSpawnChanceBoss(int spawnchance)
	{
		spawnChances[3] = spawnchance;
	}
	
	public void setCraftingSource(String bagName)
	{
		sourceBagName = bagName;
	}
	
	public void setBagNameColor(String code)
	{
		bagNameColor = code;
	}
	
	public void addUnopenedText(String text)
	{
		if(bagTextUnopened==null)
			bagTextUnopened = new ArrayList<String>();
		bagTextUnopened.add(text);
	}
	
	public void addOpenedText(String text)
	{
		if(bagTextOpened==null)
			bagTextOpened = new ArrayList<String>();
		text = text.replace("$", "");
		bagTextOpened.add(text);
	}
	
	public void addShiftText(String text)
	{
		if(bagTextShift==null)
			bagTextShift = new ArrayList<String>();
		text = text.replace("$", "");
		bagTextShift.add(text);
	}
	
	public void setBagColor(int baseColor, int stringColor)
	{
		bagTextureColorBase = baseColor;
		bagTextureColorString = stringColor;
		useAltJsonFile=false;
	}

	public String getBagName() {
		return bagName;
	}
	
	public String getBagNameColor()
	{
		return bagNameColor;
	}
	
	public String getDefaultName()
	{
		return "lootbag";
	}

	public int[] getChances()
	{
		return spawnChances;
	}
	
	public String getCraftingSource()
	{
		return sourceBagName;
	}
	
	public boolean isBagEmpty()
	{
		return bagIsEmpty;
	}
	
	/*
	 * drop chance from hostile mobs normalized to the global drop scaling
	 */
	public String getMonsterDropChance()
	{
		float chance = (spawnChances[2]*100.0f)/LootBags.DROPRESOLUTION;
		return String.format("%.2f",chance);
	}
	
	/*
	 * drop chance from passive mobs normalized to the global drop scaling
	 */
	public String getPassiveDropChance()
	{
		float chance = (spawnChances[1]*100.0f)/LootBags.DROPRESOLUTION;
		return String.format("%.2f",chance);
	}
	
	/*
	 * drop chance from players normalized to the global drop scaling
	 */
	public String getPlayerDropChance()
	{
		float chance = (spawnChances[0]*100.0f)/LootBags.DROPRESOLUTION;
		return String.format("%.2f",chance);
	}
	
	/*
	 * drop chance from bosses normalized to the global drop scaling
	 */
	public String getBossDropChance()
	{
		float chance = (spawnChances[3]*100.0f)/LootBags.DROPRESOLUTION;
		return String.format("%.2f", chance);
	}
	
	public int getBossDropWeight()
	{
		return spawnChances[3];
	}
	
	public int getPlayerDropWeight()
	{
		return spawnChances[0];
	}
	
	public int getPassiveDropWeight()
	{
		return spawnChances[1];
	}
	
	public int getMonsterDropWeight()
	{
		return spawnChances[2];
	}
	
	public ItemStack getBagItem()
	{
		return new ItemStack(LootBags.lootbagItem, 1, bagIndex);
	}
	
	/*
	 * Used to set the secret state of the bag, it's defaulted to false
	 */
	public Bag setSecret(boolean secret)
	{
		this.isSecret = secret;
		return this;
	}
	
	public void setGeneralSources(boolean state)
	{
		useGeneralLootTable=state;
	}
	
	public boolean getSecret()
	{
		return isSecret;
	}
	
	public int getBagIndex() {
		return bagIndex;
	}

	public List<String> getBagTextUnopened() {
		return bagTextUnopened;
	}

	public List<String> getBagTextOpened() {
		return bagTextOpened;
	}

	public List<String> getBagTextShift() {
		return bagTextShift;
	}

	public int[] getBagTextureColor()
	{
		int[] color = new int[2];
		color[0]=bagTextureColorBase;
		color[1]=bagTextureColorString;
		return color;
	}
	
	public boolean getUseAltJson()
	{
		return useAltJsonFile;
	}

	public int getBagWeight() {
		return bagWeight;
	}

	public HashMap<String, LootItem> getMap() {
		return map;
	}
	
	private boolean addItemToMap(LootItem content)
	{
		String key = content.getItemModID()+content.getItemName()+content.getContentItem().theItemId.getItemDamage();
		
		if(!map.containsKey(key))
		{
			map.put(key, content);
			return true;
		}
		else
			return false;
	}

	public void setMaximumItemsDropped(int weight) {
		maxItems = weight;
	}

	public void setMinimumItemsDropped(int weight) {
		minItems = weight;
	}

	public void setMaximumGeneralWeight(int weight) {
		maxGeneralWeight=weight;
	}
	
	public void setMinimumGeneralWeight(int weight) {
		minGeneralWeight=weight;
	}
	
	public void setItemRepeats(int state)
	{
		preventItemRepeats=state;
	}
	
	public void setEntityExclusion(boolean state)
	{
		entityExclusionToggle=state;
	}
	
	public boolean getEntityExlusion()
	{
		return entityExclusionToggle;
	}
	
	public void addEntityToList(String name, boolean isVisibleName)
	{
		BagEntitySource bs = new BagEntitySource(name, isVisibleName);
		EntityList.add(bs);
	}
	
	public ArrayList<BagEntitySource> getEntityList()
	{
		return EntityList;
	}
	
	public int getMinItems()
	{
		return minItems;
	}
	
	public int getMaxItems()
	{
		return maxItems;
	}
	
	public int getItemRepeats()
	{
		return preventItemRepeats;
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/