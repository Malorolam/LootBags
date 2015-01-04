package mal.lootbags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.TreeMap;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Generate and retrieve a list of every item that can be dropped by a loot bag
 * @author Mal
 *
 */
public class LootMap {

	private TreeMap<String, WeightedRandomChestContent> map = new TreeMap<String, WeightedRandomChestContent>();
	
	private int totalWeight=0;
	private Random random = new Random();
	
	/**
	 * Add every item in a category to the map calculating the drop chance
	 * @param categoryName
	 */
	public void addLootCategory(String categoryName)
	{
		WeightedRandomChestContent[] h = ChestGenHooks.getItems(categoryName, random);
		for(int i = 0; i < h.length; i++)
		{
			WeightedRandomChestContent c = h[i];
			boolean skip = false;
			for(String modid:LootBags.MODBLACKLIST)
			{
				if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
					skip = true;
			}
			for(ItemStack istack:LootBags.LOOTBLACKLIST)
			{
				if(istack.isItemEqual(c.theItemId))
					skip = true;
			}
			if(!skip)
			{
				if(!map.containsKey(c.theItemId.toString()))
				{
					map.put(c.theItemId.toString(), c);
					totalWeight+= c.itemWeight;
				}
				else
				{
					int weight = map.get(c.theItemId.toString()).itemWeight;
					totalWeight -= weight;
					weight = (weight+c.itemWeight)/2;
					map.put(c.theItemId.toString(), c);
					totalWeight += weight;
				}
			}
			else
			{
				FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from " + categoryName + " from spawning in Loot Bags.");
			}
		}
	}
	
	public void addWhitelistedItems(String[] whitelistlist)
	{
		for(String s: whitelistlist)
		{
			String trim = s.trim();
			if(!trim.isEmpty())
			{
				String[] words = trim.split("\\s+");
				if(words.length == 3)
				{
					if(!OreDictionary.getOres(words[0]).isEmpty())
					{
						FMLLog.log(Level.INFO, "Added Whitelist item from OreDictionary: " + words[0] + "x" + words[1]);
						ItemStack is = OreDictionary.getOres(words[0]).get(0).copy();
						is.stackSize=Integer.parseInt(words[1]);
						
						WeightedRandomChestContent c = new WeightedRandomChestContent(is, 1, is.stackSize, Integer.parseInt(words[2]));
						
						boolean skip = false;
						for(String modid:LootBags.MODBLACKLIST)
						{
							if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
								skip = true;
						}
						for(ItemStack istack:LootBags.LOOTBLACKLIST)
						{
							if(istack.isItemEqual(c.theItemId))
								skip = true;
						}
						if(!skip)
						{
							map.put(c.theItemId.toString(), c);
							totalWeight+= c.itemWeight;
						}
						else
						{
							FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from the LootBags whitelist from spawning in Loot Bags.");
						}
					}
				}
				if(words.length == 5)
				{
					ItemStack stack = null;
					//one of these should be not null
					Block block = GameRegistry.findBlock(words[0], words[1]);
					Item item = GameRegistry.findItem(words[0], words[1]);
					if(item != null)
						stack = new ItemStack(item,Integer.parseInt(words[3]),Integer.parseInt(words[2]));
					else if(block != null)
						stack = new ItemStack(block,Integer.parseInt(words[3]),Integer.parseInt(words[2]));
					if(stack != null && stack.getItem() != null)
					{
						FMLLog.log(Level.INFO, "Added Whitelist item: " + stack.toString());
						WeightedRandomChestContent c = new WeightedRandomChestContent(stack, 1, stack.stackSize, Integer.parseInt(words[4]));
						
						boolean skip = false;
						for(String modid:LootBags.MODBLACKLIST)
						{
							if(GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).modId.equalsIgnoreCase(modid))
								skip = true;
						}
						for(ItemStack istack:LootBags.LOOTBLACKLIST)
						{
							if(istack.isItemEqual(c.theItemId))
								skip = true;
						}
						if(!skip)
						{
							map.put(c.theItemId.toString(), c);
							totalWeight+= c.itemWeight;
						}
						else
						{
							FMLLog.log(Level.INFO, "Blacklisted item: " + GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem()).toString() + " dropping from the LootBags whitelist from spawning in Loot Bags.");
						}
					}
				}
			}
		}
	}
	
	public ItemStack getRandomItem(int maxWeight)
	{
		WeightedRandomChestContent[] content;
		int randWeight;
		if(maxWeight == -1)
		{
			content = map.values().toArray(new WeightedRandomChestContent[map.values().size()]);
			randWeight = random.nextInt(totalWeight);
		}
		else
		{
			content = generateContent(maxWeight);
			randWeight = random.nextInt(generateMaxTotalWeight(maxWeight));
		}
		WeightedRandomChestContent item = (WeightedRandomChestContent) WeightedRandom.getItem(content, randWeight);
		int r = 0;
		while (item == null && r < LootBags.MAXREROLLCOUNT)
		{
			System.out.println("reroll null item");
			item = (WeightedRandomChestContent) WeightedRandom.getItem(content, randWeight);
			r++;
		}
		if(item == null)
			return null;
    	ItemStack[] stacks = ChestGenHooks.generateStacks(random, item.theItemId, item.theMinimumChanceToGenerateItem, item.theMaximumChanceToGenerateItem);
        return (stacks.length > 0 ? stacks[0] : null);
	}
	
	private WeightedRandomChestContent[] generateContent(int maxWeight)
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(WeightedRandomChestContent c : map.values())
		{
			if(c.itemWeight <= maxWeight)
				list.add(c);
		}
		return list.toArray(new WeightedRandomChestContent[list.size()]);
	}
	
	private int generateMaxTotalWeight(int maxWeight)
	{
		int weight = 0;
		for(WeightedRandomChestContent c : map.values())
		{
			if(c.itemWeight <= maxWeight)
				weight+= c.itemWeight;
		}
		return weight;
	}
	
	/**
	 * Generates the weight needed to guarantee X% of the items can drop
	 * @param percentile
	 * @return
	 */
	public int generatePercentileWeight(int percentile)
	{
		double val = percentile/100.0*map.size();
		WeightedRandomChestContent[] content = map.values().toArray(new WeightedRandomChestContent[map.values().size()]);
		ArrayList<Integer> weights = new ArrayList<Integer>();
		for(WeightedRandomChestContent c: content)
		{
			weights.add(c.itemWeight);
		}
		Collections.sort(weights);
		return weights.get((int) Math.round(val));
	}
	public ArrayList<ItemStack> getMapAsList()
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(WeightedRandomChestContent c : map.values())
		{
			list.add(c.theItemId);
		}
		return list;
	}
	
	public ArrayList<WeightedRandomChestContent> getMapAsChestList()
	{
		ArrayList<WeightedRandomChestContent> list = new ArrayList<WeightedRandomChestContent>();
		for(WeightedRandomChestContent c : map.values())
		{
			list.add(c);
		}
		return list;
	}
	
	public int getTotalWeight()
	{
		return totalWeight;
	}
	
	public boolean isItemInMap(ItemStack is)
	{
		ArrayList<ItemStack> list = getMapAsList();
		
		for(ItemStack i : list)
		{
			if(LootBags.areItemStacksEqualItem(i, is, false, false))
				return true;
		}
		return false;
	}
	public void printMap()
	{
		for(String i: map.keySet())
		{
			System.out.println(i + ": " + map.get(i).itemWeight + ": " + map.get(i).theMinimumChanceToGenerateItem + ": " + map.get(i).theMaximumChanceToGenerateItem);
		}
		System.out.println("Total Weight: " + totalWeight);
	}
}
