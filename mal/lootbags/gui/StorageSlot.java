package mal.lootbags.gui;

import mal.lootbags.LootBags;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.item.LootbagItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class StorageSlot extends Slot{

	public StorageSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if(stack.isEmpty() || !(stack.getItem() instanceof LootbagItem) || !BagHandler.isBagInsertable(stack.getMetadata()))
			return false;
		if(LootBags.PREVENTMERGEDBAGS && BagHandler.isBagOpened(stack))
			return false;
		return true;
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/