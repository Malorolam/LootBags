package mal.lootbags.recipe;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import mal.lootbags.LootBags;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class StorageCondition implements IConditionFactory{

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		return new BooleanSupplier()
		{
			@Override
			public boolean getAsBoolean()
			{
				return !LootBags.DISABLESTORAGE;
			}
		};
	}

}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/