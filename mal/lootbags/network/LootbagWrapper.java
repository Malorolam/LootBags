package mal.lootbags.network;

import mal.lootbags.LootbagsUtil;
import mal.lootbags.item.LootbagItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

public class LootbagWrapper implements IInventory {

	private final ItemStack stack;
	private ItemStack[] inventory = LootbagsUtil.getItemStackArrayEmpty(5);
	
	public LootbagWrapper(ItemStack is)
	{
		stack = is;
		
		if (!this.stack.hasTagCompound()) 
		{
			this.stack.setTagCompound(new NBTTagCompound());
		}
		
		readFromNBT(this.stack.getTagCompound());
	}
	
	public ItemStack getStack()
	{
		return stack;
	}
	
	@Override
	public int getSizeInventory() {
		return 5;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack s = getStackInSlot(i);
		if(s != null)
		{
			if(s.getCount() > j)
			{
				s = s.splitStack(j);
				
				this.markDirty();
			}
			else
			{
				setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
		return s;
	}
	
	public ItemStack getItemInSlot(int slot)
	{
		return (inventory[slot]==null)?(inventory[slot]=ItemStack.EMPTY):(inventory[slot]);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		this.inventory[i] = (itemstack==null)?ItemStack.EMPTY:itemstack;

		if (itemstack != null && !itemstack.isEmpty() && itemstack.getCount() > this.getInventoryStackLimit())
		{
			itemstack.setCount(this.getInventoryStackLimit());
		}

		this.markDirty();
	}
	
	@Override
	public void markDirty()
	{
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty() && this.getStackInSlot(i).getCount() <= 0)
				this.setInventorySlotContents(i, ItemStack.EMPTY);
		}

		LootbagItem.setTagCompound(stack,inventory);
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		
		NBTTagList items = compound.getTagList("inventory", 10);

		for (int i = 0; i < items.tagCount(); ++i)
		{
			NBTTagCompound item = items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if (slot >= 0 && slot < getSizeInventory())
			{
				setInventorySlotContents(slot, new ItemStack(item));
			}
		}
	}

	/**
	 * A custom method to write our inventory to an ItemStack's NBT compound
	 */
	public void writeToNBT(NBTTagCompound compound)
	{
		// Create a new NBT Tag List to store itemstacks as NBT Tags
		NBTTagList items= new NBTTagList();

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			// Only write stacks that contain items
			if (getStackInSlot(i) != null)
			{
				// Make a new NBT Tag Compound to write the itemstack and slot index to
				NBTTagCompound item = new NBTTagCompound();
				item.setInteger("Slot", i);
				// Writes the itemstack in slot(i) to the Tag Compound we just made
				getStackInSlot(i).writeToNBT(item);

				// add the tag compound to our tag list
				items.appendTag(item);
			}
		}
		// Add the TagList to the ItemStack's Tag Compound with the name "ItemInventory"
		compound.setTag("inventory", items);
	}


	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return !LootbagItem.checkInventory(stack);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public String getName() {
		return "lootbagstack";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/