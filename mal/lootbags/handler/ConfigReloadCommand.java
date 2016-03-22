package mal.lootbags.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mal.lootbags.LootBags;
import mal.lootbags.config.GeneralConfigHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/*
 * Reloads the configs and the loot tables to ease pack makers a bit
 */
public class ConfigReloadCommand implements ICommand {

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public ConfigReloadCommand()
	{
		aliases.add("lootbags_reloadconfig");
	}
	
	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "/lootbags_reloadconfig";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/lootbags_reloadconfig";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommand, String[] p_71515_2_) {
		//clear the bags
		BagHandler.clearBags();
		LootBags.LOOTMAP.clearMapData();
		icommand.addChatMessage(new ChatComponentText("Cleared the existing bags and tables."));
		
		//reload the general config
		GeneralConfigHandler.reloadConfig();
		icommand.addChatMessage(new ChatComponentText("Reloaded the general config."));
		
		//reload the bag config
		LootBags.bagconfig.reloadBagConfig();
		icommand.addChatMessage(new ChatComponentText("Reloaded the bag config."));
		
		//repopulate the general map
		LootBags.LOOTMAP.populateGeneralBlacklist(GeneralConfigHandler.getBlacklistConfigData());
		LootBags.LOOTMAP.populateGeneralWhitelist(GeneralConfigHandler.getWhitelistConfigData());
		LootBags.LOOTMAP.setLootSources(LootBags.LOOTCATEGORYLIST);
		LootBags.LOOTMAP.populateGeneralMap();
		icommand.addChatMessage(new ChatComponentText("Repopulated the general map."));
		
		//repopulate the bags
		BagHandler.populateBagLists();
		icommand.addChatMessage(new ChatComponentText("Repopulated the bags."));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommand) {
		return icommand.canCommandSenderUseCommand(4, getCommandName());
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/