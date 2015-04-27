package mal.lootbags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Generate and retrieve a list of every item that can be dropped by a loot bag
 * @author Mal
 *
 */
public class LootMap {

	private TreeMap<String, LootItem> map = new TreeMap<String, LootItem>();
	//private TreeMap<String, ArrayList<WeightedRandomChestContent>> whitelists = new TreeMap<String, ArrayList<WeightedRandomChestContent>>();
	
	private int totalWeight=0;
	private Random random = new Random();
	
	/**
	 * Add every item in a category to the map calculating the drop chance
	 * @param categoryName
	 */
	public void addLootCategory(String categoryName)
	{
		WeightedRandomChestContent[] h = ChestGenHooks.getItems(categoryName, random);
		for(int i = 0; i < h.length; i++)
		{
			WeightedRandomChestContent c = h[i];
			boolean skip = false;
			//Do some fixing to prevent <1 errors in the bags
			if(c.itemWeight < 1)
			{
				FMLLog.log(Level.ERROR, "Item " + c.theItemId.toString() + " has a weighting of " + c.itemWeight + ".  This is not a Lootbags error but an error in a different mod!  "
						+ "The item for Lootbags is now weighted at 1.");
				c.itemWeight = 1;
			}
			
			for(String modid:LootBags.MODBLACKLIST)
			{
				if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
					skip = true;
			}
			for(ItemStack istack:LootBags.BLACKLIST.get(0))
			{
				if(istack.isItemEqual(c.theItemId))
					skip = true;
			}
			if(!skip)
			{
				ArrayList<BagTypes> ty = new ArrayList<BagTypes>();
				for(BagTypes t:BagTypes.droppableValues())
				{
					for(ItemStack istack:LootBags.BLACKLIST.get(t.getIndex()+1))
					{
						if(istack.isItemEqual(c.theItemId))
						{
							ty.add(t);
						}
					}
				}
				
				if(!map.containsKey(c.theItemId.toString()))
				{
					LootItem ii = new LootItem(c, BagTypes.Common, BagTypes.Legendary);
					ii.removeBagTypes(ty);
					map.put(c.theItemId.toString(), ii);
					totalWeight+= c.itemWeight;
				}
				else
				{
					LootItem item = map.get(c.theItemId.toString());
					int weight = item.getContentItem().itemWeight;
					totalWeight -= weight;
					weight = (weight+c.itemWeight)/2;
					item.getContentItem().itemWeight = weight;
					item.removeBagTypes(ty);
					map.put(c.theItemId.toString(), item);
					totalWeight += weight;
				}
			}
		}
	}
	
	public void addWhitelistedItems(String[][] whitelistlist)
	{
		for(int i = 0; i < whitelistlist.length; i++)
		{
			for(String s: whitelistlist[i])
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
							int weight = Integer.parseInt(words[2]);
							
							WeightedRandomChestContent c = new WeightedRandomChestContent(is, 1, is.stackSize, weight);
							
							boolean skip = false;
							for(String modid:LootBags.MODBLACKLIST)
							{
								if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
									skip = true;
							}
							for(ItemStack istack:LootBags.BLACKLIST.get(i))
							{
								if(istack.isItemEqual(c.theItemId))
									skip = true;
							}
							if(!skip)
							{
								if(i == 0)
								{
									map.put(c.theItemId.toString(), new LootItem(c, BagTypes.Common, BagTypes.Legendary));
									totalWeight+= c.itemWeight;
								}
								else
								{
									int tier = i-1;
									BagTypes type;
									if(BagTypes.droppableValues()[tier].getIndex() == tier)
										type = BagTypes.droppableValues()[tier];
									else
										type = BagTypes.Common;
									if(!map.containsKey(c.theItemId.toString()))
									{
										map.put(c.theItemId.toString(), new LootItem(c, type));
										totalWeight += c.itemWeight;
									}
									else
									{
										LootItem item = map.get(c.theItemId.toString());
										
										item.addBagType(type);
									}
								}
							}
							else
							{
								FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from the LootBags whitelist from spawning in Loot Bags.");
							}
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
						else if(block != null)
							stack = new ItemStack(block,Integer.parseInt(words[3]),Integer.parseInt(words[2]));
						if(stack != null && stack.getItem() != null)
						{
							FMLLog.log(Level.INFO, "Added Whitelist item: " + stack.toString());
							WeightedRandomChestContent c = new WeightedRandomChestContent(stack, 1, stack.stackSize, Integer.parseInt(words[4]));
							
							boolean skip = false;
							for(String modid:LootBags.MODBLACKLIST)
							{
								if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
									skip = true;
							}
							for(ItemStack istack:LootBags.BLACKLIST.get(i))
							{
								if(istack.isItemEqual(c.theItemId))
									skip = true;
							}
							if(!skip)
							{
								if(i == 0)
								{
									map.put(c.theItemId.toString(), new LootItem(c, BagTypes.Common, BagTypes.Legendary));
									totalWeight+= c.itemWeight;
								}
								else
								{
									int tier = i-1;
									BagTypes type;
									if(BagTypes.droppableValues()[tier].getIndex() == tier)
										type = BagTypes.droppableValues()[tier];
									else
										type = BagTypes.Common;
									if(!map.containsKey(c.theItemId.toString()))
									{
										map.put(c.theItemId.toString(), new LootItem(c, type));
										totalWeight += c.itemWeight;
									}
									else
									{
										LootItem iitem = map.get(c.theItemId.toString());
										
										iitem.addBagType(type);
									}
								}
							}
							else
							{
								FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from the LootBags whitelist from spawning in Loot Bags.");
							}
						}
					}
				}
			}
		}
	}
	
	public ItemStack getRandomItem(int maxWeight, BagTypes type)
	{
		WeightedRandomChestContent[] content;
		int randWeight;
		content = generateContent(maxWeight, type);
		if(content.length > 0 && generateMaxTotalWeight(content)>0)
		{
			randWeight = random.nextInt(generateMaxTotalWeight(content));
		
			WeightedRandomChestContent item = (WeightedRandomChestContent) WeightedRandom.getItem(content, randWeight);
			int r = 0;
			while (item == null && r < LootBags.MAXREROLLCOUNT)
			{
				System.out.println("reroll null item");
				item = (WeightedRandomChestContent) WeightedRandom.getItem(content, randWeight);
				r++;
			}
			if(item == null)
				return null;
    		ItemStack[] stacks = ChestGenHooks.generateStacks(random, item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
        	return (stacks.length > 0 ? stacks[0] : null);
		}
		return null;
	}
	
	private WeightedRandomChestContent[] generateContent(int maxWeight, BagTypes type)
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(LootItem c : map.values())
		{
			if(((c.getContentItem().itemWeight <= maxWeight || maxWeight==-1) && c.getContentItem().itemWeight > 0) && c.canDrop(type))
				list.add(c.getContentItem());
		}
		return list.toArray(new WeightedRandomChestContent[list.size()]);
	}
	
	private int generateMaxTotalWeight(WeightedRandomChestContent[] table)
	{
		int weight = 0;
		for(WeightedRandomChestContent c : table)
		{
			weight+= c.itemWeight;
		}
		return weight;
	}
	
	/**
	 * Generates the weight needed to guarantee X% of the items can drop
	 * @param percentile
	 * @return
	 */
	public int generatePercentileWeight(int percentile, BagTypes type)
	{
		LootItem[] lcontent = map.values().toArray(new LootItem[map.values().size()]);
		ArrayList<Integer> weights = new ArrayList<Integer>();
		for(LootItem c: lcontent)
		{
			if(c.canDrop(type))
				weights.add(c.getContentItem().itemWeight);
		}
		Collections.sort(weights);
		double val = percentile/100.0*weights.size();
		return weights.get((int) Math.floor(val));
	}
	
	public int getLargestWeight()
	{
		int weight = 0;
		for(LootItem item : map.values())
		{
			if(item.getContentItem().itemWeight > weight)
				weight = item.getContentItem().itemWeight;
		}
		
		return weight;
	}
	
	public ArrayList<ItemStack> getMapAsList()
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(LootItem c : map.values())
		{
			list.add(c.getContentItem().theItemId);
		}
		return list;
	}
	
	public ArrayList<WeightedRandomChestContent> getMapAsChestList()
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(LootItem c : map.values())
		{
			list.add(c.getContentItem());
		}
		return list;
	}
	
	public int getTotalWeight()
	{
		return totalWeight;
	}
	
	public boolean isItemInMap(ItemStack is)
	{
		ArrayList<ItemStack> list = getMapAsList();
		
		for(ItemStack i : list)
		{
			if(LootBags.areItemStacksEqualItem(i, is, false, false))
				return true;
		}
		return false;
	}
	public void printMap()
	{
		for(String i: map.keySet())
		{
			System.out.println(i + ": " + map.get(i).getContentItem().itemWeight + ": " + map.get(i).getContentItem().theMinimumChanceToGenerateItem + ": " + map.get(i).getContentItem().theMaximumChanceToGenerateItem);
		}
		System.out.println("Total Weight: " + totalWeight);
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/