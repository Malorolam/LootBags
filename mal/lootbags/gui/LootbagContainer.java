package mal.lootbags.gui;

import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LootbagContainer extends Container{

	public LootbagWrapper wrapper;
	public InventoryPlayer player;
	private int islot;
	
	public LootbagContainer(InventoryPlayer player, LootbagWrapper wrap)
	{
		wrapper=wrap;
		this.player=player;
		islot = player.currentItem;
		
		for(int i = 0; i < wrap.getSizeInventory(); i++)
		{
			this.addSlotToContainer(new LootbagSlot(wrapper,i,44+i*18, 15));
		}
		
		//main inventory
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
            	if(areItemStacksEqualItem(player.getStackInSlot(j+i*9+9), wrapper.getStack(), true, false))
            		this.addSlotToContainer(new FixedSlot(player, j+ i*9+9, 8 + i * 18, 46 + i*18));
            	else
            		this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 46 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
        	if(areItemStacksEqualItem(player.getStackInSlot(i), wrapper.getStack(), true, false))
        		this.addSlotToContainer(new FixedSlot(player, i, 8 + i * 18, 103));
        	else
        		this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 103));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		return true;
	}

	public void detectAndSendChanges()
    {
		super.detectAndSendChanges();
		
		if(areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false))
		{
			if(LootbagItem.checkInventory(wrapper.getStack()))
			{
				player.mainInventory[islot] = null;
			}
			else
			{
				player.mainInventory[islot] = wrapper.getStack();
			}
		}
    }
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if(!player.worldObj.isRemote)
		{
			if(areItemStacksEqualItem(player.inventory.mainInventory[islot], wrapper.getStack(), true, false))
			{
				if(LootbagItem.checkInventory(wrapper.getStack()))
				{
					player.inventory.mainInventory[islot] = null;
				}
				else
				{
					player.inventory.mainInventory[islot] = wrapper.getStack();
				}
			}
			/*else
			{
				player.dropPlayerItemWithRandomChoice(((Slot)this.inventorySlots.get(0)).getStack(), false);
			}*/
		}
		super.onContainerClosed(player);
	}
	
	private boolean areItemStacksEqualItem(ItemStack is1, ItemStack is2, boolean considerDamage, boolean considerNBT)
    {
    	if(is1==null ^ is2==null)
    		return false;
    	if(Item.getIdFromItem(is1.getItem()) != Item.getIdFromItem(is2.getItem()))
    		return false;
    	if(considerDamage && is1.getItemDamage() != is2.getItemDamage())
    		return false;
    	if(considerNBT && !ItemStack.areItemStackTagsEqual(is1, is2))
    		return false;
    	return true;
    }
	
	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
	//TODO: Make it actually do stuff instead of being lazy
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
    {
    	ItemStack var3 = null;
        Slot var4 = (Slot)this.inventorySlots.get(slot);

        if (var4 != null && var4.getHasStack())
        {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(slot>=0 && slot <5)//inventory
            {
            	if (!this.mergeItemStack(var5, 5, 41, true))
                {
                    return null;
                }

                var4.onSlotChange(var5, var3);

            }
            
            if (var5.stackSize == 0)
            {
                var4.putStack((ItemStack)null);
            }
            else
            {
                var4.onSlotChanged();
            }

            if (var5.stackSize == var3.stackSize)
            {
                return null;
            }

            var4.onPickupFromSlot(par1EntityPlayer, var5);
        }
        
        return var3;
    }
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/