package mal.lootbags.network.message;

import java.io.IOException;

import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RecyclerMessageServer implements IMessage, IMessageHandler<RecyclerMessageServer, IMessage>{

	public int xpos, ypos, zpos;
	public int lootbagCount, totalValue;
	
	public RecyclerMessageServer(){}
	public RecyclerMessageServer(TileEntityRecycler te, int count, int value)
	{
		xpos = te.xCoord;
		ypos = te.yCoord;
		zpos = te.zCoord;
		lootbagCount = count;
		totalValue = value;
	}
	@Override
	public IMessage onMessage(RecyclerMessageServer message, MessageContext ctx) {
		TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.xpos, message.ypos, message.zpos);
		if(te instanceof TileEntityRecycler)
		{
			((TileEntityRecycler)te).setData(message.lootbagCount, message.totalValue);
		}
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		xpos = buf.readInt();
		ypos = buf.readInt();
		zpos = buf.readInt();
		lootbagCount = buf.readInt();
		totalValue = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(xpos);
		buf.writeInt(ypos);
		buf.writeInt(zpos);
		buf.writeInt(lootbagCount);
		buf.writeInt(totalValue);
	}
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/