package mal.lootbags.network;

import mal.lootbags.LootBags;
import mal.lootbags.gui.LootbagContainer;
import mal.lootbags.gui.RecyclerContainer;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID==0 && player.getCurrentEquippedItem() != null && Item.getIdFromItem(player.getCurrentEquippedItem().getItem()) == Item.getIdFromItem(LootBags.lootbag))
		{
			return new LootbagContainer(player.inventory, new LootbagWrapper(player.getCurrentEquippedItem(), player.inventory.currentItem));
		}
		if(ID==1)
			return new RecyclerContainer(player.inventory, (TileEntityRecycler) world.getTileEntity(x, y, z));
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

    public World getClientWorld()
    {
        return null;
    }
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/