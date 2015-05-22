package mal.lootbags.handler;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.BagTypes;
import mal.lootbags.LootBags;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
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
		if(!LootBags.REVERSEQUALITY)
			stringlist.add("XXXX Max Weights Per Bag Tier XXXX");
		else
			stringlist.add("XXXX Min Weights Per Bag Tier XXXX");
		stringlist.add("Common: " + LootBags.LOOTMAP.getLargestWeight());
		stringlist.add("Uncommon: " + LootBags.LOOTMAP.generatePercentileWeight(75, BagTypes.Uncommon));
		stringlist.add("Rare: " + LootBags.LOOTMAP.generatePercentileWeight(50, BagTypes.Rare));
		stringlist.add("Epic: " + LootBags.LOOTMAP.generatePercentileWeight(25, BagTypes.Epic));
		stringlist.add("Legendary: " + LootBags.LOOTMAP.generatePercentileWeight(5, BagTypes.Legendary));
		stringlist.add("");
		stringlist.add("XXXX LootBags Drop Table XXXX");
		stringlist.add("modid  itemname  itemdamage  droppercent weight");
		for(WeightedRandomChestContent c : LootBags.LOOTMAP.getMapAsChestList())
		{
			UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(c.theItemId.getItem());
			if(u != null && c.theItemId != null)
			{
				float percent = (100.0f*c.itemWeight)/LootBags.LOOTMAP.getTotalWeight();
				stringlist.add(u.modId + "  " + u.name + "  " + c.theItemId.getItemDamage() + "  " + String.format("%.3f", percent) + "  " + c.itemWeight);
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
		stringlist.add("XXXX LootBags Blacklist XXXX");
		for(int i = 0; i < LootBags.BLACKLIST.size(); i++)
		{
			switch(i)
			{
			case 0:
				stringlist.add("Global Blacklist:");
				break;
			case 1:
				stringlist.add("Common Bag Blacklist:");
				break;
			case 2:
				stringlist.add("Uncommon Bag Blacklist:");
				break;
			case 3:
				stringlist.add("Rare Bag Blacklist:");
				break;
			case 4:
				stringlist.add("Epic Bag Blacklist:");
				break;
			case 5:
				stringlist.add("Legendary Bag Blacklist:");
				break;
			}
			
			for(ItemStack is:LootBags.BLACKLIST.get(i))
			{
				UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(is.getItem());
				stringlist.add(u.modId + "  " + u.name + "  " + + is.getItemDamage());
			}
		}
		
		stringlist.add("");
		stringlist.add("XXXX LootBags Blacklisted Mod IDs XXXX");
		for(int i = 0; i < LootBags.MODBLACKLIST.size(); i++)
		{
			stringlist.add(LootBags.MODBLACKLIST.get(0));
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
	public List addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] p_71516_2_) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
		return false;
	}

}
