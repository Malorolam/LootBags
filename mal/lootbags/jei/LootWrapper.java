package mal.lootbags.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;

import mal.core.util.FontHandler;
import mal.lootbags.Bag;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.item.LootbagItem;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class LootWrapper extends BlankRecipeWrapper implements ITooltipCallback<ItemStack>{

	public final LootEntry loot;
	
	public LootWrapper(LootEntry loot)
	{
		this.loot = loot;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setOutputs(ItemStack.class, this.loot.getItemStacks(null));
		ingredients.setInput(ItemStack.class, this.loot.getBag().getBagItem());
	}
	
	public int amountOfItems(IFocus<ItemStack> focus)
	{
		return this.loot.getItemStacks(focus).size();
	}
	
	/**
	 * 
	 * @param slot starting slot
	 * @param slots total number of visible slots, to double up the displayed items
	 * @param pages number of pages the items need to show up
	 * @return
	 */
	public List<ItemStack> getItems(IFocus<ItemStack> focus, int slot, int slots)
	{
		List<ItemStack> list = this.loot.getItemStacks(focus).subList(slot, slot+1);
		for(int i = 0; i< ((int)Math.ceil((double)amountOfItems(focus)/slots)); i++)
			list.add(this.amountOfItems(focus) <= slot+slots*i ? null : this.loot.getItemStacks(focus).get(slot+slots*i));
		for(int i = 0; i < list.size()-1; i++)
		{
			if(list.get(i) == null && list.get(i+1)==null)
			{
				list.remove(i);
				list.remove(i+1);
			}
		}
		return list;
	}

	@Override
	public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
		tooltip.add(this.loot.getBagDrop(ingredient).toString());
	}
}
