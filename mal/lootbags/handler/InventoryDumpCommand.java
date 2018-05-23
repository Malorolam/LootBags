package mal.lootbags.handler;

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

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryDumpCommand implements ICommand {

    private List aliases = new ArrayList();
    private Random random = new Random();

    public InventoryDumpCommand() { aliases.add("lootbags_dumpinventory");}

    @Override
    public String getName() { return "/lootbags_dumpinventory"; }

    @Override
    public String getUsage(ICommandSender sender) { return "/lootbags_dumpinventory"; }

    @Override
    public List<String> getAliases() { return aliases; }

    @Override
    public void execute(MinecraftServer server, ICommandSender icommand, String[] args) throws CommandException {
        EntityPlayer player = null;
        if(icommand instanceof EntityPlayer)
            player = (EntityPlayer)icommand;
        else
        {
            icommand.sendMessage(new TextComponentString("Lootbags Inventory Dump Failed: Did not recognize command sender as a player."));
            return;
        }

        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.addAll(player.inventory.mainInventory);
        stacks.addAll(player.inventory.offHandInventory);
        boolean flag = true;
        for(ItemStack stack:stacks)
            if(!stack.isEmpty())
                flag=false;
        if(stacks.isEmpty() || flag)
        {
            icommand.sendMessage(new TextComponentString("Lootbags Inventory Dump Failed: Player has no items in inventory."));
            return;
        }

        try {
            File file = new File(icommand.getServer().getDataDirectory(), "dumps/LootBagsInventoryDump.txt");
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if(!file.exists())
                file.createNewFile();

            PrintWriter write = new PrintWriter(file);

            for(ItemStack stack:stacks) {
                byte[] barray = new byte[0];
                if (stack.hasTagCompound()) {
                    try {
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        CompressedStreamTools.writeCompressed(stack.getTagCompound(), ostream);
                        barray = ostream.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
			/*icommand.addChatMessage(new TextComponentString("Lootbags NBT Dump Failed: Held itemstack " + stack.toString() + " has no NBT data."));
			return;*/
                }

                String s = "";
                ResourceLocation id = stack.getItem().getRegistryName();
                s = id.getResourceDomain() + ":" + id.getResourcePath() + ":" + stack.getItemDamage() + ":" + stack.getCount() + ":" + stack.getMaxStackSize() + ":20";
                if (barray != null) {
                    s += ":";
                    for (byte b : barray) {
                        s += b + "|";
                    }
                }
                s = s.substring(0, s.length() - 1);
                write.println(s);
            }




            icommand.sendMessage(new TextComponentString("LootBags Inventory Dump Written - Look in your dumps folder"));

            write.close();
        } catch (Exception exception) {
            LootbagsUtil.LogError("Error in dumping sources... oh dear not again...");
            exception.printStackTrace();
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) { return null; }

    @Override
    public boolean isUsernameIndex(String[] args, int index) { return false; }

    @Override
    public int compareTo(ICommand o) { return this.getName().compareTo(o.getName()); }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 *
 *********************************************************************************/