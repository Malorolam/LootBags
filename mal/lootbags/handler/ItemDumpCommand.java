package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.loot.LootItem;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
//import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public class ItemDumpCommand implements ICommand{

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public ItemDumpCommand()
	{
		aliases.add("lootbags_itemdump");
	}

	@Override
	public String getCommandName() {
		return "/lootbags_itemdump";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/lootbags_itemdump";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommand, String[] astring) {
		ArrayList<String> stringlist = new ArrayList<String>();
		stringlist.add("XXXX LootBags General Drop Table XXXX");
		stringlist.add("<modid>:<itemname>:<itemdamage>:<droppercent>:<weight>");
		for(LootItem it : LootBags.LOOTMAP.getMap().values())
		{
			WeightedRandomChestContent c = it.getContentItem();
			UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem());
			if(u != null && c.theItemId != null)
			{
				float percent = (100.0f*c.itemWeight)/LootBags.LOOTMAP.getTotalWeight();
				stringlist.add(u.modId + ":" + u.name + ":" + c.theItemId.getItemDamage() + ":" + String.format("%.3f", percent) + ":" + c.itemWeight);
			}
			else if(c.theItemId != null)
			{
				stringlist.add(c.toString() + ": Unique Identifier not found.");
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
				WeightedRandomChestContent c = it.getContentItem();
				UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem());
				if(u != null && c.theItemId != null)
				{
					float percent = (100.0f*c.itemWeight)/LootBags.LOOTMAP.getTotalWeight();
					stringlist.add(u.modId + ":" + u.name + ":" + c.theItemId.getItemDamage() + ":" + String.format("%.3f", percent) + ":" + c.itemWeight);
				}
				else if(c.theItemId != null)
				{
					stringlist.add(c.toString() + ": Unique Identifier not found.");
				}
				else
				{
					stringlist.add("Found null item.  Whatever this is probably can't be dropped");
				}
			}
		}
		
		try {
			File file = new File(Minecraft.getMinecraft().mcDataDir, "dumps/LootBagsItemDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			for(String s:stringlist)
			{
				write.println(s);
			}
			icommand.addChatMessage(new ChatComponentText("LootBags Item Dump Written - Look in your dumps folder"));
			
			write.close();
		} catch (Exception exception) {
			System.err.println("Error in dumping items... oh dear not again...");
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
		return 0;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender,
			String[] args, BlockPos pos) {
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