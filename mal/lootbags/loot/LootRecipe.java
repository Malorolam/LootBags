package mal.lootbags.loot;

import java.util.ArrayList;

import mal.lootbags.LootBags;
import mal.lootbags.item.LootbagItem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Basically an extension of shapeless recipes to allow for generated bag blocking
 */
public class LootRecipe extends ShapelessOreRecipe{

	public LootRecipe(ItemStack result, Object[] recipe) {
		super(result, recipe);
	}
	
    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean matches(InventoryCrafting var1, World world)
    {
		boolean opened = super.matches(var1, world);
		if(!opened)
			return false;
		
    	if(LootBags.PREVENTMERGEDBAGS)
    	{
    		for(int i = 0; i < var1.getSizeInventory(); i++)
    		{
    			if(var1.getStackInSlot(i) != null && var1.getStackInSlot(i).getItem() instanceof LootbagItem && var1.getStackInSlot(i).hasTagCompound() && var1.getStackInSlot(i).getTagCompound().getBoolean("generated"))
    			{
    				opened = false;
    			}
    		}
    	}
    	return opened;
    }
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/