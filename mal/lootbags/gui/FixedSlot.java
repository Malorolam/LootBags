package mal.lootbags.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FixedSlot extends Slot{

	public FixedSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_,
			int p_i1824_4_) {
		super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
	}
	
    /**
     * Return whether this slot's stack can be taken from this slot.
     */
	@Override
    public boolean canTakeStack(EntityPlayer p_82869_1_)
    {
        return false;
    }
    
	@Override
	public boolean isItemValid(ItemStack p_75214_1_)
    {
        return false;
    }
	
	@Override
	public void putStack(ItemStack is)
	{	}
	
/*	@Override
    public ItemStack decrStackSize(int p_75209_1_)
    {
		return null;
    }*/
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/