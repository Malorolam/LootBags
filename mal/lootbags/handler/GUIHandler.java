package mal.lootbags.handler;

import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.gui.LootbagContainer;
import mal.lootbags.gui.LootbagGui;
import mal.lootbags.gui.OpenerContainer;
import mal.lootbags.gui.OpenerGui;
import mal.lootbags.gui.RecyclerContainer;
import mal.lootbags.gui.RecyclerGui;
import mal.lootbags.gui.StorageContainer;
import mal.lootbags.gui.StorageGui;
import mal.lootbags.network.LootbagWrapper;
import mal.lootbags.tileentity.TileEntityOpener;
import mal.lootbags.tileentity.TileEntityRecycler;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		ArrayList<ItemStack> heldItems = (ArrayList<ItemStack>) player.getHeldEquipment();
		if(ID==0) {
			if(heldItems.isEmpty())
				return null;
			if(!heldItems.get(0).isEmpty() && Item.getIdFromItem(heldItems.get(0).getItem()) == Item.getIdFromItem(LootBags.lootbagItem))
				return new LootbagContainer(player.inventory, new LootbagWrapper(heldItems.get(0)));
			else if(!heldItems.get(1).isEmpty() && Item.getIdFromItem(heldItems.get(1).getItem()) == Item.getIdFromItem(LootBags.lootbagItem))
				return new LootbagContainer(player.inventory, new LootbagWrapper(heldItems.get(1)));
		}
		if(ID==1)
			return new RecyclerContainer(player.inventory, (TileEntityRecycler) world.getTileEntity(new BlockPos(x,y,z)));
		if(ID==2)
			return new OpenerContainer(player.inventory, (TileEntityOpener)world.getTileEntity(new BlockPos(x,y,z)));
		if(ID==3)
			return new StorageContainer(player.inventory, (TileEntityStorage)world.getTileEntity(new BlockPos(x,y,z)));
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		ArrayList<ItemStack> heldItems = (ArrayList<ItemStack>) player.getHeldEquipment();
		if(ID==0) {
			if(heldItems.isEmpty())
				return null;
			if(!heldItems.get(0).isEmpty() && Item.getIdFromItem(heldItems.get(0).getItem()) == Item.getIdFromItem(LootBags.lootbagItem))
				return new LootbagGui(player.inventory, new LootbagWrapper(heldItems.get(0)));
			if(!heldItems.get(1).isEmpty() && Item.getIdFromItem(heldItems.get(1).getItem()) == Item.getIdFromItem(LootBags.lootbagItem))
				return new LootbagGui(player.inventory, new LootbagWrapper(heldItems.get(1)));
		}
		if(ID==1)
			return new RecyclerGui(player.inventory, (TileEntityRecycler) world.getTileEntity(new BlockPos(x,y,z)));
		if(ID==2)
			return new OpenerGui(player.inventory, (TileEntityOpener)world.getTileEntity(new BlockPos(x,y,z)));
		if(ID==3)
			return new StorageGui(player.inventory, (TileEntityStorage)world.getTileEntity(new BlockPos(x,y,z)));
		return null;
	}

}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/