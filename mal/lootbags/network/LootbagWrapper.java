package mal.lootbags.network;

import mal.lootbags.item.LootbagItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class LootbagWrapper implements IInventory {

	private final ItemStack stack;
	private ItemStack[] inventory = new ItemStack[5];
	public int Invslot;
	
	public LootbagWrapper(ItemStack is, int slot)
	{
		stack = is;
		Invslot = slot;
		
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
			if(s.stackSize > j)
			{
				s = s.splitStack(j);
				
				this.markDirty();
			}
			else
			{
				setInventorySlotContents(i, null);
			}
		}
		return s;
	}
	
	public ItemStack getItemInSlot(int slot)
	{
		return inventory[slot];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		this.inventory[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}

		this.markDirty();
	}
	
	@Override
	public void markDirty()
	{
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null && this.getStackInSlot(i).stackSize <= 0)
				this.setInventorySlotContents(i, null);
		}

		LootbagItem.setTagCompound(stack,inventory);
	}

	public void readFromNBT(NBTTagCompound compound)
	{
		
		NBTTagList items = compound.getTagList("inventory", 10);

		for (int i = 0; i < items.tagCount(); ++i)
		{
			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if (slot >= 0 && slot < getSizeInventory())
			{
				setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public void openInventory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub
		this.markDirty();
	}

	@Override
	public String getInventoryName() {
		// TODO Auto-generated method stub
		return "lootbagstack";
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO Auto-generated method stub
		return false;
	}

}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/