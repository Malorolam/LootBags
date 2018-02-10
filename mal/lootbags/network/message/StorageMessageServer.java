package mal.lootbags.network.message;

import io.netty.buffer.ByteBuf;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StorageMessageServer implements IMessage, IMessageHandler<StorageMessageServer, IMessage>{

	public BlockPos pos;
	public int xpos, ypos, zpos;
	public int stored_value, outputID, outputindex;
	
	public StorageMessageServer() {}
	public StorageMessageServer(TileEntityStorage te, int stored_value, int outputID, int outputindex)
	{
		pos = te.getPos();
		this.stored_value = stored_value;
		this.outputID = outputID;
		this.outputindex = outputindex;
	}
	@Override
	public IMessage onMessage(StorageMessageServer message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.pos);
				if(te instanceof TileEntityStorage)
				{
					((TileEntityStorage)te).setDataClient(message.stored_value, message.outputID, message.outputindex);
				}
			}
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		xpos = buf.readInt();
		ypos = buf.readInt();
		zpos = buf.readInt();
		pos = new BlockPos(xpos,ypos,zpos);
		stored_value = buf.readInt();
		outputID = buf.readInt();
		outputindex = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(stored_value);
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