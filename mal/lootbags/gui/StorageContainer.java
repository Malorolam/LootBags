package mal.lootbags.gui;

import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class StorageContainer extends Container{

	TileEntityStorage bench;
	private boolean flag = false;
	
	public StorageContainer(InventoryPlayer player, TileEntityStorage te)
	{
		bench = te;
		
		//input
		this.addSlotToContainer(new StorageSlot(te, 1, 26, 16));
		
		//output
		this.addSlotToContainer(new LootbagSlot(te, 0, 135, 16));
		
		//main inventory, so 18-44
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 66 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 123));
        }
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return bench.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
		flag = false;
		return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
    	if(flag)
    		return ItemStack.EMPTY;
    	flag = true;
    	
    	ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot==0)//input
            {
            	if (!this.mergeItemStack(var5, 2, 38, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);

            }
            else if(slot==1)
            {
            	if (!this.mergeItemStack(var5, 2, 38, true))
                {
                    return ItemStack.EMPTY;
                }

            	bench.decrStorage(var3);
                var4.onSlotChange(var5, var3);
            }
            else
            {
            	if (!this.mergeItemStack(var5, 0, 1, true))
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

            var4.onTake(player, var5);
        }
        
        return var3;
    }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/