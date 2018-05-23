package mal.lootbags.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.handler.BagHandler;

public class LootRegistry {

	private HashMap<Integer, LootEntry> registry;

	private static LootRegistry instance;
	
	public static LootRegistry getInstance()
	{
		if(instance == null)
			instance = new LootRegistry();
		return instance;
	}
	
	public LootRegistry()
	{
		registry = new HashMap<Integer, LootEntry>();
		for(Bag bag:BagHandler.getBagList().values())
		{
			if(!(!LootBags.SHOWSECRETBAGS && bag.getSecret()))
				registry.put(bag.getBagIndex(), new LootEntry(bag));
		}
	}
	
	public List<LootEntry> getLoot()
	{
		return new ArrayList<>(registry.values());
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/