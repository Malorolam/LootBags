package mal.lootbags.gui;

import mal.lootbags.item.LootbagItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class OpenerLootbagSlot extends Slot{

	public OpenerLootbagSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if(stack==null || stack.isEmpty())
			return false;
		if(stack.getItem() instanceof LootbagItem)
			return true;
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