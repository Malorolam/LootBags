package mal.lootbags.item;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLLog;

import mal.lootbags.LootBags;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

/**
 * Main item for lootbag
 * 
 * @author Mal
 * 
 */
public class LootbagItem extends Item {

	private static Random random = new Random();

	public LootbagItem() {
		super();
		this.setUnlocalizedName("lootbag");
		this.maxStackSize = 1;
		this.setMaxDamage(1);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	public void addInformation(ItemStack is, EntityPlayer ep, List list,
			boolean bool) {
		if(is.getTagCompound() != null && is.getTagCompound().getBoolean("generated"))
		{
			list.add("\u00A7b" + "What's inside is not as");
			list.add("\u00A7b" + "interesting as not knowing.");
		}
		else
			list.add("\u00A7b" + "Ooh, what could be inside?");
		if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
			list.add("\u00A77" + "Current Drop Rates: Monster: " + LootBags.MONSTERDROPCHANCE + "%");
			list.add("\u00A77" + "Passive: " + LootBags.PASSIVEDROPCHANCE + "% Player: " + LootBags.PLAYERDROPCHANCE + "%");
		}
	}

	public static void setTagCompound(ItemStack is, ItemStack[] inventory) {
		NBTTagCompound nbt = new NBTTagCompound();
			

		// inventory
		NBTTagList nbtinventory = new NBTTagList();

		if (inventory != null) {
			for (int i = 0; i < 5; ++i) {
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setInteger("Slot", i);
				if (inventory[i] != null) {
					inventory[i].writeToNBT(var4);
				}
				nbtinventory.appendTag(var4);
			}
		}

		nbt.setTag("inventory", nbtinventory);
		nbt.setBoolean("generated", true);

		is.setTagCompound(nbt);
	}

	public static ItemStack[] getInventory(ItemStack is) {
		ItemStack[] inventory = new ItemStack[5];

		NBTTagList items = is.stackTagCompound.getTagList("inventory", 10);

		for (int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if (slot >= 0 && slot < inventory.length) {
				ItemStack ii = ItemStack.loadItemStackFromNBT(item);
				inventory[i] = ii;
			}
		}

		return inventory;
	}

	public static void generateInventory(ItemStack is) {
		boolean gen = false;
		if(is.getTagCompound()!=null)
			gen = is.getTagCompound().getBoolean("generated");
		if (!gen) {
			int numitems = random.nextInt(5) + 1;
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList nbtinventory = new NBTTagList();

			for (int i = 0; i < numitems; i++) {
				ItemStack inv = getLootItem();
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setInteger("Slot", i);
				if (inv != null) {
					inv.writeToNBT(var4);
				}
				else
					i--;
				nbtinventory.appendTag(var4);
			}

			nbt.setTag("inventory", nbtinventory);
			nbt.setBoolean("generated", true);
			is.setTagCompound(nbt);
		}
	}
	
	private static ItemStack getLootItem()
	{
		int count = LootBags.LOOTCATEGORYLIST.length;
		int rand = random.nextInt(count);
		ItemStack is = null;
		try {
			is = ChestGenHooks.getOneItem(LootBags.LOOTCATEGORYLIST[rand], random).copy();
		} catch (IllegalArgumentException e) {
			FMLLog.log(Level.ERROR, "DANGER DANGER DANGER!! Attempted Chest Gen Hook \""+LootBags.LOOTCATEGORYLIST[rand]+"\" is unrecognized by Forge or has no items!  You should have listened to the comment in the config!!");
		}
		return is;
	}
	
	/**
	 * Returns true if the stack should be removed
	 * @param is
	 * @return
	 */
	public static boolean checkInventory(ItemStack is)
	{
		boolean gen = is.getTagCompound().getBoolean("generated");
		if(gen)
		{
			ItemStack[] stack = getInventory(is);
			if(stack==null)
				return true;
			boolean b = true;
			for(int i = 0; i < stack.length; i++)
			{
				if(stack[i]!=null && stack[i].stackSize>0)
					b=false;
			}
			return b;
		}
		return false;
	}

	public ItemStack onItemRightClick(ItemStack is, World world,
			EntityPlayer player) {
		if (!world.isRemote) {
			LootbagItem.generateInventory(is);
			player.openGui(LootBags.LootBagsInstance, 0, world, 0, 0, 0);
		}

		return is;
	}

	@Override
	public void registerIcons(IIconRegister ir) {
		this.itemIcon = ir.registerIcon("lootbags:lootbagItemTexture");
	}

	@Override
	public boolean getShareTag() {
		return true;
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
