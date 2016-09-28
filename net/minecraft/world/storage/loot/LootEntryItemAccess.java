package net.minecraft.world.storage.loot;

import java.lang.reflect.Field;

import mal.lootbags.LootBags;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;

/*
 * Access the protected fields in the loot package... because getters are apparently too hard to add in
 */
public class LootEntryItemAccess {

	public static int getLootEntryItemWeight(LootEntry item)
	{
		return item.weight;
	}
	
	public static ItemStack getLootEntryItemStack(LootEntryItem item, LootContext context)
	{
		ItemStack itemstack = new ItemStack(item.item);
		int i = 0;

        //applyFunctions(item, itemstack, context);

        return itemstack;
	}
	
	public static RandomValueRange getStackSizes(LootEntryItem item)
	{
		for(int i = 0; i < item.functions.length; i++)
		{
			LootFunction lootfunction = item.functions[i];
			
			if(lootfunction instanceof SetCount)
			{
				try
				{
					Field countRangeField = SetCount.class.getDeclaredField("countRange");
					countRangeField.setAccessible(true);
					RandomValueRange range = (RandomValueRange)countRangeField.get(lootfunction);
					return range;
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void applyFunctions(LootEntryItem item, ItemStack stack, LootContext context)
	{
		if(item == null)
			return;
		
		for (LootFunction lootfunction : item.functions)
        {
            if (LootConditionManager.testAllConditions(lootfunction.getConditions(), LootBags.getRandom(), context))
            {
                stack = lootfunction.apply(stack, LootBags.getRandom(), context);
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