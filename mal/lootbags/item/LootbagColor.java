package mal.lootbags.item;

import mal.lootbags.Bag;
import mal.lootbags.handler.BagHandler;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LootbagColor implements IItemColor {

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		Bag bag = BagHandler.getBag(stack.getItemDamage());
		if(bag==null)
			return 16777215;
		int[] colors = bag.getBagTextureColor();
		if(tintIndex==0)
			return colors[0];
		else
			return colors[1];
	}

}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/