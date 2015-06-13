package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import mal.lootbags.LootBags;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ChestGenHooks;

public class LootSourceCommand implements ICommand{

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public LootSourceCommand()
	{
		aliases.add("lootbags_identifysources");
	}
	
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName() {
		return "/lootbags_identifysources";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/lootbags_identifysources";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommand, String[] p_71515_2_) {
		ArrayList<String> stringlist = new ArrayList<String>();
		
		if(LootBags.LOOTBAGINDUNGEONLOOT != null)
		{
			stringlist.add("==Loot Sources Bags are found in==");
			for(int i = 0; i < LootBags.LOOTBAGINDUNGEONLOOT.length; i++)
			{
				stringlist.add(LootBags.LOOTBAGINDUNGEONLOOT[i]);
			}
		}
		stringlist.add("");
		
		if(LootBags.LOOTCATEGORYLIST != null)
		{
			stringlist.add("==Loot Sources That Provide Loot Found in Bags==");
			for(int i = 0; i < LootBags.LOOTCATEGORYLIST.length; i++)
			{
				stringlist.add(LootBags.LOOTCATEGORYLIST[i]);
			}
		}
		stringlist.add("");
		
		stringlist.add("==Loot Sources Registered in Forge==");
		
		//Reflection to obtain the chestInfo in ChestGenHooks
		try {
			Field info = Class.forName("net.minecraftforge.common.ChestGenHooks").getDeclaredField("chestInfo");
			info.setAccessible(true);
			HashMap chestinfo = (HashMap)info.get(null);
			
			for(int i = 0; i < chestinfo.keySet().size(); i++)
			{
				String category = chestinfo.keySet().toArray()[i].toString();
				int count = ChestGenHooks.getInfo(category).getItems(random).length;
				stringlist.add(category + " contains " + count + " items.");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			File file = new File(Minecraft.getMinecraft().mcDataDir, "dumps/LootBagsSourcesDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			for(String s:stringlist)
			{
				write.println(s);
			}
			icommand.addChatMessage(new ChatComponentText("LootBags Loot Source Dump Written - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			System.err.println("Error in dumping sources... oh dear not again...");
			exception.printStackTrace();
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		// TODO Auto-generated method stub
		return false;
	}

}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/