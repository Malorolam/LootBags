package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.loot.LootItem;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class ItemDumpCommand implements ICommand{

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public ItemDumpCommand()
	{
		aliases.add("lootbags_itemdump");
	}

	@Override
	public String getName() {
		return "/lootbags_itemdump";
	}

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "/lootbags_itemdump";
	}

	@Override
	public List getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender icommand, String[] args) throws CommandException {
		ArrayList<String> stringlist = new ArrayList<String>();
		stringlist.add("XXXX LootBags General Drop Table XXXX");
		stringlist.add("<modid>:<itemname>:<itemdamage>:<droppercent>:<weight>:<min>:<max>");
		for(LootItem it : LootBags.LOOTMAP.getMap().values())
		{
			if(it != null && !it.getContentItem().isEmpty())
			{
				float percent = (100.0f*it.getItemWeight())/LootBags.LOOTMAP.getTotalListWeight();
				stringlist.add(it.getItemModID() + ":" + it.getItemName() + ":" + it.getContentItem().getItemDamage() + ":" + String.format("%.3f", percent) + ":" + it.getItemWeight() + ":" + it.getMinStack() + ":" + it.getMaxStack());
			}
			else if(!it.getContentItem().isEmpty())
			{
				stringlist.add(it.getContentItem().toString() + ": Unique Identifier not found.");
			}
			else
			{
				stringlist.add("Found null item.  Whatever this is probably can't be dropped");
			}
		}
		
		stringlist.add("");
		for(Bag bag:BagHandler.getBagList().values())
		{
			stringlist.add("XXXX " + bag.getBagName() + " Drop Table XXXX");
			for(LootItem it: bag.getMap().values())
			{
				if(it != null && !it.getContentItem().isEmpty())
				{
					float percent = (100.0f*it.getItemWeight()/LootBags.LOOTMAP.getTotalListWeight());
					stringlist.add(it.getItemModID() + ":" + it.getItemName() + ":" + it.getContentItem().getItemDamage() + ":" + String.format("%.3f", percent) + ":" + it.getItemWeight() + ":" + it.getMinStack() + ":" + it.getMaxStack());
				}
				else if(!it.getContentItem().isEmpty())
				{
					stringlist.add(it.getContentItem().toString() + ": Unique Identifier not found.");
				}
				else
				{
					stringlist.add("Found null item.  Whatever this is probably can't be dropped");
				}
			}
		}
		
		try {
			File file = new File(icommand.getServer().getDataDirectory(), "./dumps/LootBagsItemDump.txt");
			//System.out.println(file.getAbsolutePath());
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			for(String s:stringlist)
			{
				write.println(s);
			}
			icommand.sendMessage(new TextComponentString("LootBags Item Dump Written - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			LootbagsUtil.LogError("Error in dumping items... oh dear not again...");
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