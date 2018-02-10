package mal.lootbags.config;

public class BagEntitySource {

	private String name;//name of the entity in the black/whitelist
	private boolean visibleName;//if it's the visible name or internal name
	
	public BagEntitySource(String name, boolean isVisibleName)
	{
		this.name=name;
		visibleName=isVisibleName;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean getIsVisibleName()
	{
		return visibleName;
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/