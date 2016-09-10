package mal.lootbags.network;

import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.gui.LootbagContainer;
import mal.lootbags.gui.LootbagGui;
import mal.lootbags.gui.RecyclerContainer;
import mal.lootbags.gui.RecyclerGui;
import mal.lootbags.rendering.ItemRenderingRegister;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ClientProxy extends CommonProxy{

	@Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }
	
	@Override
	public void registerRenderers()
	{
		ItemRenderingRegister.registerItemRender();
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/