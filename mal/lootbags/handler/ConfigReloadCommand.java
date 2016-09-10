package mal.lootbags.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mal.lootbags.LootBags;
import mal.lootbags.config.GeneralConfigHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
	public void execute(MinecraftServer server, ICommandSender icommand, String[] args) throws CommandException {
		//clear the bags
		BagHandler.clearBags();
		LootBags.LOOTMAP.clearMapData();
		icommand.addChatMessage(new TextComponentString("Cleared the existing bags and tables."));
		
		//reload the general config
		GeneralConfigHandler.reloadConfig();
		icommand.addChatMessage(new TextComponentString("Reloaded the general config."));
		
		//reload the bag config
		LootBags.bagconfig.reloadBagConfig(icommand);
		icommand.addChatMessage(new TextComponentString("Reloaded the bag config."));
		
		//repopulate the general map
		LootBags.LOOTMAP.populateGeneralBlacklist(GeneralConfigHandler.getBlacklistConfigData());
		LootBags.LOOTMAP.populateGeneralWhitelist(GeneralConfigHandler.getWhitelistConfigData());
		LootBags.LOOTMAP.populateRecyclerBlacklist(GeneralConfigHandler.getRecyclerBlacklistConfigData());
		LootBags.LOOTMAP.populateRecyclerWhitelist(GeneralConfigHandler.getRecyclerWhitelistConfigData());
		LootBags.LOOTMAP.setLootSources(LootBags.LOOTCATEGORYLIST);
		LootBags.LOOTMAP.populateGeneralMap(FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0]);
		icommand.addChatMessage(new TextComponentString("Repopulated the general map."));
		
		//repopulate the bags
		BagHandler.populateBagLists();
		LootBags.LOOTMAP.setTotalListWeight();
		icommand.addChatMessage(new TextComponentString("Repopulated the bags."));
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canCommandSenderUseCommand(4, getCommandName());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos pos) {
		return null;
	}

}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/