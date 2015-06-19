package mal.lootbags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.item.LootbagItem;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
						+ "This item will be removed from the loot table.");
//				ChestGenHooks.removeItem(categoryName, c.theItemId);
			}
			else
			{
			
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
					
					String key = c.theItemId.getUnlocalizedName()+c.theItemId.getItemDamage();
					
					if(!map.containsKey(key))
					{
						LootItem ii = new LootItem(c, BagTypes.Common, BagTypes.Legendary);
						ii.removeBagTypes(ty);
						map.put(key, ii);
						totalWeight+= c.itemWeight;
					}
					else
					{
						LootItem item = map.get(key);
						int weight = item.getContentItem().itemWeight;
						totalWeight -= weight;
						weight = (weight+c.itemWeight)/2;
						item.getContentItem().itemWeight = weight;
						item.removeBagTypes(ty);
						map.put(key, item);
						totalWeight += weight;
					}
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
				boolean skip = false;
				String trim = s.trim();
				if(!trim.isEmpty())
				{
					String[] words = trim.split("\\s+");
					if(words.length == 4)
					{
						if(!OreDictionary.getOres(words[0]).isEmpty())
						{
							FMLLog.log(Level.INFO, "Added Whitelist item from OreDictionary: " + words[0] + "x" + words[1] + " - " + words[3]);
							ArrayList<ItemStack> ll = OreDictionary.getOres(words[0]);
							//System.out.println(ll.size());
							for(int j = 0; j < (words[3].equalsIgnoreCase("ALL")?(ll.size()):(1)); j++)
							{
								ItemStack is = ll.get(j).copy();
								//System.out.println(((ItemRecord)is.getItem()).recordName);
								is.stackSize=Integer.parseInt(words[1]);
								int weight = Integer.parseInt(words[2]);
								
								WeightedRandomChestContent c = new WeightedRandomChestContent(is, 1, is.stackSize, weight);
								
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
									String key = c.theItemId.getUnlocalizedName()+c.theItemId.getItemDamage();
									if(words[0].equalsIgnoreCase("record") && is.getItem() instanceof ItemRecord)
										key = ((ItemRecord)is.getItem()).recordName;
									
									if(i == 0)
									{
										map.put(key, new LootItem(c, BagTypes.Common, BagTypes.Legendary));
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
										if(!map.containsKey(key))
										{
											map.put(key, new LootItem(c, type));
											totalWeight += c.itemWeight;
										}
										else
										{
											LootItem item = map.get(key);
											
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
					}
					if(words.length == 5)
					{
						int damage = 0;
						boolean isdamage = false;
						boolean damagerange = false;
						ArrayList<Integer> damages = null;
						try {
							damage = Integer.parseInt(words[2]);
							isdamage = true;
						} catch(Exception e) {}
						
						if(!isdamage)
						{
							damagerange = true;
							//verify that the word is only numbers and #, &
							for(int k = 0; k < words[2].length(); k++)
							{
								if(!(words[2].substring(k, k+1).matches("[0-9]") || words[2].substring(k, k+1).matches("[#&]")))
								{
									damagerange=false;
									break;
								}
							}
							if(damagerange)
								damages = constructDamageRange(words[2]);
						}
						
						ItemStack stack = GameRegistry.findItemStack(words[0], words[1], 1);
						if(stack == null)
						{
						//one of these should be not null
						Block block = GameRegistry.findBlock(words[0], words[1]);
						Item item = GameRegistry.findItem(words[0], words[1]);
						if(item != null)
							stack = new ItemStack(item,Integer.parseInt(words[3]),damage);
						else if(block != null)
							stack = new ItemStack(block,Integer.parseInt(words[3]),damage);
						}
						
						if(stack != null && stack.getItem() != null)
						{
							if(stack.getItem() instanceof ItemRecord)
							{
								String tag = words[1].substring(7);
								stack = new ItemStack(ItemRecord.getRecord("records."+tag),Integer.parseInt(words[3]),damage);
							}
							else if(stack.getItem() instanceof ItemEnchantedBook && !isdamage)
							{
								//must be an enchantment, so make sure the text is actually an enchantment
								int b = -1;
								for(int k = 0; k < Enchantment.enchantmentsList.length; k++)
								{
									if(Enchantment.enchantmentsList[k] != null && Enchantment.enchantmentsList[k].getName().equalsIgnoreCase(words[2]))
									{
										b = k;
										break;
									}
								}
								if(b>-1)
								{
									int level = Integer.parseInt(words[3]);
									/*if(level > Enchantment.enchantmentsList[b].getMaxLevel())
									{
										level = Enchantment.enchantmentsList[b].getMaxLevel();
										FMLLog.log(Level.INFO,"Enchantment " + words[2] + " level exceeds maximum.  Setting to maximum value.");
									}*/
									if(level < Enchantment.enchantmentsList[b].getMinLevel())
									{
										level = Enchantment.enchantmentsList[b].getMinLevel();
										FMLLog.log(Level.INFO,"Enchantment " + words[2] + " level below minimum.  Setting to minimum value.");
									}
									
									stack = ((ItemEnchantedBook) stack.getItem()).getEnchantedItemStack(new EnchantmentData(b, level));
								}
								else
								{
									FMLLog.log(Level.INFO, "Enchantment " + words[2] + " not found.");
									skip = true;
								}
							}
							else
							{
								stack.stackSize = Integer.parseInt(words[3]);
								stack.setItemDamage(damage);
							}
							
							if(stack.stackSize > stack.getMaxStackSize())
								stack.stackSize = stack.getMaxStackSize();
							
							if(!damagerange)
								addWhitelistItem(stack, i, words[1].startsWith("record"), (stack.getItem() instanceof ItemEnchantedBook)?(words[2]):(null), Integer.parseInt(words[4]), skip);
							else
							{
								for(int l = 0; l < damages.size(); l++)
								{
									ItemStack is = stack.copy();
									is.setItemDamage(damages.get(l));
									addWhitelistItem(is, i, words[1].startsWith("record"), (stack.getItem() instanceof ItemEnchantedBook)?(words[2]):(null), Integer.parseInt(words[4]), skip);
								}
							}
						}
						else
						{
							FMLLog.log(Level.ERROR, "There is a null item, it's string in the config is: " + s + " Make sure that the whitelist entry is correct.");
						}
					}
				}
			}
		}
	}
	
	private void addWhitelistItem(ItemStack stack, int i, boolean isRecord, String enchantmentName, int weight, boolean skip)
	{
		FMLLog.log(Level.INFO, "Added Whitelist item: " + stack.toString());
		WeightedRandomChestContent c = new WeightedRandomChestContent(stack, 1, stack.stackSize, weight);
		
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
			String key;
			if(isRecord && stack.getItem() instanceof ItemRecord)
				key = ((ItemRecord)stack.getItem()).recordName;
			else if(enchantmentName !=null && stack.getItem() instanceof ItemEnchantedBook)
			{
				key = enchantmentName;
				NBTTagCompound tag = c.theItemId.stackTagCompound;
				tag.setBoolean("LootbagsWhitelist", true);
			}
			else
				key = c.theItemId.getUnlocalizedName()+c.theItemId.getItemDamage();
			
			if(i == 0)
			{
				map.put(key, new LootItem(c, BagTypes.Common, BagTypes.Legendary));
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
				if(!map.containsKey(key))
				{
					map.put(key, new LootItem(c, type));
					totalWeight += c.itemWeight;
				}
				else
				{
					LootItem iitem = map.get(key);
					
					iitem.addBagType(type);
				}
			}
		}
		else
		{
			FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from the LootBags whitelist from spawning in Loot Bags.");
		}
	}
	
	public ItemStack getRandomItem(int maxWeight, BagTypes type)
	{
		WeightedRandomChestContent[] content;
		int randWeight;
		if(!LootBags.REVERSEQUALITY)
			content = generateContent(maxWeight, type);
		else
			content = generateInverseContent(maxWeight, type);
		
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
			if(item.theItemId.getItem() instanceof ItemEnchantedBook)
			{
				NBTTagCompound tag = item.theItemId.getTagCompound();
				if(tag != null)
				{
					if(tag.hasKey("LootbagsWhitelist"))
					{
						//System.out.println("removed whitelist tag");
						item = new WeightedRandomChestContent(item.theItemId.copy(), 1, item.theItemId.stackSize, item.itemWeight);
						tag = item.theItemId.getTagCompound();
						tag.removeTag("LootbagsWhitelist");
					}
					else
					{
						//System.out.println("refreshed enchantment");
						item = ((ItemEnchantedBook)item.theItemId.getItem()).func_92112_a(random,item.theMinimumChanceToGenerateItem,item.theMaximumChanceToGenerateItem, item.itemWeight);
					}
				}
			}
    		ItemStack[] stacks = ChestGenHooks.generateStacks(random, item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
        	return (stacks.length > 0 ? stacks[0] : null);
		}
		return null;
	}
	
	private WeightedRandomChestContent[] generateInverseContent(int minWeight, BagTypes type)
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(LootItem c : map.values())
		{
			if(((c.getContentItem().itemWeight >= minWeight || minWeight==-1) && c.getContentItem().itemWeight > 0) && c.canDrop(type))
				list.add(c.getContentItem());
		}
		return list.toArray(new WeightedRandomChestContent[list.size()]);
	}
	
	private WeightedRandomChestContent[] generateContent(int maxWeight, BagTypes type)
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(LootItem c : map.values())
		{
			if((((c.getContentItem().itemWeight <= maxWeight || maxWeight==-1) && c.getContentItem().itemWeight > 0) && c.canDrop(type)) || c.canOnlyDrop(type))
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
		if(weights.size()==0)
			return 0;
		return weights.get((int) Math.floor(val));
	}
	
	/**
	 * Inverse version of the above
	 * @return
	 */
	public int generateInversePercentileWeight(int percentile, BagTypes type)
	{
		LootItem[] lcontent = map.values().toArray(new LootItem[map.values().size()]);
		ArrayList<Integer> weights = new ArrayList<Integer>();
		for(LootItem c: lcontent)
		{
			if(c.canDrop(type))
				weights.add(c.getContentItem().itemWeight);
		}
		Collections.sort(weights);
		double val = (1.0-percentile/100.0)*weights.size();
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
	
	private ArrayList<Integer> constructDamageRange(String word)
	{
		//construct the damage values from the syntax, & between two numbers indicates inclusive and # indicates
		//to skip number inbetween the border numbers
		//example: 0&5#8&10 will use damage values 0, 1, 2, 3, 4, 5, 8, 9, 10
		
		ArrayList<Integer> damages = new ArrayList<Integer>();
		
		String[] sec = word.split("[#&]");
		//should be a bunch of numbers now
		
		//figure out if between two numbers is inclusive or exclusive
		int wordindex = 0;
		for(int i = 0; i < sec.length-1; i++)
		{
			wordindex += sec[i].length();
			int num1 = Integer.parseInt(sec[i]);
			int num2 = Integer.parseInt(sec[i+1]);
			if(word.substring(wordindex, wordindex+1).equalsIgnoreCase("#"))//exclusive
			{
				damages.add(num1);
				damages.add(num2);//don't worry about duplicates, we'll fix it later
			}
			else if(word.substring(wordindex, wordindex+1).equalsIgnoreCase("&"))
			{
				if(num2<num1)
				{
					int t = num1;
					num1 = num2;
					num2 = t;
				}
				
				for(int j = num1; j <= num2; j++)
				{
					damages.add(j);
				}
			}
			wordindex += 1;//add in the index for the extra character
		}
		
		//sort the list and remove all duplicates
		for(int i = 1; i < damages.size(); i++)
		{
			for(int j = i; j>1 && damages.get(j) < damages.get(j-1); j--)
			{
				int temp = damages.get(j);
				damages.set(j, damages.get(j-1));
				damages.set(j-1, temp);
			}
		}
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(int i = 0; i < damages.size(); i++)
		{
			if(!ret.contains(damages.get(i)))
				ret.add(damages.get(i));
		}
		
		System.out.println(ret.toString());
		return ret;
	}
	
	/*
	 * Simulate loot table situations and find the bags that are empty to remove them from dropping and crafting
	 */
	public BagTypes[] checkEmptyBags()
	{
		BagTypes[] ret = BagTypes.droppableValues().clone();
		for(int i = 0; i < 5; i++)
		{
			BagTypes type = BagTypes.droppableValues()[i];
			int maxWeight = LootbagItem.getWeightFromDamage(i);
			
			WeightedRandomChestContent[] content;
			int randWeight;
			if(!LootBags.REVERSEQUALITY)
				content = generateContent(maxWeight, type);
			else
				content = generateInverseContent(maxWeight, type);
			
			if(content.length >0)
				ret[i]=null;
		}
		return ret;
	}
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/