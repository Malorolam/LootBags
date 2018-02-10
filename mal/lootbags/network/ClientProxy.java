package mal.lootbags.network;

import mal.lootbags.LootBags;
import mal.lootbags.item.LootbagColor;
import mal.lootbags.rendering.ItemRenderingRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ClientProxy extends CommonProxy{

	@Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().world;
    }
	
	@Override
	public void registerRenderersItem()
	{
		ItemRenderingRegister.registerItemRender();
	}
	
	@Override
	public void registerRenderersInit()
	{
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(LootBags.recyclerBlock), 0, new ModelResourceLocation(LootBags.MODID + ":" + LootBags.recyclerBlock.getName(), "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(LootBags.openerBlock), 0, new ModelResourceLocation(LootBags.MODID + ":" + LootBags.openerBlock.getName(), "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(LootBags.storageBlock), 0, new ModelResourceLocation(LootBags.MODID + ":" + LootBags.storageBlock.getName(), "inventory"));
		
		ItemColors color = Minecraft.getMinecraft().getItemColors();
		color.registerItemColorHandler(new LootbagColor(), LootBags.lootbagItem);
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/