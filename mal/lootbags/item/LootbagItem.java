package mal.lootbags.item;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mal.lootbags.BagTypes;
import mal.lootbags.LootBags;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
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
	private IIcon[] iconlist = new IIcon[10];

	public LootbagItem() {
		super();
		this.setUnlocalizedName("lootbag");
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.hasSubtypes = true;
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	public void addInformation(ItemStack is, EntityPlayer ep, List list,
			boolean bool) {
		switch(is.getItemDamage())
		{
		case 0:
			list.add(EnumChatFormatting.WHITE + "Common");
			break;
		case 1:
			list.add(EnumChatFormatting.GREEN + "Uncommon");
			break;
		case 2:
			list.add(EnumChatFormatting.BLUE + "Rare");
			break;
		case 3:
			list.add(EnumChatFormatting.DARK_PURPLE + "Epic");
			break;
		case 4:
			list.add(EnumChatFormatting.GOLD + "Legendary");
			break;
		case 5:
			list.add("\u00A7d" + "Bacon");
			break;
		case 6:
			list.add(EnumChatFormatting.GRAY + "Worn Out");
			break;
		case 7:
			list.add(EnumChatFormatting.BLUE + "Soaryn");
			break;
		case 8:
			list.add(EnumChatFormatting.RED + "Wyld");
			break;
		case 9:
			list.add(EnumChatFormatting.YELLOW + "Waffle");
			break;
		}
		
		if(is.getTagCompound() != null && is.getTagCompound().getBoolean("generated"))
		{
			if(is.getItemDamage()==5)
			{
				list.add("\u00A7d" + "Turns out there is bacon inside...");
			}
			else if(is.getItemDamage()==6)
			{
				list.add("\u00A7b" + "I told you my bags don't");
				list.add("\u00A7b" + "drop beds! baconNub");
			}
			else if(is.getItemDamage()==7)
			{
				list.add("\u00A7b" + "Everytime a random chest is placed,");
				list.add("\u00A7b" + "a Soaryn gets more Chick Fil A.");
			}
			else if(is.getItemDamage()==8)
			{
				list.add("\u00A7b" + "Raise your Cluckingtons!");
			}
			else if(is.getItemDamage()==9)
			{
				list.add("\u00A7b" + "You have been banned from talking");
				list.add("\u00A7b" + "in this channel's chat.");
			}
			else
			{
				list.add("\u00A7b" + "What's inside is not as");
				list.add("\u00A7b" + "interesting as not knowing.");
			}
		}
		else
			list.add("\u00A7b" + "Ooh, what could be inside?");
		if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
			if(is.getItemDamage() < 5)
			{
				int mchance;
				int pchance;
				int lchance;
				switch(is.getItemDamage())
				{
				case 0:
					mchance = LootBags.CMONSTERDROPCHANCE;
					pchance = LootBags.CPASSIVEDROPCHANCE;
					lchance = LootBags.CPLAYERDROPCHANCE;
					break;
				case 1:
					mchance = LootBags.UMONSTERDROPCHANCE;
					pchance = LootBags.UPASSIVEDROPCHANCE;
					lchance = LootBags.UPLAYERDROPCHANCE;
					break;
				case 2:
					mchance = LootBags.RMONSTERDROPCHANCE;
					pchance = LootBags.RPASSIVEDROPCHANCE;
					lchance = LootBags.RPLAYERDROPCHANCE;
					break;
				case 3:
					mchance = LootBags.EMONSTERDROPCHANCE;
					pchance = LootBags.EPASSIVEDROPCHANCE;
					lchance = LootBags.EPLAYERDROPCHANCE;
					break;
				case 4:
					mchance = LootBags.LMONSTERDROPCHANCE;
					pchance = LootBags.LPASSIVEDROPCHANCE;
					lchance = LootBags.LPLAYERDROPCHANCE;
					break;
				default:
					mchance = LootBags.CMONSTERDROPCHANCE;
					pchance = LootBags.CPASSIVEDROPCHANCE;
					lchance = LootBags.CPLAYERDROPCHANCE;
					break;
				}
				list.add("\u00A77" + "Current Drop Rates: Monster: " + String.format("%.2f", mchance/10.0f) + "%");
				list.add("\u00A77" + "Passive: " + String.format("%.2f", pchance/10.0f) + "% Player: " + String.format("%.2f", lchance/10.0f) + "%");
			}
			else if(is.getItemDamage() == 5)
			{
				list.add("\u00A77" + "Three out of every four bacons agree");
				list.add("\u00A77" + "that they don't have enough bacon.");
				list.add("\u00A77" + "The fourth has a bag full of bacon.");
				list.add("\u00A7b" + "(It still isn't enough bacon.)");
			}
			else if(is.getItemDamage() == 6)
			{
				list.add("\u00A77" + "My bags are not configured");
				list.add("\u00A77" + "to drop beds in this pack.");
				list.add("\u00A77" + "I am 100% certain about this.");
				list.add(EnumChatFormatting.DARK_PURPLE + "~Malorolam");
			}
			else if(is.getItemDamage() == 7)
			{
				list.add("\u00A77" + "One out of ever four chests");
				list.add("\u00A77" + "is a Soaryn chest.");
				list.add("\u00A77" + "Only you can prevent inventory");
				list.add("\u00A77" + "clutter by creating more.");
			}
			else if(is.getItemDamage() == 8)
			{
				list.add("\u00A77" + "Cluck Cluck...");
			}
			else if(is.getItemDamage() == 9)
			{
				list.add("\u00A77" + "(Hay) (Cha)rcoal (T)orch");
			}
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
			int numitems;
			switch(is.getItemDamage())
			{
			case 6:
			case 8:
				numitems = 1;
				break;
			case 7:
				numitems = 3;
				break;
			case 9:
				numitems = 3;
				break;
			default:
				numitems = (random.nextInt(5) + 1);
				break;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList nbtinventory = new NBTTagList();

			for (int i = 0; i < numitems; i++) {
				ItemStack inv = getLootItem(is.getItemDamage(), i);
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setInteger("Slot", i);
				if (inv != null && inv.stackSize>0) {
					inv.writeToNBT(var4);
				}
				else
				{
					//System.out.println("Skipping null slot.");
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
	
	private static ItemStack getLootItem(int damage, int slot){return getLootItem(0, damage, slot);}
	private static ItemStack getLootItem(int rerollCount, int damage, int slot)
	{
		if(damage == 5)
		{
			ItemStack[] stacks;
			if(random.nextInt(2)==0)
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.porkchop), 4, 16);
			else
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.cooked_porkchop), 4, 16);
	        return (stacks.length > 0 ? stacks[0] : null);
		}
		if(damage == 6)
		{
			ItemStack[] stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.cake), 1, 1);
	        return (stacks.length > 0 ? stacks[0] : null);
		}
		if(damage == 7)
		{
			ItemStack[] stacks;
			switch(slot)
			{
			case 0:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Blocks.chest), 1, 2);
				break;
			case 1:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.stick), 1, 1);
				break;
			case 2:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.quartz), 4, 4);
				break;
			default:
				stacks = null;
			}
			return(stacks.length > 0 ? stacks[0] : null);
		}
		if(damage == 8)
		{
			ItemStack[] stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.spawn_egg,1,93), 1, 1);
			return (stacks.length > 0 ? stacks[0] : null);
		}
		if(damage == 9)
		{
			ItemStack[] stacks;
			switch(slot)
			{
			case 0:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Blocks.hay_block), 1, 1);
				break;
			case 1:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Items.coal,1,1), 2, 2);
				break;
			case 2:
				stacks = ChestGenHooks.generateStacks(random, new ItemStack(Blocks.torch), 3, 3);
				break;
			default:
				stacks = null;
			}
			return(stacks.length > 0 ? stacks[0] : null);
		}
		if(random.nextInt(1000000)==0)
		{
			ItemStack[] stacks = ChestGenHooks.generateStacks(random, new ItemStack(LootBags.lootbag, 1, 6), 1, 1);
	        return (stacks.length > 0 ? stacks[0] : null);
		}
		boolean reroll = false;
		ItemStack is = LootBags.LOOTMAP.getRandomItem(getWeightFromDamage(damage), getTypeFromDamage(damage));
		if(is == null || is.getItem()==null || is.stackSize<= 0)
			reroll = true;
		if(reroll && rerollCount<LootBags.MAXREROLLCOUNT)
		{
			return getLootItem(++rerollCount, damage);
		}
		else if (rerollCount>=LootBags.MAXREROLLCOUNT)
			return null;
		return is;
	}
	
	private static BagTypes getTypeFromDamage(int damage)
	{
		switch(damage)
		{
		case 0:
			return BagTypes.Common;
		case 1:
			return BagTypes.Uncommon;
		case 2:
			return BagTypes.Rare;
		case 3:
			return BagTypes.Epic;
		case 4:
			return BagTypes.Legendary;
		case 5:
			return BagTypes.Bacon;
		case 6:
			return BagTypes.WornOut;
		default:
			return BagTypes.Common;
		}
	}
	
	private static int getWeightFromDamage(int damage)
	{
		switch(damage)
		{
		case 0:
			return -1;
		case 1:
			return LootBags.LOOTMAP.generatePercentileWeight(75);
		case 2:
			return LootBags.LOOTMAP.generatePercentileWeight(50);
		case 3:
			return LootBags.LOOTMAP.generatePercentileWeight(25);
		case 4:
			return LootBags.LOOTMAP.generatePercentileWeight(5);
		default:
			return -1;
		}
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
							if(ite.isItemValidForSlot(i, stack) && (itstack==null || (LootBags.areItemStacksEqualItem(itstack, stack, false, true) || ite.getStackInSlot(i) == null)))
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
	public String getUnlocalizedName(ItemStack is)
	{
		switch(is.getItemDamage())
		{
		case 0:
			return "item.lootbag.common";
		case 1:
			return "item.lootbag.uncommon";
		case 2:
			return "item.lootbag.rare";
		case 3:
			return "item.lootbag.epic";
		case 4:
			return "item.lootbag.legendary";
		case 5:
			return "item.lootbag.bacon";
		case 6:
			return "item.lootbag.wornout";
		case 7:
			return "item.lootbag.soaryn";
		case 8:
			return "item.lootbag.wyld";
		case 9:
			return "item.lootbag.waffle";
		default:
			return "item.lootbag.derp";
		}
	}
	@Override
	public void registerIcons(IIconRegister ir) {
		iconlist[0] = ir.registerIcon("lootbags:lootbagCommonItemTexture");
		iconlist[1] = ir.registerIcon("lootbags:lootbagUncommonItemTexture");
		iconlist[2] = ir.registerIcon("lootbags:lootbagRareItemTexture");
		iconlist[3] = ir.registerIcon("lootbags:lootbagEpicItemTexture");
		iconlist[4] = ir.registerIcon("lootbags:lootbagLegendaryItemTexture");
		iconlist[5] = ir.registerIcon("lootbags:lootbagBaconItemTexture");
		iconlist[6] = ir.registerIcon("lootbags:lootbagCommonItemTexture");
		iconlist[7] = ir.registerIcon("lootbags:lootbagSoarynItemTexture");
		iconlist[8] = ir.registerIcon("lootbags:lootbagWyldItemTexture");
		iconlist[9] = ir.registerIcon("lootbags:lootbagWaffleItemTexture");
	}

	public IIcon getIconFromDamage(int value)
	{
		return iconlist[value];
	}
	
    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public IIcon getBlockTextureFromSideAndMetadata(int par1, int par2)
    {
        if (par2 < 0 || par2 >= this.iconlist.length)
        {
            par2 = 0;
        }

        return this.iconlist[par2];
    }
    
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
        //par3List.add(new ItemStack(par1, 1, 5));
        //par3List.add(new ItemStack(par1, 1, 7));
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
