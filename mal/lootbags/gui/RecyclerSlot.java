package mal.lootbags.gui;

import mal.lootbags.LootBags;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class RecyclerSlot extends Slot {

	public RecyclerSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_,
			int p_i1824_4_) {
		super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
    {
		if(stack == null || stack.isEmpty())
			return false;
		return (LootBags.isItemDroppable(stack) && (!LootBags.isItemRecyleBlacklisted(stack)) || LootBags.isItemRecycleWhitelisted(stack));
    }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/