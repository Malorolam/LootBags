package mal.lootbags.network;

import mal.lootbags.LootBags;
import mal.lootbags.gui.LootbagGui;
import mal.lootbags.gui.RecyclerGui;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;

public class ClientProxy extends CommonProxy{

	@Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID==0 && player.getCurrentEquippedItem() != null && Item.getIdFromItem(player.getCurrentEquippedItem().getItem()) == Item.getIdFromItem(LootBags.lootbag))
		{
			return new LootbagGui(player.inventory, new LootbagWrapper(player.getCurrentEquippedItem(), player.inventory.currentItem));
		}
		if(ID==1)
			return new RecyclerGui(player.inventory, (TileEntityRecycler) world.getTileEntity(x, y, z));
		return null;
	}
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/