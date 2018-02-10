package mal.lootbags.network.message;

import io.netty.buffer.ByteBuf;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StorageMessageClient implements IMessage, IMessageHandler<StorageMessageClient, IMessage>{

	public BlockPos pos;
	public int xpos, ypos, zpos;
	public int outputID, outputindex;
	
	public StorageMessageClient() {}
	public StorageMessageClient(TileEntityStorage te, int outputID, int outputindex)
	{
		pos = te.getPos();
		this.outputID = outputID;
		this.outputindex = outputindex;
	}
	
	@Override
	public IMessage onMessage(StorageMessageClient message, MessageContext ctx) {
		TileEntity te = ctx.getServerHandler().player.getServerWorld().getTileEntity(message.pos);
		if(te instanceof TileEntityStorage)
		{
			((TileEntityStorage)te).setDataServer(message.outputID, message.outputindex);
		}
		return null;
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		xpos = buf.readInt();
		ypos = buf.readInt();
		zpos = buf.readInt();
		pos = new BlockPos(xpos,ypos,zpos);
		outputID = buf.readInt();
		outputindex = buf.readInt();
	}
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(outputID);
		buf.writeInt(outputindex);
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/