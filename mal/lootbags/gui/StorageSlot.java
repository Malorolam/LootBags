package mal.lootbags.gui;

import mal.lootbags.LootBags;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.tileentity.TileEntityStorage;
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
		if(!(stack.getItem() instanceof LootbagItem))
			return false;
		if(LootBags.PREVENTMERGEDBAGS)
		{
			if (!BagHandler.isBagOpened(stack) && BagHandler.isBagInsertable(stack.getMetadata()))
			{
				if(((TileEntityStorage)this.inventory).getStorage()+BagHandler.getBagValue(stack.getMetadata())[0] >= Integer.MAX_VALUE || ((TileEntityStorage)this.inventory).getStorage()+BagHandler.getBagValue(stack.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
		else
		{
			if (BagHandler.isBagInsertable(stack.getMetadata()))
			{
				if(((TileEntityStorage)this.inventory).getStorage()+BagHandler.getBagValue(stack.getMetadata())[0] >= Integer.MAX_VALUE || ((TileEntityStorage)this.inventory).getStorage()+BagHandler.getBagValue(stack.getMetadata())[0] < 0)
					return false;
				return true;
			}
		}
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