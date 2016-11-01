package mal.lootbags.jei;

import java.util.ArrayList;
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
import scala.actors.threadpool.Arrays;

public class LootWrapper extends BlankRecipeWrapper{

	public LootEntry loot;
	
	public LootWrapper(LootEntry loot)
	{
		this.loot = loot;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setOutputs(ItemStack.class, this.loot.getItemStacks());
		ingredients.setInput(ItemStack.class, this.loot.getBag().getBagItem());
	}
	
	@Override
	@Nonnull
	public List getOutputs() {
		return loot.getItemStacks();
	}
	
	@Override
	@Nonnull
	public List getInputs()
	{
		return Arrays.asList(loot.getInput());
	}
	
	public int amountOfItems()
	{
		return this.loot.getItemStacks().size();
	}
	
	/**
	 * 
	 * @param slot starting slot
	 * @param slots total number of visible slots, to double up the displayed items
	 * @return
	 */
	public List<ItemStack> getItems(int slot, int slots)
	{
		List<ItemStack> list = this.loot.getItemStacks().subList(slot, slot+1);
		for(int i = 0; i< (amountOfItems()/slots)+1; i++)
			list.add(this.amountOfItems() <= slot+slots*i ? null : this.loot.getItemStacks().get(slot+slots*i));
		list.removeIf(Objects::isNull);
		return list;
	}
	
	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		//FontHandler.normal.print(LootbagsUtil.translateToLocal(this.loot.getName()), 60, 7);
	}
}
