package mal.lootbags.network;

import mal.lootbags.LootBags;
import mal.lootbags.network.message.OpenerMessageServer;
import mal.lootbags.network.message.RecyclerMessageServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class LootbagsPacketHandler {
	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(LootBags.MODID.toLowerCase());
	
	public static void init()
	{
		instance.registerMessage(RecyclerMessageServer.class, RecyclerMessageServer.class, 0, Side.CLIENT);
		instance.registerMessage(OpenerMessageServer.class, OpenerMessageServer.class, 1, Side.CLIENT);
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/