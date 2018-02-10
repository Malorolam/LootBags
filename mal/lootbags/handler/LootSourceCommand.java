package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.loot.LootTableList;

public class LootSourceCommand implements ICommand{

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public LootSourceCommand()
	{
		aliases.add("lootbags_identifysources");
	}

	@Override
	public String getName() {
		return "/lootbags_identifysources";
	}

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "/lootbags_identifysources";
	}

	@Override
	public List getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender icommand, String[] args) throws CommandException {
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
		
		for(ResourceLocation loc: LootTableList.getAll())
		{
			stringlist.add(loc.toString());
		}
/*		//Reflection to obtain the chestInfo in ChestGenHooks
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
		}*/
		
		//pull the full enchantment unlocalized names
		stringlist.add("");
		stringlist.add("==Enchantment Unlocalized Names==");
		for(int i = 0; i < Enchantment.REGISTRY.getKeys().size(); i++)
		{
			if(Enchantment.REGISTRY.getObjectById(i) != null)
			{
				stringlist.add(new TextComponentTranslation(Enchantment.REGISTRY.getObjectById(i).getName()).getFormattedText() + ": " + Enchantment.REGISTRY.getObjectById(i).getName());
			}
		}
		
		try {
			File file = new File(icommand.getServer().getDataDirectory(),"./dumps/LootBagsSourcesDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			for(String s:stringlist)
			{
				write.println(s);
			}
			icommand.sendMessage(new TextComponentString("LootBags Loot Source Dump Written - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			LootbagsUtil.LogError("Error in dumping sources... oh dear not again...");
			exception.printStackTrace();
		}
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