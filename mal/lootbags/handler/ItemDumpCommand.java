package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.LootBags;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

public class ItemDumpCommand implements ICommand{

	private List aliases = new ArrayList();
	private Random random = new Random();
	
	public ItemDumpCommand()
	{
		aliases.add("lootbagsitemdump");
	}
	
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName() {
		return "/lootbagsitemdump";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/lootbagsitemdump";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommand, String[] astring) {
		ArrayList<String> stringlist = new ArrayList<String>();
		stringlist.add("modid  itemname  itemdamage  dropweighting");
		for(String s:LootBags.LOOTBAGINDUNGEONLOOT)
		{
			stringlist.add("XXXX " + s + " XXXX");
			WeightedRandomChestContent[] contents = ChestGenHooks.getItems(s, random);
			for(WeightedRandomChestContent con:contents)
			{
				UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(con.theItemId.getItem());
				if(u != null)
					stringlist.add(u.modId + "  " + u.name + "  " + con.theItemId.getItemDamage() + "  " + con.itemWeight);
			}
			stringlist.add("");
		}
		
		stringlist.add("XXXX LootBags Whitelist XXXX");
		for(int i = 0; i < LootBags.LOOTWHITELIST.size(); i++)
		{
			UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(LootBags.LOOTWHITELIST.get(i).getItem());
			stringlist.add(u.modId + "  " + u.name + "  " + + LootBags.LOOTWHITELIST.get(i).getItemDamage() + "  " + LootBags.WHITELISTCHANCE.get(i));
		}
		
		stringlist.add("");
		stringlist.add("XXXX LootBags Blacklist XXXX");
		for(int i = 0; i < LootBags.LOOTBLACKLIST.size(); i++)
		{
			UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(LootBags.LOOTBLACKLIST.get(i).getItem());
			stringlist.add(u.modId + "  " + u.name + "  " + + LootBags.LOOTBLACKLIST.get(i).getItemDamage());
		}
		
		stringlist.add("");
		stringlist.add("XXXX LootBags Blacklisted Mod IDs XXXX");
		for(int i = 0; i < LootBags.MODBLACKLIST.size(); i++)
		{
			stringlist.add(LootBags.MODBLACKLIST.get(0));
		}
		
		try {
			File file = new File(Minecraft.getMinecraft().mcDataDir, "logs/LootBagsItemDump.txt");
			if(!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			if(!file.exists())
				file.createNewFile();
			
			PrintWriter write = new PrintWriter(file);
			
			for(String s:stringlist)
			{
				write.println(s);
			}
			icommand.addChatMessage(new ChatComponentText("LootBags Item Dump Written - Look in your logs folder"));
			
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
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

}
