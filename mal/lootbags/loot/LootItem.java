package mal.lootbags.loot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

import mal.lootbags.LootbagsUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandomChestContent;

public class LootItem implements Comparable{

	private WeightedRandomChestContent item;
	private String modid;
	private String name;
	private int damage;
	private int minstack;
	private int maxstack;
	private int weight;
	private byte[] nbt;
	private boolean generalItem=false;
	
	/**
	 * The new LootItem, moved to the correct package, there is now no fixed loot sources, so this is just in whichever list is needed
	 * now it's more of a wrapper to be able to construct weightedrandomchestcontent objects cleanly
	 */
	public LootItem(WeightedRandomChestContent item, boolean isgeneral)
	{
		this.item = item;
		this.modid = GameRegistry.findUniqueIdentifierFor(item.theItemId.getItem()).modId;
		this.name = GameRegistry.findUniqueIdentifierFor(item.theItemId.getItem()).name;
		this.damage = item.theItemId.getItemDamage();
		this.minstack = item.theMinimumChanceToGenerateItem;
		this.maxstack = item.theMaximumChanceToGenerateItem;
		this.weight = item.itemWeight;
		this.generalItem = isgeneral;
		try {
			if(item.theItemId.getTagCompound() != null)
				this.nbt=CompressedStreamTools.compress(item.theItemId.getTagCompound());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * General method with base values
	 */
	public LootItem(String modid, String itemname, int damage, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		ItemStack stack = new ItemStack(GameRegistry.findItem(modid, itemname), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.weight = weight;
		this.nbt = null;
		this.generalItem = isgeneral;
		
		if(stack != null && stack.getItem() != null)
		{
			stack.stackSize = maxstack;
			stack.setItemDamage(damage);
			
			if(stack.stackSize > stack.getMaxStackSize())
				stack.stackSize = stack.getMaxStackSize();
			if(stack.stackSize < 1)
			{
				stack.stackSize=1;
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = new WeightedRandomChestContent(stack, minstack, stack.stackSize, weight);
		}
	}
	
	/**
	 * General method using NBT
	*/
	public LootItem(String modid, String itemname, int damage, int minstack, int maxstack, int weight, byte[] nbt, boolean isgeneral)
	{
		ItemStack stack = new ItemStack(GameRegistry.findItem(modid, itemname), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.weight = weight;
		this.nbt = nbt;
		this.generalItem = isgeneral;
		
		if(stack.getItem() == null)
		{
		//one of these should be not null
		Block block = GameRegistry.findBlock(modid, itemname);
		Item item = GameRegistry.findItem(modid, itemname);
		if(item != null)
			stack = new ItemStack(item,maxstack,damage);
		else if(block != null)
			stack = new ItemStack(block,maxstack,damage);
		}
		
		if(stack != null && stack.getItem() != null)
		{
			stack.stackSize = maxstack;
			stack.setItemDamage(damage);
			InputStream st = new ByteArrayInputStream(nbt); 
			NBTTagCompound tag;
			try {
				tag = CompressedStreamTools.readCompressed(st);
				stack.setTagCompound(tag);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(stack.stackSize > stack.getMaxStackSize())
				stack.stackSize = stack.getMaxStackSize();
			if(stack.stackSize < 1)
			{
				stack.stackSize=1;
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = new WeightedRandomChestContent(stack, minstack, stack.stackSize, weight);
		}
	}
	
	public void reinitializeLootItem()
	{
		ItemStack stack = new ItemStack(GameRegistry.findItem(modid, name), 1);
		if(stack.getItem() == null)
		{
		//one of these should be not null
		Block block = GameRegistry.findBlock(modid, name);
		Item item = GameRegistry.findItem(modid, name);
		if(item != null)
			stack = new ItemStack(item,maxstack,damage);
		else if(block != null)
			stack = new ItemStack(block,maxstack,damage);
		}
		
		if(stack != null && stack.getItem() != null)
		{
			stack.stackSize = maxstack;
			stack.setItemDamage(damage);
			
			if(nbt != null)
			{
				InputStream st = new ByteArrayInputStream(nbt); 
				NBTTagCompound tag;
				try {
					tag = CompressedStreamTools.readCompressed(st);
					stack.setTagCompound(tag);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(stack.stackSize > stack.getMaxStackSize())
				stack.stackSize = stack.getMaxStackSize();
			if(stack.stackSize < 1)
			{
				stack.stackSize=1;
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = new WeightedRandomChestContent(stack, minstack, stack.stackSize, weight);
		}
		else
		{
			LootbagsUtil.LogInfo("Loot Item with name: " + modid + ":" + name + " did not find an item with that name and mod ID. Ensure that the information is correct.");
		}
	}
			
	public WeightedRandomChestContent getContentItem()
	{
		return item;
	}
	
	public String getItemName()
	{
		return name;
	}
	
	public String getItemModID()
	{
		return modid;
	}
	
	public int getItemWeight()
	{
		return item.itemWeight;
	}
	
	public boolean getGeneral()
	{
		return generalItem;
	}
	
	public String toString()
	{
		return item.theItemId.toString() + ":" + minstack + ":" + maxstack + ":" + weight;
	}
	
	public LootItem copy()
	{
		return new LootItem(this.item, this.generalItem);
	}

	@Override
	public int compareTo(Object loot) {
		if(!(loot instanceof LootItem))
			return 0;
		
		if(item.itemWeight > ((LootItem)loot).getItemWeight())
			return 1;
		else if(item.itemWeight < ((LootItem)loot).getItemWeight())
			return -1;
		else
			return 0;
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
