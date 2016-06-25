package mal.lootbags.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LootbagContainer extends Container{

	public LootbagWrapper wrapper;
	public InventoryPlayer player;
	private int islot;
	
    private int field_94535_f = -1;
    private int field_94536_g;
    private final Set<Slot> field_94537_h = new HashSet<Slot>();
	
	public LootbagContainer(InventoryPlayer player, LootbagWrapper wrap)
	{
		wrapper=wrap;
		this.player=player;
		islot = player.currentItem;
		
		for(int i = 0; i < wrap.getSizeInventory(); i++)
		{
			this.addSlotToContainer(new LootbagSlot(wrapper,i,44+i*18, 15));
		}
		
		//main inventory
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
            	if(LootBags.areItemStacksEqualItem(player.getStackInSlot(j+i*9+9), wrapper.getStack(), true, false))
            		this.addSlotToContainer(new FixedSlot(player, j+ i*9+9, 8 + j * 18, 46 + i*18));
            	else
            		this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 46 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
        	if(LootBags.areItemStacksEqualItem(player.getStackInSlot(i), wrapper.getStack(), true, false))
        		this.addSlotToContainer(new FixedSlot(player, i, 8 + i * 18, 103));
        	else
        		this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 103));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return true;
	}

	public void detectAndSendChanges()
    {
		super.detectAndSendChanges();
		
		if(LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false))
		{
			if(LootbagItem.checkInventory(wrapper.getStack()))
			{
				player.mainInventory[islot] = null;
			}
			else
			{
				player.mainInventory[islot] = wrapper.getStack();
			}
		}
    }
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			if(LootBags.areItemStacksEqualItem(player.inventory.mainInventory[islot], wrapper.getStack(), true, false))
			{
				if(LootbagItem.checkInventory(wrapper.getStack()))
				{
					player.inventory.mainInventory[islot] = null;
				}
				else
				{
					player.inventory.mainInventory[islot] = wrapper.getStack();
				}
			}
			else
			{
				//player.dropPlayerItemWithRandomChoice(((Slot)this.inventorySlots.get(0)).getStack(), false);
			}
		}
		super.onContainerClosed(player);
	}
	
	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
    {
    	ItemStack var3 = null;
    	Slot var4 = null;
    	if(this.inventorySlots.get(slot) instanceof FixedSlot)
    		var4 = (FixedSlot)this.inventorySlots.get(slot);
    	else
    		var4 = (Slot)this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot>=0 && slot <5)//inventory
            {
            	if (!this.mergeItemStack(var5, 5, 41, true))
                {
                    return null;
                }

                var4.onSlotChange(var5, var3);

            }
            
            if (var5.stackSize == 0)
            {
                var4.putStack((ItemStack)null);
            }
            else
            {
                var4.onSlotChanged();
            }

            if (var5.stackSize == var3.stackSize)
            {
                return null;
            }

            var4.onPickupFromSlot(par1EntityPlayer, var5);
        }
        
        return var3;
    }
    
    @Override
    public ItemStack slotClick(int p_75144_1_, int p_75144_2_, int p_75144_3_, EntityPlayer eplayer)
    {
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = eplayer.inventory;
        int i1;
        ItemStack itemstack3;
        
        if(!LootBags.areItemStacksEqualItem(eplayer.inventory.mainInventory[islot], wrapper.getStack(), true, false))
        {
        	eplayer.closeScreen();
        	//LootbagsUtil.LogInfo("Missing Lootbag");
        	return null;
        }

        if (p_75144_3_ == 5)
        {
            int l = this.field_94536_g;
            this.field_94536_g = func_94532_c(p_75144_2_);

            if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g)
            {
                this.func_94533_d();
            }
            else if (inventoryplayer.getItemStack() == null)
            {
                this.func_94533_d();
            }
            else if (this.field_94536_g == 0)
            {
                this.field_94535_f = func_94529_b(p_75144_2_);

                if (func_94528_d(this.field_94535_f))
                {
                    this.field_94536_g = 1;
                    this.field_94537_h.clear();
                }
                else
                {
                    this.func_94533_d();
                }
            }
            else if (this.field_94536_g == 1)
            {
                Slot slot = null;
                if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	slot = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                else
                	slot = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot != null && func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size() && this.canDragIntoSlot(slot))
                {
                    this.field_94537_h.add(slot);
                }
            }
            else if (this.field_94536_g == 2)
            {
                if (!this.field_94537_h.isEmpty())
                {
                    itemstack3 = inventoryplayer.getItemStack().copy();
                    i1 = inventoryplayer.getItemStack().stackSize;
                    Iterator<Slot> iterator = this.field_94537_h.iterator();

                    while (iterator.hasNext())
                    {
                        Slot slot1 = iterator.next();

                        if (slot1 != null && func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size() && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack1 = itemstack3.copy();
                            int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                            func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);

                            if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                            {
                                itemstack1.stackSize = itemstack1.getMaxStackSize();
                            }

                            if (itemstack1.stackSize > slot1.getSlotStackLimit())
                            {
                                itemstack1.stackSize = slot1.getSlotStackLimit();
                            }

                            i1 -= itemstack1.stackSize - j1;
                            slot1.putStack(itemstack1);
                        }
                    }

                    itemstack3.stackSize = i1;

                    if (itemstack3.stackSize <= 0)
                    {
                        itemstack3 = null;
                    }

                    inventoryplayer.setItemStack(itemstack3);
                }

                this.func_94533_d();
            }
            else
            {
                this.func_94533_d();
            }
        }
        else if (this.field_94536_g != 0)
        {
            this.func_94533_d();
        }
        else
        {
            Slot slot2;
            int l1;
            ItemStack itemstack5;

            if ((p_75144_3_ == 0 || p_75144_3_ == 1) && (p_75144_2_ == 0 || p_75144_2_ == 1))
            {
                if (p_75144_1_ == -999)
                {
                    if (inventoryplayer.getItemStack() != null && p_75144_1_ == -999)
                    {
                        if (p_75144_2_ == 0)
                        {
                            eplayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
                            inventoryplayer.setItemStack((ItemStack)null);
                        }

                        if (p_75144_2_ == 1)
                        {
                            eplayer.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

                            if (inventoryplayer.getItemStack().stackSize == 0)
                            {
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                }
                else if (p_75144_3_ == 1)
                {
                    if (p_75144_1_ < 0)
                    {
                        return null;
                    }

                    if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                    	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                    else
                    	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                    if (slot2 != null && slot2.canTakeStack(eplayer))
                    {
                        itemstack3 = this.transferStackInSlot(eplayer, p_75144_1_);

                        if (itemstack3 != null)
                        {
                            Item item = itemstack3.getItem();
                            itemstack = itemstack3.copy();

                            if (slot2.getStack() != null && slot2.getStack().getItem() == item)
                            {
                                this.retrySlotClick(p_75144_1_, p_75144_2_, true, eplayer);
                            }
                        }
                    }
                }
                else
                {
                    if (p_75144_1_ < 0)
                    {
                        return null;
                    }

                    if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                    	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                    else
                    	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);
                    
                    if (slot2 != null)
                    {
                        itemstack3 = slot2.getStack();
                        ItemStack itemstack4 = inventoryplayer.getItemStack();

                        if (itemstack3 != null)
                        {
                            itemstack = itemstack3.copy();
                        }

                        if (itemstack3 == null)
                        {
                            if (itemstack4 != null && slot2.isItemValid(itemstack4))
                            {
                                l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;

                                if (l1 > slot2.getSlotStackLimit())
                                {
                                    l1 = slot2.getSlotStackLimit();
                                }

                                if (itemstack4.stackSize >= l1)
                                {
                                    slot2.putStack(itemstack4.splitStack(l1));
                                }

                                if (itemstack4.stackSize == 0)
                                {
                                    inventoryplayer.setItemStack((ItemStack)null);
                                }
                            }
                        }
                        else if (slot2.canTakeStack(eplayer))
                        {
                            if (itemstack4 == null)
                            {
                                l1 = p_75144_2_ == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
                                itemstack5 = slot2.decrStackSize(l1);
                                inventoryplayer.setItemStack(itemstack5);

                                if (itemstack3.stackSize == 0)
                                {
                                    slot2.putStack((ItemStack)null);
                                }

                                slot2.onPickupFromSlot(eplayer, inventoryplayer.getItemStack());
                            }
                            else if (slot2.isItemValid(itemstack4))
                            {
                                if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                                {
                                    l1 = p_75144_2_ == 0 ? itemstack4.stackSize : 1;

                                    if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
                                    {
                                        l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
                                    }

                                    if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize)
                                    {
                                        l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
                                    }

                                    itemstack4.splitStack(l1);

                                    if (itemstack4.stackSize == 0)
                                    {
                                        inventoryplayer.setItemStack((ItemStack)null);
                                    }

                                    itemstack3.stackSize += l1;
                                }
                                else if (itemstack4.stackSize <= slot2.getSlotStackLimit())
                                {
                                    slot2.putStack(itemstack4);
                                    inventoryplayer.setItemStack(itemstack3);
                                }
                            }
                            else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                            {
                                l1 = itemstack3.stackSize;

                                if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize())
                                {
                                    itemstack4.stackSize += l1;
                                    itemstack3 = slot2.decrStackSize(l1);

                                    if (itemstack3.stackSize == 0)
                                    {
                                        slot2.putStack((ItemStack)null);
                                    }

                                    slot2.onPickupFromSlot(eplayer, inventoryplayer.getItemStack());
                                }
                            }
                        }

                        slot2.onSlotChanged();
                    }
                }
            }
            else if (p_75144_3_ == 2 && p_75144_2_ >= 0 && p_75144_2_ < 9)
            {
            	if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                else
                	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);
            	
            	Slot st = null;
            	if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	st = (FixedSlot)this.inventorySlots.get(p_75144_2_);
                else
                	st = (Slot)this.inventorySlots.get(p_75144_2_);

                itemstack3 = inventoryplayer.getStackInSlot(p_75144_2_);
                if (slot2.canTakeStack(eplayer) && ((itemstack3 != null)?(!(itemstack3.getItem() instanceof LootbagItem)):(true)))
                {
                    boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
                    l1 = -1;

                    if (!flag)
                    {
                        l1 = inventoryplayer.getFirstEmptyStack();
                        flag |= l1 > -1;
                    }

                    if (slot2.getHasStack() && flag)
                    {
                        itemstack5 = slot2.getStack();
                        inventoryplayer.setInventorySlotContents(p_75144_2_, itemstack5.copy());

                        if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null)
                        {
                            if (l1 > -1)
                            {
                                inventoryplayer.addItemStackToInventory(itemstack3);
                                slot2.decrStackSize(itemstack5.stackSize);
                                slot2.putStack((ItemStack)null);
                                slot2.onPickupFromSlot(eplayer, itemstack5);
                            }
                        }
                        else
                        {
                            slot2.decrStackSize(itemstack5.stackSize);
                            slot2.putStack(itemstack3);
                            slot2.onPickupFromSlot(eplayer, itemstack5);
                        }
                    }
                    else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3))
                    {
                        inventoryplayer.setInventorySlotContents(p_75144_2_, (ItemStack)null);
                        slot2.putStack(itemstack3);
                    }
                }
            }
            else if (p_75144_3_ == 3 && eplayer.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && p_75144_1_ >= 0)
            {
            	if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                else
                	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot2 != null && slot2.getHasStack())
                {
                    itemstack3 = slot2.getStack().copy();
                    itemstack3.stackSize = itemstack3.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack3);
                }
            }
            else if (p_75144_3_ == 4 && inventoryplayer.getItemStack() == null && p_75144_1_ >= 0)
            {
            	if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                else
                	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);

                if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(eplayer))
                {
                    itemstack3 = slot2.decrStackSize(p_75144_2_ == 0 ? 1 : slot2.getStack().stackSize);
                    slot2.onPickupFromSlot(eplayer, itemstack3);
                    eplayer.dropPlayerItemWithRandomChoice(itemstack3, true);
                }
            }
            else if (p_75144_3_ == 6 && p_75144_1_ >= 0)
            {
            	if(this.inventorySlots.get(p_75144_1_) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(p_75144_1_);
                else
                	slot2 = (Slot)this.inventorySlots.get(p_75144_1_);
                itemstack3 = inventoryplayer.getItemStack();

                if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(eplayer)))
                {
                    i1 = p_75144_2_ == 0 ? 0 : this.inventorySlots.size() - 1;
                    l1 = p_75144_2_ == 0 ? 1 : -1;

                    for (int i2 = 0; i2 < 2; ++i2)
                    {
                        for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1)
                        {
                            Slot slot3 = null;
                            if(this.inventorySlots.get(j2) instanceof FixedSlot)
                            	slot3 = (FixedSlot)this.inventorySlots.get(j2);
                            else
                            	slot3 = (Slot)this.inventorySlots.get(j2);

                            if (slot3.getHasStack() && func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(eplayer) && this.func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize()))
                            {
                                int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
                                ItemStack itemstack2 = slot3.decrStackSize(k1);
                                itemstack3.stackSize += k1;

                                if (itemstack2.stackSize <= 0)
                                {
                                    slot3.putStack((ItemStack)null);
                                }

                                slot3.onPickupFromSlot(eplayer, itemstack2);
                            }
                        }
                    }
                }

                this.detectAndSendChanges();
            }
        }

		if(!eplayer.worldObj.isRemote)
		{
			if(LootBags.areItemStacksEqualItem(eplayer.inventory.mainInventory[islot], wrapper.getStack(), true, false))
			{
				if(LootbagItem.checkInventory(wrapper.getStack()))
				{
					eplayer.inventory.mainInventory[islot] = null;
				}
				else
				{
					eplayer.inventory.mainInventory[islot] = wrapper.getStack();
				}
			}
		}
        return itemstack;
    }
    
    @Override
    protected void func_94533_d()
    {
        this.field_94536_g = 0;
        this.field_94537_h.clear();
    }
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/