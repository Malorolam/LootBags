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
		this.addSlotToContainer(new LootbagSlot(te, 0, 116, 15));

		this.addSlotToContainer(new RecyclerSlot(te, 1, 44, 15));
		
		//main inventory, so 18-44
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 50 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 105));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return bench.isUsableByPlayer(p_75145_1_);
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

            if(slot==0)//inventory
            {
            	if (!this.mergeItemStack(var5, 28, 38, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);

            }
            else if(slot>0 && slot<=1)
            {
            	if (!this.mergeItemStack(var5, 2, 38, true))
                {
                    return ItemStack.EMPTY;
                }

                var4.onSlotChange(var5, var3);
            }
            else
            {
            	if (!this.mergeItemStack(var5, 1, 2, true))
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
 /*   @Override
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

                if (!itemstack1.isEmpty() && itemstack1.getItem() == p_75135_1_.getItem() && (!p_75135_1_.getHasSubtypes() || p_75135_1_.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(p_75135_1_, itemstack1))
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
                        itemstack1.setCount(p_75135_1_.getMaxStackSize());
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

        if (p_75135_1_.getCount() > 0)
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

                if ((itemstack1 == null || itemstack1.isEmpty()) && slot.isItemValid(p_75135_1_))
                {
                    slot.putStack(p_75135_1_.copy());
                    slot.onSlotChanged();
                    p_75135_1_.setCount(0);
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