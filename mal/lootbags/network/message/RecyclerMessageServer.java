package mal.lootbags.network.message;

import java.io.IOException;

import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class RecyclerMessageServer implements IMessage, IMessageHandler<RecyclerMessageServer, IMessage>{

	public BlockPos pos;
	public int xpos, ypos, zpos;
	public int lootbagCount, totalValue;
	
	public RecyclerMessageServer(){}
	public RecyclerMessageServer(TileEntityRecycler te, int count, int value)
	{
		pos = te.getPos();
		lootbagCount = count;
		totalValue = value;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		xpos = buf.readInt();
		ypos = buf.readInt();
		zpos = buf.readInt();
		pos = new BlockPos(xpos,ypos,zpos);
		lootbagCount = buf.readInt();
		totalValue = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(lootbagCount);
		buf.writeInt(totalValue);
	}
	@Override
	public IMessage onMessage(RecyclerMessageServer message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.pos);
				if(te instanceof TileEntityRecycler)
				{
					((TileEntityRecycler)te).setData(message.lootbagCount, message.totalValue);
				}
			}
		});
		return null;
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/