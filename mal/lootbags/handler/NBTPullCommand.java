package mal.lootbags.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
//import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ChestGenHooks;

/**
 * Hopefully a way to pull nbt data and use it for configs
 * @author Mal
 *
 */
public class NBTPullCommand implements ICommand{

	private List aliases = new ArrayList();
	
	public NBTPullCommand()
	{
		aliases.add("lootbags_pullnbt");
	}

	@Override
	public String getCommandName() {
		return "/lootbags_pullnbt";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/lootbags_pullnbt";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommand, String[] p_71515_2_) {
		EntityPlayer player = null;
		if(icommand instanceof EntityPlayer)
			player = (EntityPlayer)icommand;
		else
		{
			icommand.addChatMessage(new ChatComponentText("Lootbags NBT Dump Failed: Did not recognize command sender as a player."));
			return;
		}
		
		ItemStack stack = player.getCurrentEquippedItem();
		if(stack==null)
		{
			icommand.addChatMessage(new ChatComponentText("Lootbags NBT Dump Failed: Player has no held item."));
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
			icommand.addChatMessage(new ChatComponentText("Lootbags NBT Dump Failed: Held itemstack " + stack.toString() + " has no NBT data."));
			return;
		}
		
		try {
			File file = new File(Minecraft.getMinecraft().mcDataDir, "dumps/LootBagsNBTDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			String s = "";
			UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
			s = id.modId + ":" + id.name + ":" + stack.getItemDamage() + ":1:" + stack.getMaxStackSize() +":20:"; 
			for(byte b:barray)
			{
				s += b+"|";
			}
			s=s.substring(0, s.length()-1);
			write.print(s);
			icommand.addChatMessage(new ChatComponentText("LootBags NBT Dump Written for item " + stack.toString() + " - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			LootbagsUtil.LogError("Error in dumping sources... oh dear not again...");
			exception.printStackTrace();
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return true;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		return null;
	}

/*	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender,
			String[] args, BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}*/
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/