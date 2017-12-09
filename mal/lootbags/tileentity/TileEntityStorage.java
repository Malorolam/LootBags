package mal.lootbags.tileentity;

import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagsPacketHandler;
import mal.lootbags.network.message.StorageMessageClient;
import mal.lootbags.network.message.StorageMessageServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityStorage extends TileEntity implements IInventory, ISidedInventory, ITickable{

	private int stored_value;
	private int outputID, outputindex;
	private ItemStack[] input_inventory;
	//private ItemStack output_inventory;
	private ArrayList<Integer> outputIDlist;
	
	public TileEntityStorage()
	{
		input_inventory = new ItemStack[1];
		input_inventory[0] = ItemStack.EMPTY;
		outputIDlist = new ArrayList<Integer>();
		outputIDlist.addAll(BagHandler.getExtractedBagList());
		outputindex = 0;
		outputID = outputIDlist.get(outputindex);
		//output_inventory = new ItemStack(LootBags.lootbagItem,1,outputID);
	}
	
	public void activate(World world, BlockPos pos, EntityPlayer player) {
		player.openGui(LootBags.LootBagsInstance, 3, world, pos.getX(), pos.getY(), pos.getZ());
	}
	

	@Override
	public void update() {
		if(world != null && !this.world.isRemote)
		{
			//make sure the output slot has a bag in it
			/*if(output_inventory == null || output_inventory.isEmpty())
				if(stored_value >= BagHandler.getBagValue(outputID)[1])
				{
					stored_value -= BagHandler.getBagValue(outputID)[1];
					output_inventory = new ItemStack(LootBags.lootbagItem, 1, outputID);
				}
			*/
			LootbagsPacketHandler.instance.sendToAll(new StorageMessageServer(this, stored_value, outputID, outputindex));
		}
	}
	
	public void setDataClient(int value, int ID, int index)
	{
		stored_value = value;
		outputID = ID;
		outputindex = index;
	}
	
	public void setDataServer(int ID, int index) {
		outputID = ID;
		outputindex = index;
		this.markDirty();
	}
	
	public void setOutputID(int ID)
	{
		if(!BagHandler.isIDFree(ID))
			outputID = ID;
	}
	
	public void cycleOutputID(boolean direction)
	{
		if(direction==true)
		{
			outputindex++;
			if(outputindex == outputIDlist.size())
				outputindex = 0;
		}
		else
		{
			outputindex--;
			if(outputindex < 0)
				outputindex = outputIDlist.size()-1;
		}
		outputID = outputIDlist.get(outputindex);
		if(world.isRemote)
			LootbagsPacketHandler.instance.sendToServer(new StorageMessageClient(this, outputID, outputindex));
	}
	
	public int getStorage()
	{
		return stored_value;
	}
	
	public ItemStack getOutputStack()
	{
		return new ItemStack(LootBags.lootbagItem, 1, outputID);//output_inventory;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		outputID = nbt.getInteger("outputID");
		stored_value = nbt.getInteger("totalValue");
		outputindex = nbt.getInteger("outputindex");
		
		NBTTagList input = nbt.getTagList("inputItems", 10);
		for (int i = 0; i < input.tagCount(); ++i)
		{
			NBTTagCompound var4 = input.getCompoundTagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.input_inventory.length)
			{
				this.input_inventory[var5] = new ItemStack(var4);
			}
		}
		
		//NBTTagList olootbag = nbt.getTagList("outputItem", 10);
		//NBTTagCompound var3 = (NBTTagCompound)olootbag.getCompoundTagAt(0);
		//output_inventory = new ItemStack(var3);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("outputID", outputID);
		nbt.setInteger("totalValue", stored_value);
		nbt.setInteger("outputindex", outputindex);
		
		NBTTagList input = new NBTTagList();
		for (int i = 0; i < this.input_inventory.length; ++i)
		{
			if (this.input_inventory[i] != null && !this.input_inventory[i].isEmpty())
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.input_inventory[i].writeToNBT(var4);
				input.appendTag(var4);
			}
		}
		nbt.setTag("inputItems", input);
		
		/*NBTTagList olootbag = new NBTTagList();
		if(output_inventory != null && !output_inventory.isEmpty())
		{
			NBTTagCompound var = new NBTTagCompound();
			var.setByte("Slot", (byte)0);
			output_inventory.writeToNBT(var);
			olootbag.appendTag(var);
		}
		nbt.setTag("outputItem", olootbag);*/
		
		return nbt;
	}
	
	public NBTTagCompound getDropNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("outputID", outputID);
		tag.setInteger("stored_value", stored_value);
		tag.setInteger("outputindex", outputindex);
		
		return tag;
	}
	
	@Override
	public String getName() {
		return "storage";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] {0, 1};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		if(!(itemStackIn.getItem() instanceof LootbagItem))
			return false;
		if(LootBags.PREVENTMERGEDBAGS)
		{
			if (!BagHandler.isBagOpened(itemStackIn) && BagHandler.isBagInsertable(itemStackIn.getMetadata()))
			{
				if(stored_value+BagHandler.getBagValue(itemStackIn.getMetadata())[0] == Integer.MAX_VALUE || stored_value+BagHandler.getBagValue(itemStackIn.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
		else
		{
			if (BagHandler.isBagInsertable(itemStackIn.getMetadata()))
			{
				if(stored_value+BagHandler.getBagValue(itemStackIn.getMetadata())[0] == Integer.MAX_VALUE || stored_value+BagHandler.getBagValue(itemStackIn.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
		return false;
			
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return input_inventory.length+1;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if(index==0)
		{
			if(stored_value >= BagHandler.getBagValue(outputID)[1])
			{
				//stored_value -= BagHandler.getBagValue(outputID)[1];
				return new ItemStack(LootBags.lootbagItem, 1, outputID);
			}
			else
				return ItemStack.EMPTY;
		}
		else
			return input_inventory[index-1];
	}

	@Override
	public ItemStack decrStackSize(int slot, int dec) {
		if(slot == 0)
		{
			int value = BagHandler.getBagValue(outputID)[1];
			if(stored_value >= value)
			{
				stored_value -= value;
				return new ItemStack(LootBags.lootbagItem, 1, outputID);
			}
		}
		else
		{
			if(input_inventory[slot-1] != null)
			{
				ItemStack is;
				if(input_inventory[slot-1].getCount() <= dec)
				{
					is = input_inventory[slot-1];
					input_inventory[slot-1] = ItemStack.EMPTY;
					return is;
				}
				else
				{
					is = input_inventory[slot-1].splitStack(dec);
					if(input_inventory[slot-1].getCount() == 0)
						input_inventory[slot-1] = ItemStack.EMPTY;
					return is;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if(index==0)
		{
			int value = BagHandler.getBagValue(outputID)[1];
			if(stored_value >= value)
			{
				stored_value -= value;
				return new ItemStack(LootBags.lootbagItem, 1, outputID);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		//if(world != null && !this.world.isRemote)
		{
			if(index==0)
			{
				
			}
			else if(index<getSizeInventory())
			{
				if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof LootbagItem))
					return;
				
				int value = BagHandler.getBagValue(stack)[0];
				if(value < 1)
					return;
				stored_value += value;
			}
			
			this.markDirty();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(!(stack.getItem() instanceof LootbagItem))
			return false;
		if(LootBags.PREVENTMERGEDBAGS)
		{
			if (!BagHandler.isBagOpened(stack) && BagHandler.isBagInsertable(stack.getMetadata()))
			{
				if(stored_value+BagHandler.getBagValue(stack.getMetadata())[0] >= Integer.MAX_VALUE || stored_value+BagHandler.getBagValue(stack.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
		else
		{
			if (BagHandler.isBagInsertable(stack.getMetadata()))
			{
				if(stored_value+BagHandler.getBagValue(stack.getMetadata())[0] >= Integer.MAX_VALUE || stored_value+BagHandler.getBagValue(stack.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
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
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	public int getID() {
		return outputID;
	}

	public void decrStorage(ItemStack is) {
		if(is != null && !is.isEmpty() && is.getItem() instanceof LootbagItem)
		{
			int value = BagHandler.getBagValue(is.getMetadata())[1];
			if(stored_value >= value)
				stored_value -= value;
			else
				stored_value = 0;
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/