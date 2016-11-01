package mal.lootbags.jei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.loot.LootItem;
import net.minecraft.item.ItemStack;
import mezz.jei.api.recipe.IFocus;

public class LootEntry {

	private Set<LootItem> drops;
	private Bag bag;
	private String bagName;
	
	public LootEntry(Bag bag)
	{
		this.drops = new HashSet<LootItem>();
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
	
	public List<ItemStack> getItemStacks()
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(LootItem item: drops)
			list.add(item.getContentItem());
		return list;
	}
	
	public LootItem getBagDrop(ItemStack item)
	{
		return drops.stream().filter(drop -> ItemStack.areItemsEqual(drop.getContentItem(), item)).findFirst().orElse(null);
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
