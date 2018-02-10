package mal.lootbags.network;

import mal.lootbags.LootBags;
import mal.lootbags.network.message.OpenerMessageServer;
import mal.lootbags.network.message.RecyclerMessageServer;
import mal.lootbags.network.message.StorageMessageClient;
import mal.lootbags.network.message.StorageMessageServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class LootbagsPacketHandler {
	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(LootBags.MODID.toLowerCase());
	
	public static void init()
	{
		instance.registerMessage(RecyclerMessageServer.class, RecyclerMessageServer.class, 0, Side.CLIENT);
		instance.registerMessage(OpenerMessageServer.class, OpenerMessageServer.class, 1, Side.CLIENT);
		instance.registerMessage(StorageMessageServer.class, StorageMessageServer.class, 2, Side.CLIENT);
		instance.registerMessage(StorageMessageClient.class, StorageMessageClient.class, 3, Side.SERVER);
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/