package mal.lootbags.gui;

import mal.lootbags.tileentity.TileEntityOpener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class OpenerContainer extends Container{

	TileEntityOpener bench;
	
	public OpenerContainer(InventoryPlayer player, TileEntityOpener te)
	{
		bench = te;
		//input
		for(int i = 0; i < 9; i++)
			for(int j = 0; j<1; j++)
			{
				this.addSlotToContainer(new OpenerLootbagSlot(te, i+9*j, 8+i*18, 19+j*18));
			}
		
		//output
		for(int i = 0; i < 9; i++)
			for(int j = 0; j<3; j++)
			{
				this.addSlotToContainer(new LootbagSlot(te, 9+i+9*j, 8+i*18, 43+j*18));
			}
		
		//main inventory, so 18-44
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 102 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 159));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return bench.isUsableByPlayer(playerIn);
	}

	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
    	ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot>=0 && slot<=8)//input
            {
            	if (!this.mergeItemStack(var5, 36, 72, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);

            }
            else if(slot>8 && slot<=35)
            {
            	if (!this.mergeItemStack(var5, 36, 72, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);
            }
            else
            {
            	if (!this.mergeItemStack(var5, 0, 9, true))
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
    
    /**
     * merges provided ItemStack with the first avaliable one in the container/player inventory
     */
/*    @Override
    protected boolean mergeItemStack(ItemStack p_75135_1_, int p_75135_2_, int p_75135_3_, boolean p_75135_4_)
    {
        boolean flag1 = false;
        int k = p_75135_2_;

        if (p_75135_4_)
        {
            k = p_75135_3_ - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (p_75135_1_.isStackable())
        {
            while (p_75135_1_.getCount() > 0 && (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_))
            {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 != null && itemstack1.getItem() == p_75135_1_.getItem() && (!p_75135_1_.getHasSubtypes() || p_75135_1_.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(p_75135_1_, itemstack1))
                {
                    int l = itemstack1.getCount() + p_75135_1_.getCount();

                    if (l <= p_75135_1_.getMaxStackSize())
                    {
                        p_75135_1_.setCount(0);
                        itemstack1.setCount(l);
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                    else if (itemstack1.getCount() < p_75135_1_.getMaxStackSize())
                    {
                        p_75135_1_.setCount(p_75135_1_.getCount() - (p_75135_1_.getMaxStackSize() - itemstack1.getCount()));
                        itemstack1.stackSize = p_75135_1_.getMaxStackSize();
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                }

                if (p_75135_4_)
                {
                    --k;
                }
                else
                {
                    ++k;
                }
            }
        }

        if (p_75135_1_.stackSize > 0)
        {
            if (p_75135_4_)
            {
                k = p_75135_3_ - 1;
            }
            else
            {
                k = p_75135_2_;
            }

            while (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_)
            {
                slot = (Slot)this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                if (itemstack1 == null && slot.isItemValid(p_75135_1_))
                {
                    slot.putStack(p_75135_1_.copy());
                    slot.onSlotChanged();
                    p_75135_1_.stackSize = 0;
                    flag1 = true;
                    break;
                }

                if (p_75135_4_)
                {
                    --k;
                }
                else
                {
                    ++k;
                }
            }
        }

        return flag1;
    }*/
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/