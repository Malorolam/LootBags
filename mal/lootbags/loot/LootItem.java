package mal.lootbags.loot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LootItem extends WeightedRandom.Item implements Comparable{

	private ItemStack item;
	private String modid;
	private String name;
	private int damage;
	private int minstack;
	private int maxstack;
	//private int weight;
	private byte[] nbt;
	private boolean generalItem=false;
	
	/**
	 * The new LootItem, moved to the correct package, there is now no fixed loot sources, so this is just in whichever list is needed
	 * now it's more of a wrapper to be able to construct weightedrandomchestcontent objects cleanly
	 */
	public LootItem(ItemStack item, String modid, String itemname, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
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
			e.printStackTrace();
		}
	}
	
	public LootItem(ItemStack item, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
		this.modid = ForgeRegistries.ITEMS.getKey(item.getItem()).getResourceDomain();
		this.name = ForgeRegistries.ITEMS.getKey(item.getItem()).getResourcePath();
		this.item = item;
		this.damage = item.getItemDamage();
		this.minstack = minstack;
		this.maxstack = maxstack;
		this.generalItem = isgeneral;
		try {
			if(item.getTagCompound() != null)
				this.nbt=LootbagsUtil.compress(item.getTagCompound());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * General method with base values
	 */
	public LootItem(String modid, String itemname, int damage, int minstack, int maxstack, int weight, boolean isgeneral)
	{
		super(weight);
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname)), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
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
			
			item = stack;
		}
	}
	
	/**
	 * General method using NBT
	*/
	public LootItem(String modid, String itemname, int damage, int minstack, int maxstack, int weight, byte[] nbt, boolean isgeneral)
	{
		super(weight);
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname)), 1);
		this.modid = modid;
		this.name = itemname;
		this.damage = damage;
		this.minstack = minstack;
		this.maxstack = maxstack;
		//this.weight = weight;
		this.nbt = nbt;
		this.generalItem = isgeneral;
		
		if(stack.getItem() == null)
		{
		//one of these should be not null
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, itemname));
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemname));
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
			
			item = stack;
		}
	}
	
	public void reinitializeLootItem()
	{
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name)), 1);
		if(stack.getItem() == null)
		{
		//one of these should be not null
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modid, name));
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, name));
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
	
	public String toString()
	{
		return item.toString() + ":" + minstack + ":" + maxstack + ":" + itemWeight;
	}
	
	public LootItem copy()
	{
		return new LootItem(this.item, this.modid, this.name, this.minstack, this.maxstack, this.itemWeight, this.generalItem);
	}

	@Override
	public int compareTo(Object loot) {
		if(!(loot instanceof LootItem))
			return 0;
		
		if(itemWeight > ((LootItem)loot).getItemWeight())
			return 1;
		else if(itemWeight < ((LootItem)loot).getItemWeight())
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
