package mal.lootbags;

public enum BagTypes {

	//typical bags are positive increasing, special bags are negative so they are skipped by things that cross all bags
	Common(0), Uncommon(1), Rare(2), Epic(3), Legendary(4), Bacon(-4), WornOut(-5), Soaryn(-6), Wyld(-7), Mal(-8);
	
	private int index;
	
	private BagTypes(int index)
	{
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public static BagTypes[] droppableValues()
	{
		return new BagTypes[]{Common, Uncommon, Rare, Epic, Legendary};
	}
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/