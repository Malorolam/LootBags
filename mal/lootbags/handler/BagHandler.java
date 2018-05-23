package mal.lootbags.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import net.minecraft.item.ItemStack;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.loot.LootItem;

/*
 * Manages the different bag types and keeps the loot tables all straight between them
 */
public class BagHandler {

	private static HashMap<Integer, Bag> bagList = new HashMap<Integer, Bag>();
	private static ArrayList<Integer> storableinputIDs = new ArrayList<Integer>();
	private static ArrayList<Integer> storableoutputIDs = new ArrayList<Integer>();
	public static final int HARDMAX = 5;//absolute maximum number of items allowed in a bag, this never can be changed
	
	public static void clearBags()
	{
		bagList = new HashMap<Integer, Bag>();
		storableinputIDs = new ArrayList<Integer>();
		storableoutputIDs = new ArrayList<Integer>();
	}
	
	public static void addBag(Bag bag)
	{
		bagList.put(bag.getBagIndex(), bag);
		if(bag.isStorable().canInput())
			storableinputIDs.add(bag.getBagIndex());
		if(bag.isStorable().canOutput())
			storableoutputIDs.add(bag.getBagIndex());
		LootbagsUtil.LogDebug("Added bag: " + bag.getBagName() + " with ID: " + bag.getBagIndex() + ".");
	}
	
	public static Bag getBag(int index)
	{
		if(!isIDFree(index))
		{
			return bagList.get(index);
		}
		return null;
	}
	
	public static Bag getBag(String name)
	{
		for(Bag b: bagList.values())
		{
			if(b.getBagName().equalsIgnoreCase(name))
				return b;
		}
		return null;
	}
	
	public static boolean isIDFree(int ID)
	{
		if(bagList.containsKey(ID))
			return false;
		return true;
	}
	
	public static HashMap<Integer, Bag> getBagList()
	{
		return bagList;
	}
	
	public static void populateBagLists()
	{
		for(Bag bag:bagList.values())
		{
			if(bag != null)
				bag.populateBag();
		}
	}
	
	public static int getLowestUsedID()
	{
		int ID = Integer.MAX_VALUE;
		for(Bag bag:bagList.values())
		{
			if(bag.getBagIndex() < ID)
				ID = bag.getBagIndex();
		}
		return ID;
	}
	
	public static int getHighestUsedID()
	{
		int ID = -1;
		for(Bag bag:bagList.values())
		{
			if(bag.getBagIndex() > ID)
				ID = bag.getBagIndex();
		}
		return ID;
	}
	
	public static ArrayList<LootItem> generateContent(Collection<LootItem> collection)
	{
		ArrayList<LootItem> list = new ArrayList<LootItem>();
		for(LootItem c : collection)
		{
/*			if(c.getContentItem().getItem() instanceof ItemEnchantedBook && c.getGeneral())
			{
				LootItem original = c.copy();
				ItemStack s = c.getContentItem();
				ItemStack ss = EnchantmentHelper.addRandomEnchantment(LootBags.getRandom(), s, 30, true);
				c = new LootItem(original.getLootItem(), ss, original.getItemModID(), original.getItemName(), original.getMinStack(), original.getMaxStack(), original.getItemWeight(), original.getGeneral());
			}*/
			list.add(c);
		}
		return list;
	}
	
	public static boolean isBagEmpty(int ID)
	{
		if(isIDFree(ID))
			return true;
		return bagList.get(ID).isBagEmpty();
	}
	
	public static ArrayList<Bag> getBagListRandomized()
	{
		ArrayList<Bag> list = new ArrayList<Bag>();
		list.addAll(bagList.values());
		Collections.shuffle(list, LootBags.getRandom());
		return list;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isBagOpened(ItemStack bag)
	{
		return bag.getTagCompound() != null && bag.getTagCompound().getBoolean("generated");
	}
	
	public static int getBagCount()
	{
		return bagList.size();
	}
	
	public static int[] getBagValue(ItemStack stack)
	{
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof LootbagItem))
			return new int[] {-1, -1};
		return getBag(stack.getMetadata()).getBagValue();
	}
	
	public static int[] getBagValue(int ID)
	{
		return getBag(ID).getBagValue();
	}
	
	public static boolean isBagInsertable(int ID)
	{
		return storableinputIDs.contains(ID);
	}
	
	public static boolean isBagExtractable(int ID)
	{
		return storableoutputIDs.contains(ID);
	}
	
	public static ArrayList<Integer> getExtractedBagList()
	{
		return storableoutputIDs;
	}
	/*public static void generateBagRecipes(List recipeList)
	{
		for(Bag b: bagList.values())
		{
			if(b.getCraftingSource() != null)
			{
				Bag source = getBag(b.getCraftingSource());
				if(source != null)
				{
					int bagsneeded = b.getCraftingCount();
					if(bagsneeded < 1)
						bagsneeded = 1;
					if(bagsneeded > 9)
						bagsneeded = 9;
					
					Object[] c = new Object[bagsneeded];
					for(int j = 0; j < c.length; j++)
					{
						c[j] = new ItemStack(LootBags.lootbagItem, 1, source.getBagIndex());
					}
					
					Object[] d = new Object[1];
					d[0] = new ItemStack(LootBags.lootbagItem, 1, b.getBagIndex());
					
					if(LootBags.CRAFTTYPES != 0)
					{
						if(LootBags.CRAFTTYPES < 3)
							recipeList.add(new LootRecipe_old(b.getBagItem(), c));//source to bag
						if(LootBags.CRAFTTYPES > 1)
							recipeList.add(new LootRecipe_old(new ItemStack(LootBags.lootbagItem, bagsneeded, source.getBagIndex()), d));//bag to source
					}
				}
			}
		}
	}*/
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/