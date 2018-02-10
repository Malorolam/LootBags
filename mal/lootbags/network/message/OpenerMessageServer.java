package mal.lootbags.network.message;

import io.netty.buffer.ByteBuf;
import mal.lootbags.tileentity.TileEntityOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenerMessageServer implements IMessage, IMessageHandler<OpenerMessageServer, IMessage> {

	public BlockPos pos;
	public int xpos, ypos, zpos;
	public int cooldown;
	
	public OpenerMessageServer(){}
	public OpenerMessageServer(TileEntityOpener te, int cd)
	{
		this.cooldown = cd;
		this.pos = te.getPos();
	}
	@Override
	public IMessage onMessage(OpenerMessageServer message, MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.pos);
				if(te instanceof TileEntityOpener)
				{
					((TileEntityOpener)te).setData(message.cooldown);
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
		cooldown = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		buf.writeInt(cooldown);
	}

}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/