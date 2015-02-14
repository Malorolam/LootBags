package mal.lootbags.gui;

import mal.lootbags.LootBags;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class RecyclerSlot extends Slot {

	public RecyclerSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_,
			int p_i1824_4_) {
		super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isItemValid(ItemStack stack)
    {
		if(stack == null)
			return false;
		for(ItemStack item: LootBags.getLootbagDropList())
		{
			if(LootBags.areItemStacksEqualItem(item, stack, false, false))
				return true;
		}
		return false;
    }
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/