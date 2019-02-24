package mal.lootbags.tileentity;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.network.LootbagsPacketHandler;
import mal.lootbags.network.message.RecyclerMessageServer;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityRecycler extends TileEntity implements IInventory, ISidedInventory, ITickable{

	private ItemStack lootbagSlot = ItemStack.EMPTY;
	private int lootbagCount = 0;
	private int totalValue = 0;
	private ItemStack[] inventory = LootbagsUtil.getItemStackArrayEmpty(1);
	private NetworkRegistry.TargetPoint point;
	
	public TileEntityRecycler()
	{
		for(int i = 0; i < inventory.length; i++)
			inventory[i] = ItemStack.EMPTY;

	}
	
	@Override
	public void update()
	{
		if(world != null && !this.world.isRemote)
		{
			if(point==null)
				point = new NetworkRegistry.TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 16);
			//consume inventory to make a lootbag
			for(int i = 0; i < inventory.length; i++)
			{
				if(inventory[i] != null && inventory[i] != ItemStack.EMPTY) {
					int val = LootBags.getItemValue(inventory[i]);
					if(totalValue <= Integer.MAX_VALUE-val)
					{
						totalValue += val;
						inventory[i].shrink(1);
						if(inventory[i].getCount() <= 0)
							inventory[i] = ItemStack.EMPTY;
					}
				}
			}

			int mult = (int)Math.floor(totalValue/ LootBags.TOTALVALUE);
			if(totalValue >= LootBags.TOTALVALUE && lootbagCount < Integer.MAX_VALUE-mult)
			{
				totalValue -= LootBags.TOTALVALUE*mult;
				lootbagCount += mult;
			}
			else if(totalValue >= LootBags.TOTALVALUE && lootbagCount >= Integer.MAX_VALUE-mult)
			{
				int truemult = Integer.MAX_VALUE - lootbagCount-1;
				totalValue -= LootBags.TOTALVALUE*truemult;
				lootbagCount += truemult;
			}
			
			if(lootbagSlot == null || lootbagSlot.isEmpty() && lootbagCount > 0)
			{
				lootbagSlot = new ItemStack(LootBags.lootbagItem, 1, LootBags.RECYCLEDID);
				lootbagCount--;
			}
			if(lootbagCount <= 0)
				lootbagCount = 0;

			LootbagsPacketHandler.instance.sendToAllAround(new RecyclerMessageServer(this, lootbagCount, totalValue), point);
		}
	}
	
	public int getTotalBags()
	{
		return (lootbagCount + ((lootbagSlot!=null && !lootbagSlot.isEmpty())?(1):(0)));
	}
	
	public void setData(int count, int value)
	{
		lootbagCount = count;
		totalValue = value;
	}
	
	public int getValue()
	{
		return totalValue;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		lootbagCount = nbt.getInteger("lootbagCount");
		totalValue = nbt.getInteger("totalValue");
		
		NBTTagList lootbag = nbt.getTagList("lootbagItem", 10);
		NBTTagCompound var = lootbag.getCompoundTagAt(0);
		lootbagSlot = new ItemStack(var);
		
		
		NBTTagList input = nbt.getTagList("inputItems", 10);
		for (int i = 0; i < input.tagCount(); ++i)
		{
			NBTTagCompound var4 = input.getCompoundTagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.inventory.length)
			{
				this.inventory[var5] = new ItemStack(var4);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setInteger("lootbagCount", lootbagCount);
		nbt.setInteger("totalValue", totalValue);
		
		NBTTagList lootbag = new NBTTagList();
		if(lootbagSlot != null && !lootbagSlot.isEmpty())
		{
			NBTTagCompound var = new NBTTagCompound();
			var.setByte("Slot", (byte)0);
			lootbagSlot.writeToNBT(var);
			lootbag.appendTag(var);
		}
		nbt.setTag("lootbagItem", lootbag);
		
		NBTTagList input = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null && !this.inventory[i].isEmpty())
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(var4);
				input.appendTag(var4);
			}
		}
		nbt.setTag("inputItems", input);
		return nbt;
	}
	
	public NBTTagCompound getDropNBT()
	{
		int vp = (lootbagSlot.isEmpty())?(0):(1);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("lootbagCount", lootbagCount+vp);
		tag.setInteger("totalValue", totalValue);
		
		return tag;
	}
	
	@Override
	public int getSizeInventory() {
		return inventory.length+1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(slot == 0)
		{
			return lootbagSlot;
		}
		else if(slot < getSizeInventory())
			return inventory[slot-1];
		else
			return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int dec) {
		if(slot == 0)
		{
			if(lootbagSlot != null && !lootbagSlot.isEmpty())
			{
				ItemStack is;
				if(lootbagSlot.getCount() <= dec)
				{
					is = lootbagSlot;
					lootbagSlot = ItemStack.EMPTY;
					return is;
				}
				else
				{
					is = lootbagSlot.splitStack(dec);
					if(lootbagSlot.getCount() == 0)
						lootbagSlot = ItemStack.EMPTY;
					return is;
				}
			}
		}
		else if(slot < getSizeInventory())
		{
			if(inventory[slot-1] != null && !lootbagSlot.isEmpty())
			{
				ItemStack is;
				if(inventory[slot-1].getCount() <= dec)
				{
					is = inventory[slot-1];
					inventory[slot-1] = ItemStack.EMPTY;
					return is;
				}
				else
				{
					is = inventory[slot-1].splitStack(dec);
					if(inventory[slot-1].getCount() == 0)
						inventory[slot-1] = ItemStack.EMPTY;
					return is;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		if(slot==0)
		{
			this.lootbagSlot = item;

			if (item != null && !item.isEmpty() && item.getCount() > this.getInventoryStackLimit())
			{
				item.setCount(this.getInventoryStackLimit());
			}
		}
		else if(slot<getSizeInventory())
		{
				if (item != null && !item.isEmpty() && LootBags.isItemRecyclable(item) && inventory[slot-1].isEmpty()) {
					int val = LootBags.getItemValue(item)*item.getCount();
					if(totalValue <= Integer.MAX_VALUE-val) {
						totalValue += val;
					}
					else//not enough space, so filling the inventory
					{
						inventory[slot-1] = item;
					}
					int mult = (int)Math.floor(totalValue/ LootBags.TOTALVALUE);
					if(totalValue >= LootBags.TOTALVALUE && lootbagCount < Integer.MAX_VALUE-mult)
					{
						totalValue -= LootBags.TOTALVALUE*mult;
						lootbagCount += mult;
					}
				}
		}
		
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
		return this.world.getTileEntity(this.pos) != this ? false : par1EntityPlayer.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0)
			return false;
		else if(slot < getSizeInventory())
		{
			return LootBags.isItemRecyclable(stack);
		}
		return false;
	}

	public void activate(World world, int x, int y, int z, EntityPlayer player)
	{
		player.openGui(LootBags.LootBagsInstance, 1, world, x, y, z);
	}
	public void activate(World world, BlockPos pos, EntityPlayer player) {
		
		player.openGui(LootBags.LootBagsInstance, 1, world, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public String getName() {
		return "recycler";
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
	public int[] getSlotsForFace(EnumFacing side) {
		int[] i = new int[getSizeInventory()];
		for(int ii = 0; ii<i.length; ii++)
			i[ii]=ii;
		return i;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack,
			EnumFacing direction) {
		if(slot != 0 && LootBags.isItemDroppable(stack))
			return true;
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack,
			EnumFacing direction) {
		if(index == 0)
			return true;
		return false;
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

}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/