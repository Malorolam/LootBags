package mal.lootbags.rendering;

import mal.lootbags.LootBags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ItemRenderingRegister {

	private static ItemRenderingRegister render;
	
	private ItemRenderingRegister(){}
	
	public static ItemRenderingRegister instance()
	{
		if(ItemRenderingRegister.render==null)
			ItemRenderingRegister.render = new ItemRenderingRegister();
		
		return render;
	}
	
	public static void registerItemRender()
	{
		ItemRenderingRegister inst = instance();
		
		inst.reg(LootBags.lootbagItem);
	}
	
	public void reg(Item item)
	{
		if(item instanceof IItemVarientDetails)
			((IItemVarientDetails)item).registerItemVariants(this);
		else
			reg(item,0);
	}
	
	public void reg(Item item, int metadata)
	{
		reg(item, metadata, item.getRegistryName());
	}
	
	public void reg(Item item, int metadata, String file)
	{
		ModelResourceLocation loc = new ModelResourceLocation(LootBags.MODID+":"+file, "inventory");
		
		ModelLoader.setCustomModelResourceLocation(item, metadata, loc);
	}
	
	public void reg(Item item, int metadata, ResourceLocation file)
	{
		ModelResourceLocation loc = new ModelResourceLocation(file, "inventory");
		
		ModelLoader.setCustomModelResourceLocation(item, metadata, loc);
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/