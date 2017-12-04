package mal.lootbags.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;

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
	
	public List<ItemStack> getItems(IFocus<ItemStack> focus, int slot, int slots)
	{
		List<ItemStack> list = this.loot.getItemStacks(focus).subList(slot, slot+1);
		list.add(list.get(0));
		for(int i = 1; i< amountOfItems(focus)/slots+1; i++)
		{
			list.add(this.amountOfItems(focus) <= slot+slots*i ? null : this.loot.getItemStacks(focus).get(slot+slots*i));
			list.add(this.amountOfItems(focus) <= slot+slots*i ? null : this.loot.getItemStacks(focus).get(slot+slots*i));
		}
/*		for(int i = 0; i < list.size()-1; i++)
		{
			if(list.get(i) == null && list.get(i+1)==null)
			{
				list.remove(i);
				list.remove(i+1);
			}
		}*/
		if(amountOfItems(focus) < LootCategory.ITEMSPERPAGE)
			list.removeIf(Objects::isNull);
		return list;
	}

	@Override
	public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
		if(slotIndex != 0)
			tooltip.add(this.loot.getBagDrop(ingredient).toString());
	}
	
	public List<ItemStack> getBag()
	{
		List<ItemStack> list = new ArrayList();
		list.add(this.loot.getBag().getBagItem());
		return list;
	}
}
