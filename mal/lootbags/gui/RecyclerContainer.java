package mal.lootbags.gui;

import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class RecyclerContainer extends Container{

	TileEntityRecycler bench;
	
	public RecyclerContainer(InventoryPlayer player, TileEntityRecycler te)
	{
		bench = te;
		this.addSlotToContainer(new LootbagSlot(te, 0, 80, 15));
		
		for(int i = 0; i < 9; i++)
			for(int j = 0; j<3; j++)
			{
				this.addSlotToContainer(new Slot(te, i+9*j+1, 8+i*18, 46+j*18));
			}
		
		//main inventory, so 18-44
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 105 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 162));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return bench.isUseableByPlayer(p_75145_1_);
	}

	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
    	ItemStack var3 = null;
        Slot var4 = (Slot)this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot==0)//inventory
            {
            	if (!this.mergeItemStack(var5, 28, 64, true))
                {
                    return null;
                }

                var4.onSlotChange(var5, var3);

            }
            else if(slot>0 && slot<=27)
            {
            	if (!this.mergeItemStack(var5, 27, 64, true))
                {
                    return null;
                }

                var4.onSlotChange(var5, var3);
            }
            else
            {
            	if (!this.mergeItemStack(var5, 1, 28, true))
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

            var4.onPickupFromSlot(player, var5);
        }
        
        return var3;
    }
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/