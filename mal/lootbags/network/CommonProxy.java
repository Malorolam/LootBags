package mal.lootbags.network;

import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.gui.LootbagContainer;
import mal.lootbags.gui.RecyclerContainer;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy {

    public World getClientWorld()
    {
        return null;
    }

	public void registerRenderers() {}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/