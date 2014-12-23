package mal.lootbags.item;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import mal.lootbags.LootBags;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
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
				if (inv != null && inv.stackSize>0) {
					inv.writeToNBT(var4);
				}
				else
				{
					System.out.println("Skipping null slot.");
					i--;
					numitems--;
				}
				nbtinventory.appendTag(var4);
			}

			nbt.setTag("inventory", nbtinventory);
			nbt.setBoolean("generated", true);
			is.setTagCompound(nbt);
		}
	}
	
	private static ItemStack getLootItem(){return getLootItem(0);}
	private static ItemStack getLootItem(int rerollCount)
	{
		int count = LootBags.LOOTCATEGORYLIST.length + ((LootBags.LOOTWHITELIST.isEmpty())?(0):(1));
		int rand = random.nextInt(count);
		boolean reroll = false;
		ItemStack is = null;
		
		if(rand==LootBags.LOOTCATEGORYLIST.length)
		{
			int i = random.nextInt(LootBags.LOOTWHITELIST.size());
			if(random.nextInt(100)<LootBags.WHITELISTCHANCE.get(i))
			{
				is = LootBags.LOOTWHITELIST.get(i).copy();
				int stack = random.nextInt((is.stackSize<=is.getMaxStackSize())?(is.stackSize):(is.getMaxStackSize()))+1;
				is.stackSize = stack;
			}
			else
			{
				reroll = true;
			}
		}
		else {
			try {
				is = ChestGenHooks.getOneItem(LootBags.LOOTCATEGORYLIST[rand], random).copy();
			} catch (IllegalArgumentException e) {
				FMLLog.log(Level.ERROR, "DANGER DANGER DANGER!! Attempted Chest Gen Hook \""+LootBags.LOOTCATEGORYLIST[rand]+"\" is unrecognized by Forge or has no items!  You should have listened to the comment in the config!!");
			}
		}
		if(!reroll)
		{
			UniqueIdentifier u = GameRegistry.findUniqueIdentifierFor(is.getItem());
			for(String modid:LootBags.MODBLACKLIST)
			{
				if(modid.equalsIgnoreCase(u.modId))
					reroll = true;
			}
			
			for(ItemStack istack:LootBags.LOOTBLACKLIST)
			{
				if(is.isItemEqual(istack))
				{
					reroll = true;
				}
			}
		}
		if(reroll && rerollCount<LootBags.MAXREROLLCOUNT)
		{
			return getLootItem(++rerollCount);
		}
		else if (rerollCount>=LootBags.MAXREROLLCOUNT)
			return null;
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
		if (!world.isRemote && !player.isSneaking()) {
			LootbagItem.generateInventory(is);
			player.openGui(LootBags.LootBagsInstance, 0, world, 0, 0, 0);
		}

		return is;
	}
	
	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz)
	{
		if(!world.isRemote)
		{
			if(!player.isSneaking())
				return false;
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof IInventory)
			{
				LootbagItem.generateInventory(is);
				ItemStack[] iss = LootbagItem.getInventory(is);
				for(int j = 0; j < iss.length; j++)
				{
					ItemStack stack = iss[j];
					if(stack!=null)
					{
						IInventory ite = ((IInventory)te);
						int size = ite.getSizeInventory();
						for(int i = 0; i < size; i++)
						{
							ItemStack itstack = ite.getStackInSlot(i);
							if(ite.isItemValidForSlot(i, stack) && (itstack==null || (LootBags.areItemStacksEqualItem(itstack, stack) || ite.getStackInSlot(i) == null)))
							{
								if(itstack == null)
								{
									ite.setInventorySlotContents(i, stack);
									iss[j] = null;
									break;
								}
								else if(itstack.stackSize+stack.stackSize<=itstack.getMaxStackSize())
								{
									itstack.stackSize += stack.stackSize;
									iss[j] = null;
									break;
								}
								else if(itstack.stackSize<itstack.getMaxStackSize())
								{
									int diff =  itstack.getMaxStackSize()-itstack.stackSize;
									ite.getStackInSlot(i).stackSize = ite.getStackInSlot(i).getMaxStackSize();
									stack.stackSize -= diff;
									if(stack.stackSize<=0)
									{
										iss[j] = null;
										break;
									}	
								}
							}
						}
					}
				}
				LootbagItem.setTagCompound(is, iss);
				if(LootbagItem.checkInventory(is))
					player.inventory.mainInventory[player.inventory.currentItem] = null;
				return true;
			}
		}
		return false;
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
