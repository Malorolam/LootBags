package mal.lootbags.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.rendering.IItemVarientDetails;
import mal.lootbags.rendering.ItemRenderingRegister;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * New Lootbag Item class, to use the updated config and bag creation
 */
public class LootbagItem extends Item implements IItemVarientDetails{
	
	private final String name = "itemlootbag";
	
	public LootbagItem()
	{
		this.setUnlocalizedName(LootBags.MODID + "_" + name);
		this.setRegistryName(LootBags.MODID, name);
		setCreativeTab(CreativeTabs.MISC);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}
	
	public String getName()
	{
		return name;
	}
	
    @Override
	public String getItemStackDisplayName(ItemStack stack)
    {
        String s = ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
        Bag b = BagHandler.getBag(stack.getItemDamage());
        if(b!= null)
        	return b.getBagNameColor()+s;
        else
        	return s;
    }
    
    @Override
	public void addInformation(ItemStack is, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
    	Bag b = BagHandler.getBag(is.getItemDamage());
    	if(b==null)
    		return;
    	
    	if(b.isBagEmpty())
    	{
    		list.add(TextFormatting.RED + "Bag Disabled - Loot Table is empty.");
    		list.add(TextFormatting.RED + "This is not good.");
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

	public static ArrayList<String> parseTextCommand(String text, Bag currentBag)
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
				sret = I18n.translateToLocal("name."+LootBags.MODID+"_"+currentBag.getBagName()+".name") + " drop chances: Monster: " + currentBag.getMonsterDropChance()
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
			NBTTagCompound item = items.getCompoundTagAt(i);
			int slot = item.getInteger("Slot");

			if (slot >= 0 && slot < inventory.length) {
				ItemStack ii = new ItemStack(item);
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
				int numitems = getNumItems(is);
				
				ItemStack[] items = new ItemStack[BagHandler.HARDMAX];
				
				NBTTagCompound nbt = new NBTTagCompound();
				NBTTagList nbtinventory = new NBTTagList();

				for (int i = 0; i < BagHandler.HARDMAX; i++) {
					NBTTagCompound var4 = new NBTTagCompound();
					var4.setInteger("Slot", i);
					
					if(i<numitems)
					{
						ItemStack inv = getLootItem(is.getItemDamage(), i, items);
						items[i] = inv;
						if (inv != null && inv.getCount()>0)
							inv.writeToNBT(var4);
					}
					else
					{
						items[i] = ItemStack.EMPTY;
						
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
		
		if(b == null)
		{
			LootbagsUtil.LogError("Bag at ID: " + damage + " doesn't exist!");
			return null;
		}
		
		
		//special case for very small loot tables and item repeat prevention
		if(b.getItemRepeats()==3)
		{
			return b.getSpecificItem(slot);
		}
		
		ItemStack is = b.getRandomItem();
		if(is == null || is.getItem()==null || is.getCount()<= 0 || is.isEmpty())
			reroll = true;
		if(itemAlreadyRolled(is, items, b))
			reroll = true;
		if(reroll && rerollCount<LootBags.MAXREROLLCOUNT)
		{
			rerollCount += items.length;
			return getLootItem(rerollCount, damage, slot, items);
		}
		else if (rerollCount>=LootBags.MAXREROLLCOUNT)
			return ItemStack.EMPTY;
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
			if(items[i] != null && !items[i].isEmpty())
			{
				if(b.getItemRepeats()==1 && stack.isItemEqual(items[i]))
					return true;
				if(b.getItemRepeats()==2 && stack.getItem()==items[i].getItem())
					return true;
			}
		}
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
		ItemStack is = player.getHeldItem(handIn);
		if (!world.isRemote && !player.isSneaking()) {
			if(BagHandler.isBagEmpty(is.getItemDamage()))
				return new ActionResult(EnumActionResult.PASS, is);
			LootbagItem.generateInventory(is);
			player.openGui(LootBags.LootBagsInstance, 0, world, 0, 0, 0);
		}

		return new ActionResult(EnumActionResult.PASS, is);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			ItemStack is = player.getHeldItem(hand);
			
			if(BagHandler.isBagEmpty(is.getItemDamage()))
				return EnumActionResult.FAIL;
			if(!player.isSneaking())
				return EnumActionResult.FAIL;

			TileEntity te = world.getTileEntity(pos);
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
								else if(itstack.getCount()+stack.getCount()<=itstack.getMaxStackSize())
								{
									itstack.grow(stack.getCount());
									iss[j] = null;
									break;
								}
								else if(itstack.getCount()<itstack.getMaxStackSize())
								{
									int diff =  itstack.getMaxStackSize()-itstack.getCount();
									ite.getStackInSlot(i).setCount(ite.getStackInSlot(i).getMaxStackSize());
									stack.shrink(diff);
									if(stack.getCount()<=0)
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
				//return EnumActionResult.PASS;
			}
		}
		return EnumActionResult.SUCCESS;
	}
	
    @Override
	public void onUpdate(ItemStack is, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
		if(entityIn instanceof EntityPlayer && LootbagItem.checkInventory(is))
		{
			EntityPlayer player = (EntityPlayer)entityIn;
			if(LootBags.areItemStacksEqualItem(is, player.getHeldItemOffhand(), true, true))
				player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, null);
			else
				player.inventory.removeStackFromSlot(itemSlot);
		}
    }
    
/*    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
    	if(!world.isRemote && !player.isSneaking() && world.getTileEntity(pos)==null)
    	{
			if(BagHandler.isBagEmpty(stack.getItemDamage()))
				return EnumActionResult.PASS;
			LootbagItem.generateInventory(stack);
			player.openGui(LootBags.LootBagsInstance, 0, world, 0, 0, 0);
    		return EnumActionResult.FAIL;
    	}
        return EnumActionResult.PASS;
    }*/
    
	/**
	 * Returns true if the stack should be removed
	 * @param is
	 * @return
	 */
	public static boolean checkInventory(ItemStack is)
	{
		if(is.getTagCompound()==null)
			return false;
		boolean gen = is.getTagCompound().getBoolean("generated");
		if(gen)
		{
			ItemStack[] stack = getInventory(is);
			if(stack==null)
				return true;
			boolean b = true;
			for(int i = 0; i < stack.length; i++)
			{
				if((stack[i]!=null && stack[i] != ItemStack.EMPTY) && stack[i].getCount()>0)
					b=false;
			}
			return b;
		}
		return false;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		String base = "name."+LootBags.MODID;
		Bag bag = BagHandler.getBag(stack.getItemDamage());
		if(bag==null)
			return base+"_"+name;//return default unlocalized name if bag doesn't exist
		return base+"_"+bag.getBagName();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
		for(Bag b:BagHandler.getBagList().values())
		{
			if(!b.getSecret() || LootBags.SHOWSECRETBAGS)
				items.add(new ItemStack(this, 1, b.getBagIndex()));
		}
    }

	@Override
	public void registerItemVariants(ItemRenderingRegister register) {
		for(Bag b: BagHandler.getBagList().values())
		{
			if(b.getUseAltJson())
				register.reg(this, b.getBagIndex(), b.getBagName());
			else
				register.reg(this, b.getBagIndex(), b.getDefaultName());
		}
	}
	
	private static int getNumItems(ItemStack is)
	{
		Bag b = BagHandler.getBag(is.getItemDamage());
		
		if(b == null)
		{
			LootbagsUtil.LogError("Bag at ID: " + is.getItemDamage() + " doesn't exist!");
			return 0;
		}
		
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
	
	@Override
	public int getEntityLifespan(ItemStack itemStack, World world)
	{	
		if(!BagHandler.isBagEmpty(itemStack.getItemDamage()))
			return super.getEntityLifespan(itemStack, world);
		else
			return 1;
	}
}
/*******************************************************************************
 * Copyright (c) 2017 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/