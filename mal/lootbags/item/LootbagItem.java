package mal.lootbags.item;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.handler.BagHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
//import mal.lootbags.rendering.IItemVarientDetails;
//import mal.lootbags.rendering.ItemRenderingRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * New Lootbag Item class, to use the updated config and bag creation
 */
public class LootbagItem extends Item {//implements IItemVarientDetails{
	
	private final String name = "lootbag";
	
	private IIcon[] iconlist;
	
	public LootbagItem()
	{
		GameRegistry.registerItem(this, name);
		this.setUnlocalizedName(LootBags.MODID + "_" + name);
		setCreativeTab(CreativeTabs.tabMisc);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		iconlist = new IIcon[BagHandler.getHighestUsedID()+1];
	}
	
	public String getName()
	{
		return name;
	}
	
    public String getItemStackDisplayName(ItemStack stack)
    {
        String s = ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
        Bag b = BagHandler.getBag(stack.getItemDamage());
        if(b!= null)
        	return b.getBagNameColor()+s;
        else
        	return s;
    }
    
    public void addInformation(ItemStack is, EntityPlayer ep, List list, boolean bool) {
    	Bag b = BagHandler.getBag(is.getItemDamage());
    	if(b==null)
    		return;
    	
    	if(b.isBagEmpty())
    	{
    		list.add(EnumChatFormatting.RED + "Bag Disabled - Loot Table is empty.");
    		list.add(EnumChatFormatting.RED + "This is not good.");
    		return;
    	}
    	
    	if(is.getTagCompound() != null && is.getTagCompound().getBoolean("generated"))//bag has generated inventory
    	{
    		for(String s: b.getBagTextOpened())
    			list.addAll(parseTextCommand(s,b));
    	}
    	else
    	{
    		for(String s: b.getBagTextUnopened())
    			list.addAll(parseTextCommand(s,b));
    	}
    	
    	if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54))
    	{
    		for(String s: b.getBagTextShift())
    			list.addAll(parseTextCommand(s,b));
    	}
    }

	private ArrayList<String> parseTextCommand(String text, Bag currentBag)
	{
		String sret = "";
		if(currentBag==null)
			return new ArrayList<String>();
		
		String color = "";
		if(text.startsWith("§"))
		{
			color = text.substring(0, 2);
			text = text.substring(2);
		}
		
		switch(text.toUpperCase())
		{
			case "DROPCHANCES":
			{
				sret = StatCollector.translateToLocal("name."+LootBags.MODID+"_"+currentBag.getBagName()+".name") + " drop chances: Monster: " + currentBag.getMonsterDropChance()
						+ " Passive: " + currentBag.getPassiveDropChance() + " Player: " + currentBag.getPlayerDropChance() + " Boss: " + currentBag.getBossDropChance();
				break;
			}
			default:
			{
				sret = text;
			}
		}
		
		return LootbagsUtil.addLineBreaks(sret, color);
	}
	
	@Override
	public void registerIcons(IIconRegister ir) {
		for(Bag b:BagHandler.getBagList().values())
		{
			iconlist[b.getBagIndex()] = ir.registerIcon(LootBags.MODID+":"+b.getBagName()+"ItemTexture");
		}
	}
	
	public IIcon getIconFromDamage(int value)
	{
        if (value < 0 || value >= this.iconlist.length)
        {
            value = 0;
        }
        
		return iconlist[value];
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

		NBTTagList items = is.getTagCompound().getTagList("inventory", 10);

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
	
	private static void generateInventory(ItemStack is) {
			
			boolean gen = false;
			if(is.getTagCompound()!=null)
				gen = is.getTagCompound().getBoolean("generated");
			if (!gen) {
				int numitems = getNumItems(is);
				
				ItemStack[] items = new ItemStack[numitems];
				
				NBTTagCompound nbt = new NBTTagCompound();
				NBTTagList nbtinventory = new NBTTagList();

				for (int i = 0; i < numitems; i++) {
					ItemStack inv = getLootItem(is.getItemDamage(), i, items);
					items[i] = inv;
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
	
	private static ItemStack getLootItem(int damage, int slot, ItemStack[] items){return getLootItem(0, damage, slot, items);}
	private static ItemStack getLootItem(int rerollCount, int damage, int slot, ItemStack[] items)
	{
		boolean reroll = false;
		Bag b = BagHandler.getBag(damage);
		
		//special case for very small loot tables and item repeat prevention
		if(b.getMap().size() <= b.getMaxItems() && LootBags.PREVENTDUPLICATELOOT!=0)
		{
			return b.getSpecificItem(slot);
		}
		
		ItemStack is = b.getRandomItem();
		if(is == null || is.getItem()==null || is.stackSize<= 0)
			reroll = true;
		if(itemAlreadyRolled(is, items, b))
			reroll = true;
		if(reroll && rerollCount<LootBags.MAXREROLLCOUNT)
		{
			rerollCount += items.length;
			return getLootItem(rerollCount, damage, slot, items);
		}
		else if (rerollCount>=LootBags.MAXREROLLCOUNT)
			return null;
		return is;
	}
	
	private static boolean itemAlreadyRolled(ItemStack stack, ItemStack[] items, Bag b)
	{
		if(items == null)
			return false;
		if(b.getItemRepeats()==0)
			return false;

		for(int i = 0; i < items.length; i++)
		{
			if(items[i] != null)
			{
				if(b.getItemRepeats()==1 && stack.isItemEqual(items[i]))
					return true;
				if(b.getItemRepeats()==2 && stack.getItem()==items[i].getItem())
					return true;
			}
		}
		return false;
	}
	
	public ItemStack onItemRightClick(ItemStack is, World world,
			EntityPlayer player) {
		if (!world.isRemote && !player.isSneaking()) {
			if(BagHandler.isBagEmpty(is.getItemDamage()))
				return is;
			LootbagItem.generateInventory(is);
			player.openGui(LootBags.LootBagsInstance, 0, world, 0, 0, 0);
		}

		return is;
	}
	
	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			if(BagHandler.isBagEmpty(is.getItemDamage()))
				return false;
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
							if(ite.isItemValidForSlot(i, stack) && (itstack==null || (LootBags.areItemStacksEqualItem(itstack, stack, true, true) || ite.getStackInSlot(i) == null)))
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
	
	public String getUnlocalizedName(ItemStack stack)
	{
		String base = "name."+LootBags.MODID;
		Bag bag = BagHandler.getBag(stack.getItemDamage());
		if(bag==null)
			return base+"_"+name;//return default unlocalized name if bag doesn't exist
		return base+"_"+bag.getBagName();
	}
	
	@SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
		Bag bag = BagHandler.getBag(stack.getItemDamage());
		if(bag==null)
			return 16777215;
		int[] colors = bag.getBagTextureColor();
		if(renderPass==0)
			return colors[0];
		else
			return colors[1];
    }
	
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
		for(Bag b:BagHandler.getBagList().values())
		{
			if(!b.getSecret() || LootBags.SHOWSECRETBAGS)
				par3List.add(new ItemStack(par1, 1, b.getBagIndex()));
		}
    }

/*	@Override
	public void registerItemVariants(ItemRenderingRegister register) {
		for(Bag b: BagHandler.getBagList().values())
		{
			if(b.getUseAltJson())
				register.reg(this, b.getBagIndex(), b.getBagName());
			else
				register.reg(this, b.getBagIndex(), b.getDefaultName());
		}
	}*/
	
	private static int getNumItems(ItemStack is)
	{
		Bag b = BagHandler.getBag(is.getItemDamage());
		
		int min = b.getMinItems();
		int max = b.getMaxItems();
		
		if(min == max)
			return min;
		
		return LootBags.getRandom().nextInt(max-min+1)+min;
	}
	
	@Override
	public boolean getShareTag() {
		return true;
	}
	
	public int getEntityLifespan(ItemStack itemStack, World world)
	{	
		if(!BagHandler.isBagEmpty(itemStack.getItemDamage()))
			return super.getEntityLifespan(itemStack, world);
		else
			return 1;
	}
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/