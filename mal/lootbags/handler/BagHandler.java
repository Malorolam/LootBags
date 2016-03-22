package mal.lootbags.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.loot.LootItem;
import mal.lootbags.loot.LootRecipe;

/*
 * Manages the different bag types and keeps the loot tables all straight between them
 */
public class BagHandler {

	private static HashMap<Integer, Bag> bagList = new HashMap<Integer, Bag>();
	
	public static void clearBags()
	{
		bagList = new HashMap<Integer, Bag>();
	}
	
	public static void addBag(Bag bag)
	{
		bagList.put(bag.getBagIndex(), bag);
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
	
	public static ArrayList<WeightedRandomChestContent> generateContent(Collection<LootItem> collection)
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(LootItem c : collection)
		{
			if(c.getContentItem().theItemId.getItem() instanceof ItemEnchantedBook && c.getGeneral())
			{
				WeightedRandomChestContent original = c.getContentItem();
				c = new LootItem(((ItemEnchantedBook)c.getContentItem().theItemId.getItem()).func_92112_a(LootBags.getRandom(), original.theMinimumChanceToGenerateItem, original.theMaximumChanceToGenerateItem, original.itemWeight), c.getGeneral());
			}
			list.add(c.getContentItem());
		}
		return list;
	}
	
	public static boolean isBagEmpty(int ID)
	{
		return bagList.get(ID).isBagEmpty();
	}
	
	public static ArrayList<Bag> getBagListRandomized()
	{
		ArrayList<Bag> list = new ArrayList<Bag>();
		list.addAll(bagList.values());
		Collections.shuffle(list, LootBags.getRandom());
		return list;
	}
	
	public static void generateBagRecipes(List recipeList)
	{
		for(Bag b: bagList.values())
		{
			if(b.getCraftingSource() != null)
			{
				Bag source = getBag(b.getCraftingSource());
				if(source != null)
				{
					int bagsneeded = (int)Math.ceil(b.getBagWeight()/source.getBagWeight());
					if(bagsneeded < 1)
						bagsneeded = 1;
					if(bagsneeded > 9)
						bagsneeded = 9;
					
					Object[] c = new Object[bagsneeded];
					for(int j = 0; j < c.length; j++)
					{
						c[j] = new ItemStack(LootBags.lootbagItem, 1, source.getBagIndex());
					}
					
					recipeList.add(new LootRecipe(b.getBagItem(), c));
				}
			}
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/