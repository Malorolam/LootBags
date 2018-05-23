package net.minecraft.world.storage.loot;

import java.lang.reflect.Field;

import mal.lootbags.LootBags;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.EnchantRandomly;
import net.minecraft.world.storage.loot.functions.EnchantWithLevels;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetDamage;
import net.minecraft.world.storage.loot.functions.SetMetadata;

/*
 * Access the protected fields in the loot package... because getters are apparently too hard to add in
 */
public class LootEntryItemAccess {

	public static int getLootEntryItemWeight(LootEntry item)
	{
		return item.weight;
	}
	
	public static ItemStack getLootEntryItemStack(LootEntryItem item)
	{
		ItemStack itemstack = new ItemStack(item.item);
        return applyMetadata(item, itemstack);
	}
	
	public static LootTable getLootTable(LootEntryTable table, LootContext context)
	{
		ResourceLocation rtable = table.table;
		return context.getLootTableManager().getLootTableFromLocation(rtable);
	}
	
	@SuppressWarnings("UnnecessaryLocalVariable")
	public static RandomValueRange getStackSizes(LootEntryItem item)
	{
		for(int i = 0; i < item.functions.length; i++)
		{
			LootFunction lootfunction = item.functions[i];
			
			if(lootfunction instanceof SetCount)
			{
				try
				{
					String cRng;
					if((boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
					{
						cRng = "countRange";
					}
					else
					{
						cRng = "field_186568_a";
					}
					Field countRangeField = SetCount.class.getDeclaredField(cRng);
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
	
	public static ItemStack applyFunctions(LootEntryItem item, ItemStack stack, LootContext context)
	{
		if(item == null)
			return stack;
		
		for (LootFunction lootfunction : item.functions)
        {
            if (LootConditionManager.testAllConditions(lootfunction.getConditions(), LootBags.getRandom(), context))
            {
                stack = lootfunction.apply(stack, LootBags.getRandom(), context);
            }
        }
		
		return stack;
	}
	
	public static boolean isItemEnchanted(LootEntryItem item)
	{
		if(item.functions.length == 0)
			return false;
		for(LootFunction func : item.functions)
		{
			if(func instanceof EnchantRandomly || func instanceof EnchantWithLevels)
				return true;
		}
		return false;
	}
	
	private static ItemStack applyMetadata(LootEntryItem item, ItemStack stack)
	{
		if(item == null)
			return stack;
		
		for (LootFunction lootfunction : item.functions)
		{
			if(lootfunction instanceof SetDamage || lootfunction instanceof SetMetadata)
			{
				stack = lootfunction.apply(stack, LootBags.getRandom(), null);
			}
		}
		
		return stack;
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/