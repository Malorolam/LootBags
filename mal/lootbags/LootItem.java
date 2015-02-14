package mal.lootbags;

import java.util.ArrayList;

import net.minecraft.util.WeightedRandomChestContent;

public class LootItem {

	private WeightedRandomChestContent item;
	private BagTypes[] possibleBags = new BagTypes[BagTypes.droppableValues().length];//array of bags the item can drop from
	
	/**
	 * An object containing an item and an array of bags that item can appear in
	 * It is possible for an item to not show up due to interactions between bag restrictions and weighting
	 * min and max are inclusive, so Common, Legendary will include every bag
	 */
	public LootItem(WeightedRandomChestContent item, BagTypes min, BagTypes max)
	{
		this.item = item;
		int sindex = min.getIndex();
		int eindex = max.getIndex();
		if(sindex < eindex)
		{
			for(int i = sindex; i<=eindex; i++)
				possibleBags[i] = BagTypes.droppableValues()[i];
		}
		else if(sindex > eindex)//backwards somehow
			for(int i = eindex; i <= sindex; i++)
				possibleBags[i] = BagTypes.droppableValues()[i];
		else
			possibleBags[sindex] = BagTypes.droppableValues()[sindex];
	}
	public LootItem(WeightedRandomChestContent item, BagTypes type)
	{
		this(item, type, type);
	}
	public LootItem(WeightedRandomChestContent item, ArrayList<BagTypes> types)
	{
		this.item = item;
		for(int i = 0; i < types.size(); i++)
		{
			if(types.get(i) != null)
			possibleBags[types.get(i).getIndex()] = types.get(i);
		}
	}
	
	public void addBagType(BagTypes type)
	{
		int index = type.getIndex();
		possibleBags[index] = type;
	}
	
	public void removeBagType(BagTypes type)
	{
		int index = type.getIndex();
		possibleBags[index] = null;
	}
	
	public void removeBagTypes(ArrayList<BagTypes> type)
	{
		for(int i = 0; i < type.size(); i++)
			removeBagType(type.get(i));
	}

	public boolean canDrop(BagTypes type)
	{
		if(possibleBags[type.getIndex()] != null)
			return true;
		return false;
	}
	
	public BagTypes[] getPossibleBags()
	{
		return possibleBags;
	}
			
	public WeightedRandomChestContent getContentItem()
	{
		return item;
	}
	
	
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/