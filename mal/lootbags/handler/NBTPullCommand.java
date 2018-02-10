package mal.lootbags.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import mal.lootbags.LootbagsUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/**
 * Hopefully a way to pull item data and use it for configs
 * @author Mal
 *
 */
public class NBTPullCommand implements ICommand{

	private List aliases = new ArrayList();
	
	public NBTPullCommand()
	{
		aliases.add("lootbags_dumphelditem");
	}

	@Override
	public String getName() {
		return "/lootbags_dumphelditem";
	}

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "/lootbags_dumphelditem";
	}

	@Override
	public List getAliases() {
		return aliases;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender icommand, String[] args) throws CommandException {
		EntityPlayer player = null;
		if(icommand instanceof EntityPlayer)
			player = (EntityPlayer)icommand;
		else
		{
			icommand.sendMessage(new TextComponentString("Lootbags Held Item Dump Failed: Did not recognize command sender as a player."));
			return;
		}
		
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if(stack==null || stack.isEmpty())
		{
			icommand.sendMessage(new TextComponentString("Lootbags Held Item Dump Failed: Player has no held item."));
			return;
		}
		byte[] barray = new byte[0];
		if(stack.hasTagCompound())
		{
			try {
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				CompressedStreamTools.writeCompressed(stack.getTagCompound(), ostream);
				barray = ostream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			/*icommand.addChatMessage(new TextComponentString("Lootbags NBT Dump Failed: Held itemstack " + stack.toString() + " has no NBT data."));
			return;*/
		}
		
		try {
			File file = new File(icommand.getServer().getDataDirectory(), "dumps/LootBagsHeldItemDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			String s = "";
			ResourceLocation id = stack.getItem().getRegistryName();
			s = id.getResourceDomain() + ":" + id.getResourcePath() + ":" + stack.getItemDamage() + ":1:" + stack.getMaxStackSize() +":20"; 
			if(barray != null)
			{
				s += ":";
				for(byte b:barray)
				{
					s += b+"|";
				}
			}
			s=s.substring(0, s.length()-1);
			write.print(s);
			icommand.sendMessage(new TextComponentString("LootBags Held Item Dump Written for item " + stack.toString() + " - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			LootbagsUtil.LogError("Error in dumping sources... oh dear not again...");
			exception.printStackTrace();
		}	
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos pos) {
		return null;
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/