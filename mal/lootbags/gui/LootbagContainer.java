package mal.lootbags.gui;

import mal.lootbags.LootBags;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class LootbagContainer extends Container{

	public LootbagWrapper wrapper;
	public InventoryPlayer player;
	private int islot;
	
	//Meesa bypassa alla de clicky privat fieldsa
/*    private int dragModea = -1;
    private int dragEventa;
    private final Set<Slot> dragSlotsa = Sets.<Slot>newHashSet();*/
	
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
		if(player.getItemStack()!=null && !player.getItemStack().isEmpty())
			return true;//LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false);
		return wrapper.isUsableByPlayer(p_75145_1_); //&& LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false);
	}

	@Override
	public void detectAndSendChanges()
    {	
		super.detectAndSendChanges();
		if(LootBags.areItemStacksEqualItem(player.mainInventory.get(islot), wrapper.getStack(), true, false))
		{
			if(LootbagItem.checkInventory(wrapper.getStack()))
			{
				player.mainInventory.set(islot, ItemStack.EMPTY);
			}
			else
			{
				player.mainInventory.set(islot, wrapper.getStack());
			}
		}
    }
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if(!player.world.isRemote)
		{
			if(LootBags.areItemStacksEqualItem(player.inventory.mainInventory.get(islot), wrapper.getStack(), true, false))
			{
				if(LootbagItem.checkInventory(wrapper.getStack()))
				{
					player.inventory.mainInventory.set(islot, ItemStack.EMPTY);
				}
				else
				{
					player.inventory.mainInventory.set(islot, wrapper.getStack());
				}
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
    	ItemStack var3 = ItemStack.EMPTY;
    	Slot var4 = null;
    	if(this.inventorySlots.get(slot) instanceof FixedSlot)
    		var4 = this.inventorySlots.get(slot);
    	else
    		var4 = this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot>=0 && slot <5)//inventory
            {
            	if (!this.mergeItemStack(var5, 5, 41, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);

            }
            
            if (var5.getCount() == 0)
            {
                var4.putStack(ItemStack.EMPTY);
            }
            else
            {
                var4.onSlotChanged();
            }

            if (var5.getCount() == var3.getCount())
            {
                return ItemStack.EMPTY;
            }

            var4.onTake(par1EntityPlayer, var5);
        }
        
        return var3;
    }
    
/*    @Override
    @Nullable
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
    	ItemStack itemstack = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT)
        {
            int i = this.dragEventa;
            this.dragEventa = getDragEvent(dragType);

            if ((i != 1 || this.dragEventa != 2) && i != this.dragEventa)
            {
                this.resetDrag();
            }
            else if (inventoryplayer.getItemStack().isEmpty())
            {
                this.resetDrag();
            }
            else if (this.dragEventa == 0)
            {
                this.dragModea = extractDragMode(dragType);

                if (isValidDragMode(this.dragModea, player))
                {
                    this.dragEventa = 1;
                    this.dragSlotsa.clear();
                }
                else
                {
                    this.resetDrag();
                }
            }
            else if (this.dragEventa == 1)
            {
                Slot slot = (Slot)this.inventorySlots.get(slotId);
                ItemStack itemstack1 = inventoryplayer.getItemStack();

                if (slot != null && canAddItemToSlot(slot, itemstack1, true) && slot.isItemValid(itemstack1) && (this.dragModea == 2 || itemstack1.getCount() > this.dragSlotsa.size()) && this.canDragIntoSlot(slot))
                {
                    this.dragSlotsa.add(slot);
                }
            }
            else if (this.dragEventa == 2)
            {
                if (!this.dragSlotsa.isEmpty())
                {
                    ItemStack itemstack5 = inventoryplayer.getItemStack().copy();
                    int l = inventoryplayer.getItemStack().getCount();

                    for (Slot slot1 : this.dragSlotsa)
                    {
                        ItemStack itemstack2 = inventoryplayer.getItemStack();

                        if (slot1 != null && canAddItemToSlot(slot1, itemstack2, true) && slot1.isItemValid(itemstack2) && (this.dragModea == 2 || itemstack2.getCount() >= this.dragSlotsa.size()) && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack3 = itemstack5.copy();
                            int j = slot1.getHasStack() ? slot1.getStack().getCount() : 0;
                            computeStackSize(this.dragSlotsa, this.dragModea, itemstack3, j);
                            int k = Math.min(itemstack3.getMaxStackSize(), slot1.getItemStackLimit(itemstack3));

                            if (itemstack3.getCount() > k)
                            {
                                itemstack3.setCount(k);
                            }

                            l -= itemstack3.getCount() - j;
                            slot1.putStack(itemstack3);
                        }
                    }

                    itemstack5.setCount(l);
                    inventoryplayer.setItemStack(itemstack5);
                }

                this.resetDrag();
            }
            else
            {
                this.resetDrag();
            }
        }
        else if (this.dragEventa != 0)
        {
            this.resetDrag();
        }
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
        {
            if (slotId == -999)
            {
                if (!inventoryplayer.getItemStack().isEmpty())
                {
                    if (dragType == 0)
                    {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (dragType == 1)
                    {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
            }
            else if (clickTypeIn == ClickType.QUICK_MOVE)
            {
                if (slotId < 0)
                {
                    return ItemStack.EMPTY;
                }

                Slot slot6 = (Slot)this.inventorySlots.get(slotId);

                if (slot6 != null && slot6.canTakeStack(player))
                {
                    ItemStack itemstack10 = this.transferStackInSlot(player, slotId);

                    if (!itemstack10.isEmpty())
                    {
                        Item item = itemstack10.getItem();
                        itemstack = itemstack10.copy();

                        if (slot6.getStack().getItem() == item)
                        {
                            this.retrySlotClick(slotId, dragType, true, player);
                        }
                    }
                }
            }
            else
            {
                if (slotId < 0)
                {
                    return ItemStack.EMPTY;
                }

                Slot slot7 = (Slot)this.inventorySlots.get(slotId);

                if (slot7 != null)
                {
                    ItemStack itemstack11 = slot7.getStack();
                    ItemStack itemstack13 = inventoryplayer.getItemStack();

                    if (!itemstack11.isEmpty())
                    {
                        itemstack = itemstack11.copy();
                    }

                    if (itemstack11.isEmpty())
                    {
                        if (!itemstack13.isEmpty() && slot7.isItemValid(itemstack13))
                        {
                            int l2 = dragType == 0 ? itemstack13.getCount() : 1;

                            if (l2 > slot7.getItemStackLimit(itemstack13))
                            {
                                l2 = slot7.getItemStackLimit(itemstack13);
                            }

                            slot7.putStack(itemstack13.splitStack(l2));
                        }
                    }
                    else if (slot7.canTakeStack(player))
                    {
                        if (itemstack13.isEmpty())
                        {
                            if (itemstack11.isEmpty())
                            {
                                slot7.putStack(ItemStack.EMPTY);
                                inventoryplayer.setItemStack(ItemStack.EMPTY);
                            }
                            else
                            {
                                int k2 = dragType == 0 ? itemstack11.getCount() : (itemstack11.getCount() + 1) / 2;
                                inventoryplayer.setItemStack(slot7.decrStackSize(k2));

                                if (itemstack11.isEmpty())
                                {
                                    slot7.putStack(ItemStack.EMPTY);
                                }

                                slot7.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                        else if (slot7.isItemValid(itemstack13))
                        {
                            if (itemstack11.getItem() == itemstack13.getItem() && itemstack11.getMetadata() == itemstack13.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack11, itemstack13))
                            {
                                int j2 = dragType == 0 ? itemstack13.getCount() : 1;

                                if (j2 > slot7.getItemStackLimit(itemstack13) - itemstack11.getCount())
                                {
                                    j2 = slot7.getItemStackLimit(itemstack13) - itemstack11.getCount();
                                }

                                if (j2 > itemstack13.getMaxStackSize() - itemstack11.getCount())
                                {
                                    j2 = itemstack13.getMaxStackSize() - itemstack11.getCount();
                                }

                                itemstack13.shrink(j2);
                                itemstack11.grow(j2);
                            }
                            else if (itemstack13.getCount() <= slot7.getItemStackLimit(itemstack13))
                            {
                                slot7.putStack(itemstack13);
                                inventoryplayer.setItemStack(itemstack11);
                            }
                        }
                        else if (itemstack11.getItem() == itemstack13.getItem() && itemstack13.getMaxStackSize() > 1 && (!itemstack11.getHasSubtypes() || itemstack11.getMetadata() == itemstack13.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack11, itemstack13) && !itemstack11.isEmpty())
                        {
                            int i2 = itemstack11.getCount();

                            if (i2 + itemstack13.getCount() <= itemstack13.getMaxStackSize())
                            {
                                itemstack13.grow(i2);
                                itemstack11 = slot7.decrStackSize(i2);

                                if (itemstack11.isEmpty())
                                {
                                    slot7.putStack(ItemStack.EMPTY);
                                }

                                slot7.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    slot7.onSlotChanged();
                }
            }
        }
        else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9)
        {
            Slot slot5 = (Slot)this.inventorySlots.get(slotId);
            ItemStack itemstack9 = inventoryplayer.getStackInSlot(dragType);
            ItemStack itemstack12 = slot5.getStack();

            if (!itemstack9.isEmpty() || !itemstack12.isEmpty())
            {
                if (itemstack9.isEmpty())
                {
                    if (slot5.canTakeStack(player))
                    {
                        inventoryplayer.setInventorySlotContents(dragType, itemstack12);
                        slot5.onSwapCraft(itemstack12.getCount());
                        slot5.putStack(ItemStack.EMPTY);
                        slot5.onTake(player, itemstack12);
                    }
                }
                else if (itemstack12.isEmpty())
                {
                    if (slot5.isItemValid(itemstack9))
                    {
                        int k1 = slot5.getItemStackLimit(itemstack9);

                        if (itemstack9.getCount() > k1)
                        {
                            slot5.putStack(itemstack9.splitStack(k1));
                        }
                        else
                        {
                            slot5.putStack(itemstack9);
                            inventoryplayer.setInventorySlotContents(dragType, ItemStack.EMPTY);
                        }
                    }
                }
                else if (slot5.canTakeStack(player) && slot5.isItemValid(itemstack9))
                {
                    int l1 = slot5.getItemStackLimit(itemstack9);

                    if (itemstack9.getCount() > l1)
                    {
                        slot5.putStack(itemstack9.splitStack(l1));
                        slot5.onTake(player, itemstack12);

                        if (!inventoryplayer.addItemStackToInventory(itemstack12))
                        {
                            player.dropItem(itemstack12, true);
                        }
                    }
                    else
                    {
                        slot5.putStack(itemstack9);
                        inventoryplayer.setInventorySlotContents(dragType, itemstack12);
                        slot5.onTake(player, itemstack12);
                    }
                }
            }
        }
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && slotId >= 0)
        {
            Slot slot4 = (Slot)this.inventorySlots.get(slotId);

            if (slot4 != null && slot4.getHasStack())
            {
                ItemStack itemstack8 = slot4.getStack().copy();
                itemstack8.setCount(itemstack8.getMaxStackSize());
                inventoryplayer.setItemStack(itemstack8);
            }
        }
        else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && slotId >= 0)
        {
            Slot slot3 = (Slot)this.inventorySlots.get(slotId);

            if (slot3 != null && slot3.getHasStack() && slot3.canTakeStack(player))
            {
                ItemStack itemstack7 = slot3.decrStackSize(dragType == 0 ? 1 : slot3.getStack().getCount());
                slot3.onTake(player, itemstack7);
                player.dropItem(itemstack7, true);
            }
        }
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
        {
            Slot slot2 = (Slot)this.inventorySlots.get(slotId);
            ItemStack itemstack6 = inventoryplayer.getItemStack();

            if (!itemstack6.isEmpty() && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player)))
            {
                int i1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                int j1 = dragType == 0 ? 1 : -1;

                for (int i3 = 0; i3 < 2; ++i3)
                {
                    for (int j3 = i1; j3 >= 0 && j3 < this.inventorySlots.size() && itemstack6.getCount() < itemstack6.getMaxStackSize(); j3 += j1)
                    {
                        Slot slot8 = (Slot)this.inventorySlots.get(j3);

                        if (slot8.getHasStack() && canAddItemToSlot(slot8, itemstack6, true) && slot8.canTakeStack(player) && this.canMergeSlot(itemstack6, slot8))
                        {
                            ItemStack itemstack14 = slot8.getStack();

                            if (i3 != 0 || itemstack14.getCount() != itemstack14.getMaxStackSize())
                            {
                                int k3 = Math.min(itemstack6.getMaxStackSize() - itemstack6.getCount(), itemstack14.getCount());
                                ItemStack itemstack4 = slot8.decrStackSize(k3);
                                itemstack6.grow(k3);

                                if (itemstack4.isEmpty())
                                {
                                    slot8.putStack(ItemStack.EMPTY);
                                }

                                slot8.onTake(player, itemstack4);
                            }
                        }
                    }
                }
            }

            this.detectAndSendChanges();
        }

        return itemstack;
    }
    
    *//**
     * Reset the drag fields
     *//*
    @Override
    protected void resetDrag()
    {
        this.dragEventa = 0;
        this.dragSlotsa.clear();
    }*/
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/