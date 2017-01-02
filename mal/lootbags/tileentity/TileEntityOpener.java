package mal.lootbags.tileentity;

import mal.lootbags.LootBags;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagsPacketHandler;
import mal.lootbags.network.message.OpenerMessageServer;
import mal.lootbags.network.message.RecyclerMessageServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityOpener extends TileEntity implements IInventory, ISidedInventory, ITickable{

	private ItemStack[] lootbagInventory = new ItemStack[9];
	private int cooldown = 0;
	private ItemStack[] inventory = new ItemStack[27];
	
	@Override
	public String getName() {
		return "opener";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public void update() {
		if(worldObj != null && !worldObj.isRemote)
		{
			//if not cooled down yet, don't do anything
			if(cooldown > 0)
				cooldown--;
			else
			{
				//cycle through the inventory and open bags
				boolean opened=false;
				int invslot = 0;
				while(!opened && invslot < lootbagInventory.length)
				{
					ItemStack stack = lootbagInventory[invslot];
					if(stack==null)
						invslot++;
					else if(!(stack.getItem() instanceof LootbagItem))//not a lootbag somehow
					{
						insertItemToOutput(stack);
						invslot++;
					}
					else//lootbag
					{
						LootbagItem.generateInventory(stack);
						ItemStack[] llist = LootbagItem.getInventory(stack);
						for(int i=0; i < llist.length; i++)
						{
							llist[i] = insertItemToOutput(llist[i]);
						}
						
						LootbagItem.setTagCompound(stack, llist);
						if(LootbagItem.checkInventory(stack))//empty bag now
						{
							cooldown = LootBags.OPENERMAXCOOLDOWN;
							setInventorySlotContents(invslot, null);
							if(LootBags.OPENERMAXCOOLDOWN>0)
								opened = true;
						}
						else
							invslot++;
					}
				}
			}
			
			LootbagsPacketHandler.instance.sendToAll(new OpenerMessageServer(this, cooldown));
		}
	}

	private ItemStack insertItemToOutput(ItemStack is)
	{
		if(is==null)
			return null;
		for(int i = 0; i < inventory.length; i++)
		{
			if(inventory[i] == null)
			{
				inventory[i] = is.copy();
				return null;
			}
			else if(LootBags.areItemStacksEqualItem(is, inventory[i], true, true))
			{
				if(inventory[i].stackSize<inventory[i].getMaxStackSize())
				{
					if(inventory[i].stackSize+is.stackSize<=inventory[i].getMaxStackSize())
					{
						inventory[i].stackSize += is.stackSize;
						return null;
					}
					else
					{
						int diff = inventory[i].stackSize+is.stackSize-inventory[i].getMaxStackSize();
						inventory[i].stackSize = inventory[i].getMaxStackSize();
						is.stackSize=diff;
						return is.copy();
					}
				}
			}
		}
		return is;
	}
	
	public void activate(World world, BlockPos pos, EntityPlayer player) {
		
		player.openGui(LootBags.LootBagsInstance, 2, world, pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		cooldown = nbt.getInteger("cooldown");
		
		NBTTagList input = nbt.getTagList("inputItems", 10);
		for (int i = 0; i < input.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound)input.getCompoundTagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.lootbagInventory.length)
			{
				this.lootbagInventory[var5] = ItemStack.loadItemStackFromNBT(var4);
			}
		}
		
		NBTTagList output = nbt.getTagList("outputItems", 10);
		for (int i = 0; i < output.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound)output.getCompoundTagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.inventory.length)
			{
				this.inventory[var5] = ItemStack.loadItemStackFromNBT(var4);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("cooldown", cooldown);
		
		NBTTagList input = new NBTTagList();

		for (int i = 0; i < this.lootbagInventory.length; ++i)
		{
			if (this.lootbagInventory[i] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.lootbagInventory[i].writeToNBT(var4);
				input.appendTag(var4);
			}
		}
		nbt.setTag("inputItems", input);
		
		NBTTagList output = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(var4);
				output.appendTag(var4);
			}
		}
		nbt.setTag("outputItems", output);
		return nbt;
	}
	
	public int getCooldown()
	{
		return cooldown;
	}
	
	public void setData(int cd)
	{
		cooldown = cd;
	}
	
	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		int[] i = new int[getSizeInventory()];
		for(int ii = 0; ii<i.length; ii++)
			i[ii]=ii;
		return i;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		if(itemStackIn.getItem() instanceof LootbagItem && index >= 0 && index < lootbagInventory.length)
			return true;
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		if(index>=lootbagInventory.length && index<getSizeInventory())
			return true;
		return false;
	}

	@Override
	public int getSizeInventory() {
		return lootbagInventory.length+inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if(index >= 0 && index < lootbagInventory.length)
			return lootbagInventory[index];
		else if(index < getSizeInventory())
			return inventory[index-lootbagInventory.length];
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (this.getStackInSlot(index) != null) {
	        ItemStack itemstack;

	        if (this.getStackInSlot(index).stackSize <= count) {
	            itemstack = this.getStackInSlot(index);
	            this.setInventorySlotContents(index, null);
	            this.markDirty();
	            return itemstack;
	        } else {
	            itemstack = this.getStackInSlot(index).splitStack(count);

	            if (this.getStackInSlot(index).stackSize <= 0) {
	                this.setInventorySlotContents(index, null);
	            } else {
	                this.setInventorySlotContents(index, this.getStackInSlot(index));
	            }

	            this.markDirty();
	            return itemstack;
	        }
	    } else {
	        return null;
	    }
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = this.getInventoryStackLimit();
		if(stack != null && stack.stackSize <= 0)
			stack = null;
		
		if(index >= 0 && index < lootbagInventory.length)
			lootbagInventory[index] = stack;
		else if(index < getSizeInventory())
			inventory[index-lootbagInventory.length] = stack;
		
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index>=0 && index<lootbagInventory.length && stack.getItem() instanceof LootbagItem)
			return true;//input inventory
		return false;
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
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new SidedInvWrapper(this, facing);
		}
		return super.getCapability(capability, facing);
	}

}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/