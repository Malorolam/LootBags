package mal.lootbags.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mal.lootbags.Bag;
import mal.lootbags.loot.LootItem;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;

public class LootEntry {

	private List<LootItem> drops;
	private Bag bag;
	private String bagName;
	
	public LootEntry(Bag bag)
	{
		this.drops = new ArrayList<LootItem>();
		this.bag = bag;
		this.bagName = bag.getBagName();
		
		for(LootItem item: bag.getMap().values())
		{
			drops.add(item);
		}
	}
	
	public String getName()
	{
		return bagName;
	}
	
	public boolean containsItem(ItemStack item)
	{
		return drops.stream().anyMatch(drop -> drop.getContentItem().isItemEqual(item));
	}
	
	public List<ItemStack> getItemStacks(IFocus<ItemStack> focus)
	{
		return drops.stream().map(drop -> drop.getContentItem())
				/*.filter(stack -> focus==null || ItemStack.areItemStacksEqual(ItemHandlerHelper.copyStackWithSize(stack, focus.getValue().getCount()), focus.getValue()))*/
				.collect(Collectors.toList());
	}
	
	public LootItem getBagDrop(ItemStack item)
	{
		return drops.stream().filter(drop -> ItemStack.areItemsEqual(drop.getContentItem(), item)).findFirst().orElse(null);
	}

	public double getItemChance(ItemStack item)
	{
		LootItem loot = getBagDrop(item);
		Double dd = (loot.getItemWeight()*100.0)/ bag.getBagMapWeight();
		return dd;
	}
	
	public Bag getBag()
	{
		return bag;
	}
	
	public Object[] getInput()
	{
		return new Object[]{bag.getBagItem()};
	}
}
