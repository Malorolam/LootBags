package mal.lootbags.loot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

import mal.lootbags.LootbagsUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class LootItem extends WeightedRandom.Item implements Comparable<LootItem>{

	private ItemStack item;
	private String modid;
	private String name;
	private LootEntryItem lootitem;
	private int damage;
	private int minstack;
	private int maxstack;
	//private int weight;
	private byte[] nbt;
	private boolean generalItem=false;
	
	/**
	 * The new LootItem, moved to the correct package, there is now no fixed loot sources, so this is just in whichever list is needed
	 * now it's more of a wrapper to be able to construct loot objects cleanly
	 */
	public LootItem(@Nullable LootEntryItem lootitem, ItemStack item, String modid, String itemname, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
		this.lootitem = lootitem;
		this.item = item;
		this.modid = modid;
		this.name = itemname;
		this.damage = item.getItemDamage();
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.generalItem = isgeneral;
		try {
			if(item.getTagCompound() != null)
				this.nbt=LootbagsUtil.compress(item.getTagCompound());
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	public LootItem(@Nullable LootEntryItem lootitem, ItemStack item, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
		this.lootitem = lootitem;
		try {
			this.modid = ForgeRegistries.ITEMS.getKey(item.getItem()).getResourceDomain();
			this.name = ForgeRegistries.ITEMS.getKey(item.getItem()).getResourcePath();
		} catch (Exception e) {
			LootbagsUtil.LogError("Mod/Item ID lookup failed for: " + item.toString() + ". This is likely an issue caused by a different mod.");
		}
		this.item = item;
		this.damage = item.getItemDamage();
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.generalItem = isgeneral;
		try {
			if(item.getTagCompound() != null)
				this.nbt=LootbagsUtil.compress(item.getTagCompound());
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * General method with base values
	 */
	public LootItem(@Nullable LootEntryItem lootitem, String modid, String itemname, int damage, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
		this.lootitem = lootitem;
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname)), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.nbt = null;
		this.generalItem = isgeneral;
		
		if(stack != null && !stack.isEmpty() && stack.getItem() != null)
		{
			stack.setCount(maxstack);
			stack.setItemDamage(damage);
			
			if(stack.getCount() > stack.getMaxStackSize())
				stack.setCount(stack.getMaxStackSize());
			if(stack.getCount() < 1)
			{
				stack.setCount(1);
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = stack;
		}
	}
	
	/**
	 * General method using NBT
	*/
	public LootItem(@Nullable LootEntryItem lootitem, String modid, String itemname, int damage, int minstack, int maxstack, int weight, byte[] nbt, boolean isgeneral)
	{
		super(weight);
		this.lootitem = lootitem;
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname)), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
		//this.weight = weight;
		this.nbt = nbt;
		this.generalItem = isgeneral;
		
		if(stack.isEmpty() || stack.getItem() == null)
		{
		//one of these should be not null
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, itemname));
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname));
		if(item != null)
			stack = new ItemStack(item,maxstack,damage);
		else if(block != null)
			stack = new ItemStack(block,maxstack,damage);
		}
		
		if(stack != null && !stack.isEmpty() && stack.getItem() != null)
		{
			stack.setCount(maxstack);
			stack.setItemDamage(damage);
			InputStream st = new ByteArrayInputStream(nbt); 
			NBTTagCompound tag;
			try {
				tag = CompressedStreamTools.readCompressed(st);
				stack.setTagCompound(tag);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(stack.getCount() > stack.getMaxStackSize())
				stack.setCount(stack.getMaxStackSize());
			if(stack.getCount() < 1)
			{
				stack.setCount(1);
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = stack;
		}
	}
	
	public void reinitializeLootItem()
	{
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name)), 1);
		if(stack.isEmpty() || stack.getItem() == null)
		{
		//one of these should be not null
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, name));
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name));
		if(item != null)
			stack = new ItemStack(item,maxstack,damage);
		else if(block != null)
			stack = new ItemStack(block,maxstack,damage);
		}
		
		if(stack != null && !stack.isEmpty() && stack != ItemStack.EMPTY && stack.getItem() != null)
		{
			stack.setCount(maxstack);
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
			
			if(stack.getCount() > stack.getMaxStackSize())
				stack.setCount(stack.getMaxStackSize());
			if(stack.getCount() < 1)
			{
				stack.setCount(1);
				LootbagsUtil.LogInfo("Stack size for whitelisted item: " + stack.toString() + " below 1.  Setting to 1.");
			}
			
			if(minstack > maxstack)
				minstack=maxstack;
			
			item = stack;
		}
		else
		{
			LootbagsUtil.LogInfo("Loot Item with name: " + modid + ":" + name + " did not find an item with that name and mod ID. Ensure that the information is correct.");
		}
	}
			
	public ItemStack getContentItem()
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
		return itemWeight;
	}
	
	public void setItemWeight(int weight)
	{
		itemWeight = weight;
	}
	
	public boolean getGeneral()
	{
		return generalItem;
	}
	
	public int getMinStack()
	{
		return minstack;
	}
	
	public int getMaxStack()
	{
		return maxstack;
	}
	
	public LootEntryItem getLootItem()
	{
		return lootitem;
	}
	
	@Override
	public String toString()
	{
		return item.toString() + ":" + minstack + ":" + maxstack + ":" + itemWeight;
	}
	
	public LootItem copy()
	{
		return new LootItem(this.lootitem, this.item, this.modid, this.name, this.minstack, this.maxstack, this.itemWeight, this.generalItem);
	}

	@Override
	public int compareTo(LootItem loot) {
		if(itemWeight > loot.getItemWeight())
			return 1;
		else if(itemWeight < loot.getItemWeight())
			return -1;
		else
			return 0;
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
