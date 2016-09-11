package mal.lootbags.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.item.LootbagItem;
import mal.lootbags.network.LootbagWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LootbagContainer extends Container{

	public LootbagWrapper wrapper;
	public InventoryPlayer player;
	private int islot;
	
	//Meesa bypassa alla de clicky privat fieldsa
    private int dragModea = -1;
    private int dragEventa;
    private final Set<Slot> dragSlotsa = Sets.<Slot>newHashSet();
	
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
            	if(LootBags.areItemStacksEqualItem(player.getStackInSlot(j+i*9+9), wrapper.getStack(), true, false))
            		this.addSlotToContainer(new FixedSlot(player, j+ i*9+9, 8 + j * 18, 46 + i*18));
            	else
            		this.addSlotToContainer(new Slot(player, j + i * 9+9, 8 + j * 18, 46 + i * 18));
            }
        }

        //hotbar, so 45-53
        for (int i = 0; i < 9; ++i)
        {
        	if(LootBags.areItemStacksEqualItem(player.getStackInSlot(i), wrapper.getStack(), true, false))
        		this.addSlotToContainer(new FixedSlot(player, i, 8 + i * 18, 103));
        	else
        		this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 103));
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {
		if(player.getItemStack()!=null)
			return true;//LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false);
		return wrapper.isUseableByPlayer(p_75145_1_); //&& LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false);
	}

	public void detectAndSendChanges()
    {
		super.detectAndSendChanges();
		
		if(LootBags.areItemStacksEqualItem(player.mainInventory[islot], wrapper.getStack(), true, false))
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
			if(LootBags.areItemStacksEqualItem(player.inventory.mainInventory[islot], wrapper.getStack(), true, false))
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
			else
			{
				//player.dropPlayerItemWithRandomChoice(((Slot)this.inventorySlots.get(0)).getStack(), false);
			}
		}
		super.onContainerClosed(player);
	}
	
	/**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
    {
    	ItemStack var3 = null;
    	Slot var4 = null;
    	if(this.inventorySlots.get(slot) instanceof FixedSlot)
    		var4 = (FixedSlot)this.inventorySlots.get(slot);
    	else
    		var4 = (Slot)this.inventorySlots.get(slot);

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
    
    /*@Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer eplayer)
    {
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = eplayer.inventory;
        int i1;
        ItemStack itemstack3;
        
        if(!eplayer.worldObj.isRemote && !LootBags.areItemStacksEqualItem(eplayer.inventory.mainInventory[islot], wrapper.getStack(), true, false))
        {
        	eplayer.closeScreen();
        	//LootbagsUtil.LogInfo("Missing Lootbag");
        	return null;
        }

        if (clickTypeIn == ClickType.QUICK_CRAFT)
        {
            int l = this.dragEvent;
            this.dragEvent = getDragEvent(dragType);

            if ((l != 1 || this.dragEvent != 2) && l != this.dragEvent)
            {
                this.resetDrag();
            }
            else if (inventoryplayer.getItemStack() == null)
            {
                this.resetDrag();
            }
            else if (this.dragEvent == 0)
            {
                this.dragMode = extractDragMode(dragType);

                if (isValidDragMode(this.dragMode, eplayer))
                {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                }
                else
                {
                    this.resetDrag();
                }
            }
            else if (this.dragEvent == 1)
            {
                Slot slot = null;
                if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	slot = (FixedSlot)this.inventorySlots.get(slotId);
                else
                	slot = (Slot)this.inventorySlots.get(slotId);

                if (slot != null && canAddItemToSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.dragSlots.size() && this.canDragIntoSlot(slot))
                {
                    this.dragSlots.add(slot);
                }
            }
            else if (this.dragEvent == 2)
            {
                if (!this.dragSlots.isEmpty())
                {
                    itemstack3 = inventoryplayer.getItemStack().copy();
                    i1 = inventoryplayer.getItemStack().stackSize;
                    Iterator<Slot> iterator = this.dragSlots.iterator();

                    while (iterator.hasNext())
                    {
                        Slot slot1 = iterator.next();

                        if (slot1 != null && canAddItemToSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack1 = itemstack3.copy();
                            int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                            computeStackSize(this.dragSlots, this.dragMode, itemstack1, j1);

                            if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                            {
                                itemstack1.stackSize = itemstack1.getMaxStackSize();
                            }

                            if (itemstack1.stackSize > slot1.getSlotStackLimit())
                            {
                                itemstack1.stackSize = slot1.getSlotStackLimit();
                            }

                            i1 -= itemstack1.stackSize - j1;
                            slot1.putStack(itemstack1);
                        }
                    }

                    itemstack3.stackSize = i1;

                    if (itemstack3.stackSize <= 0)
                    {
                        itemstack3 = null;
                    }

                    inventoryplayer.setItemStack(itemstack3);
                }

                this.resetDrag();
            }
            else
            {
                this.resetDrag();
            }
        }
        else if (this.dragEvent != 0)
        {
            this.resetDrag();
        }
        else
        {
            Slot slot2;
            int l1;
            ItemStack itemstack5;

            if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
            {
                if (slotId == -999)
                {
                    if (inventoryplayer.getItemStack() != null && slotId == -999)
                    {
                        if (dragType == 0)
                        {
                            eplayer.dropItem(inventoryplayer.getItemStack(), true);
                            inventoryplayer.setItemStack((ItemStack)null);
                        }

                        if (dragType == 1)
                        {
                            eplayer.dropItem(inventoryplayer.getItemStack().splitStack(1), true);

                            if (inventoryplayer.getItemStack().stackSize == 0)
                            {
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                }
                else if (clickTypeIn == ClickType.QUICK_MOVE)
                {
                    if (slotId < 0)
                    {
                        return null;
                    }

                    if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                    	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                    else
                    	slot2 = (Slot)this.inventorySlots.get(slotId);

                    if (slot2 != null && slot2.canTakeStack(eplayer))
                    {
                        itemstack3 = this.transferStackInSlot(eplayer, slotId);

                        if (itemstack3 != null)
                        {
                            Item item = itemstack3.getItem();
                            itemstack = itemstack3.copy();

                            if (slot2.getStack() != null && slot2.getStack().getItem() == item)
                            {
                                this.retrySlotClick(slotId, dragType, true, eplayer);
                            }
                        }
                    }
                }
                else
                {
                    if (slotId < 0)
                    {
                        return null;
                    }

                    if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                    	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                    else
                    	slot2 = (Slot)this.inventorySlots.get(slotId);
                    
                    if (slot2 != null)
                    {
                        itemstack3 = slot2.getStack();
                        ItemStack itemstack4 = inventoryplayer.getItemStack();

                        if (itemstack3 != null)
                        {
                            itemstack = itemstack3.copy();
                        }

                        if (itemstack3 == null)
                        {
                            if (itemstack4 != null && slot2.isItemValid(itemstack4))
                            {
                                l1 = dragType == 0 ? itemstack4.stackSize : 1;

                                if (l1 > slot2.getSlotStackLimit())
                                {
                                    l1 = slot2.getSlotStackLimit();
                                }

                                if (itemstack4.stackSize >= l1)
                                {
                                    slot2.putStack(itemstack4.splitStack(l1));
                                }

                                if (itemstack4.stackSize == 0)
                                {
                                    inventoryplayer.setItemStack((ItemStack)null);
                                }
                            }
                        }
                        else if (slot2.canTakeStack(eplayer))
                        {
                            if (itemstack4 == null)
                            {
                                l1 = dragType == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
                                itemstack5 = slot2.decrStackSize(l1);
                                inventoryplayer.setItemStack(itemstack5);

                                if (itemstack3.stackSize == 0)
                                {
                                    slot2.putStack((ItemStack)null);
                                }

                                slot2.onPickupFromSlot(eplayer, inventoryplayer.getItemStack());
                            }
                            else if (slot2.isItemValid(itemstack4))
                            {
                                if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                                {
                                    l1 = dragType == 0 ? itemstack4.stackSize : 1;

                                    if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
                                    {
                                        l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
                                    }

                                    if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize)
                                    {
                                        l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
                                    }

                                    itemstack4.splitStack(l1);

                                    if (itemstack4.stackSize == 0)
                                    {
                                        inventoryplayer.setItemStack((ItemStack)null);
                                    }

                                    itemstack3.stackSize += l1;
                                }
                                else if (itemstack4.stackSize <= slot2.getSlotStackLimit())
                                {
                                    slot2.putStack(itemstack4);
                                    inventoryplayer.setItemStack(itemstack3);
                                }
                            }
                            else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                            {
                                l1 = itemstack3.stackSize;

                                if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize())
                                {
                                    itemstack4.stackSize += l1;
                                    itemstack3 = slot2.decrStackSize(l1);

                                    if (itemstack3.stackSize == 0)
                                    {
                                        slot2.putStack((ItemStack)null);
                                    }

                                    slot2.onPickupFromSlot(eplayer, inventoryplayer.getItemStack());
                                }
                            }
                        }

                        slot2.onSlotChanged();
                    }
                }
            }
            else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9)
            {
            	if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                else
                	slot2 = (Slot)this.inventorySlots.get(slotId);
            	
            	Slot st = null;
            	if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	st = (FixedSlot)this.inventorySlots.get(dragType);
                else
                	st = (Slot)this.inventorySlots.get(dragType);

                itemstack3 = inventoryplayer.getStackInSlot(dragType);
                if (slot2.canTakeStack(eplayer) && ((itemstack3 != null)?(!(itemstack3.getItem() instanceof LootbagItem)):(true)))
                {
                    boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
                    l1 = -1;

                    if (!flag)
                    {
                        l1 = inventoryplayer.getFirstEmptyStack();
                        flag |= l1 > -1;
                    }

                    if (slot2.getHasStack() && flag)
                    {
                        itemstack5 = slot2.getStack();
                        inventoryplayer.setInventorySlotContents(dragType, itemstack5.copy());

                        if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null)
                        {
                            if (l1 > -1)
                            {
                                inventoryplayer.addItemStackToInventory(itemstack3);
                                slot2.decrStackSize(itemstack5.stackSize);
                                slot2.putStack((ItemStack)null);
                                slot2.onPickupFromSlot(eplayer, itemstack5);
                            }
                        }
                        else
                        {
                            slot2.decrStackSize(itemstack5.stackSize);
                            slot2.putStack(itemstack3);
                            slot2.onPickupFromSlot(eplayer, itemstack5);
                        }
                    }
                    else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3))
                    {
                        inventoryplayer.setInventorySlotContents(dragType, (ItemStack)null);
                        slot2.putStack(itemstack3);
                    }
                }
            }
            else if (clickTypeIn == ClickType.CLONE && eplayer.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && slotId >= 0)
            {
            	if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                else
                	slot2 = (Slot)this.inventorySlots.get(slotId);

                if (slot2 != null && slot2.getHasStack())
                {
                    itemstack3 = slot2.getStack().copy();
                    itemstack3.stackSize = itemstack3.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack3);
                }
            }
            else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack() == null && slotId >= 0)
            {
            	if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                else
                	slot2 = (Slot)this.inventorySlots.get(slotId);

                if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(eplayer))
                {
                    itemstack3 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().stackSize);
                    slot2.onPickupFromSlot(eplayer, itemstack3);
                    eplayer.dropItem(itemstack3, true);
                }
            }
            else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
            {
            	if(this.inventorySlots.get(slotId) instanceof FixedSlot)
                	slot2 = (FixedSlot)this.inventorySlots.get(slotId);
                else
                	slot2 = (Slot)this.inventorySlots.get(slotId);
                itemstack3 = inventoryplayer.getItemStack();

                if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(eplayer)))
                {
                    i1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                    l1 = dragType == 0 ? 1 : -1;

                    for (int i2 = 0; i2 < 2; ++i2)
                    {
                        for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1)
                        {
                            Slot slot3 = null;
                            if(this.inventorySlots.get(j2) instanceof FixedSlot)
                            	slot3 = (FixedSlot)this.inventorySlots.get(j2);
                            else
                            	slot3 = (Slot)this.inventorySlots.get(j2);

                            if (slot3.getHasStack() && canAddItemToSlot(slot3, itemstack3, true) && slot3.canTakeStack(eplayer) && this.canMergeSlot(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize()))
                            {
                                int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
                                ItemStack itemstack2 = slot3.decrStackSize(k1);
                                itemstack3.stackSize += k1;

                                if (itemstack2.stackSize <= 0)
                                {
                                    slot3.putStack((ItemStack)null);
                                }

                                slot3.onPickupFromSlot(eplayer, itemstack2);
                            }
                        }
                    }
                }

                this.detectAndSendChanges();
            }
        }

		if(!eplayer.worldObj.isRemote)
		{
			if(LootBags.areItemStacksEqualItem(eplayer.inventory.mainInventory[islot], wrapper.getStack(), true, false))
			{
				if(LootbagItem.checkInventory(wrapper.getStack()))
				{
					eplayer.inventory.mainInventory[islot] = null;
				}
				else
				{
					eplayer.inventory.mainInventory[islot] = wrapper.getStack();
				}
			}
		}
        return itemstack;
    }*/
    
    @Override
    @Nullable
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = player.inventory;
        
/*        if(!player.worldObj.isRemote && !LootBags.areItemStacksEqualItem(player.inventory.mainInventory[islot], wrapper.getStack(), true, false))
        {
        	player.closeScreen();
        	//LootbagsUtil.LogInfo("Missing Lootbag");
        	return null;
        }*/

        if (clickTypeIn == ClickType.QUICK_CRAFT)
        {
            int i = this.dragEventa;
            this.dragEventa = getDragEvent(dragType);

            if ((i != 1 || this.dragEventa != 2) && i != this.dragEventa)
            {
                this.resetDrag();
            }
            else if (inventoryplayer.getItemStack() == null)
            {
                this.resetDrag();
            }
            else if (this.dragEventa == 0)
            {
                this.dragModea = extractDragMode(dragType);

                if (isValidDragMode(this.dragModea, player))
                {
                    this.dragEventa = 1;
                    this.dragSlotsa.clear();
                }
                else
                {
                    this.resetDrag();
                }
            }
            else if (this.dragEventa == 1)
            {
                Slot slot = (Slot)this.inventorySlots.get(slotId);

                if (slot != null && canAddItemToSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.dragSlotsa.size() && this.canDragIntoSlot(slot))
                {
                    this.dragSlotsa.add(slot);
                }
            }
            else if (this.dragEventa == 2)
            {
                if (!this.dragSlotsa.isEmpty())
                {
                    ItemStack itemstack3 = inventoryplayer.getItemStack().copy();
                    int j = inventoryplayer.getItemStack().stackSize;

                    for (Slot slot1 : this.dragSlotsa)
                    {
                        if (slot1 != null && canAddItemToSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.dragSlotsa.size() && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack1 = itemstack3.copy();
                            int k = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                            computeStackSize(this.dragSlotsa, this.dragModea, itemstack1, k);

                            if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                            {
                                itemstack1.stackSize = itemstack1.getMaxStackSize();
                            }

                            if (itemstack1.stackSize > slot1.getItemStackLimit(itemstack1))
                            {
                                itemstack1.stackSize = slot1.getItemStackLimit(itemstack1);
                            }

                            j -= itemstack1.stackSize - k;
                            slot1.putStack(itemstack1);
                        }
                    }

                    itemstack3.stackSize = j;

                    if (itemstack3.stackSize <= 0)
                    {
                        itemstack3 = null;
                    }

                    inventoryplayer.setItemStack(itemstack3);
                }

                this.resetDrag();
            }
            else
            {
                this.resetDrag();
            }
        }
        else if (this.dragEventa != 0)
        {
            this.resetDrag();
        }
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
        {
            if (slotId == -999)
            {
                if (inventoryplayer.getItemStack() != null)
                {
                    if (dragType == 0)
                    {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack((ItemStack)null);
                    }

                    if (dragType == 1)
                    {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);

                        if (inventoryplayer.getItemStack().stackSize == 0)
                        {
                            inventoryplayer.setItemStack((ItemStack)null);
                        }
                    }
                }
            }
            else if (clickTypeIn == ClickType.QUICK_MOVE)
            {
                if (slotId < 0)
                {
                    return null;
                }

                Slot slot6 = (Slot)this.inventorySlots.get(slotId);

                if (slot6 != null && slot6.canTakeStack(player))
                {
                    ItemStack itemstack8 = slot6.getStack();

                    if (itemstack8 != null && itemstack8.stackSize <= 0)
                    {
                        itemstack = itemstack8.copy();
                        slot6.putStack((ItemStack)null);
                    }

                    ItemStack itemstack11 = this.transferStackInSlot(player, slotId);

                    if (itemstack11 != null)
                    {
                        Item item = itemstack11.getItem();
                        itemstack = itemstack11.copy();

                        if (slot6.getStack() != null && slot6.getStack().getItem() == item)
                        {
                            this.retrySlotClick(slotId, dragType, true, player);
                        }
                    }
                }
            }
            else
            {
                if (slotId < 0)
                {
                    return null;
                }

                Slot slot7 = (Slot)this.inventorySlots.get(slotId);

                if (slot7 != null)
                {
                    ItemStack itemstack9 = slot7.getStack();
                    ItemStack itemstack12 = inventoryplayer.getItemStack();

                    if (itemstack9 != null)
                    {
                        itemstack = itemstack9.copy();
                    }

                    if (itemstack9 == null)
                    {
                        if (itemstack12 != null && slot7.isItemValid(itemstack12))
                        {
                            int l2 = dragType == 0 ? itemstack12.stackSize : 1;

                            if (l2 > slot7.getItemStackLimit(itemstack12))
                            {
                                l2 = slot7.getItemStackLimit(itemstack12);
                            }

                            slot7.putStack(itemstack12.splitStack(l2));

                            if (itemstack12.stackSize == 0)
                            {
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                    else if (slot7.canTakeStack(player))
                    {
                        if (itemstack12 == null)
                        {
                            if (itemstack9.stackSize > 0)
                            {
                                int k2 = dragType == 0 ? itemstack9.stackSize : (itemstack9.stackSize + 1) / 2;
                                inventoryplayer.setItemStack(slot7.decrStackSize(k2));

                                if (itemstack9.stackSize <= 0)
                                {
                                    slot7.putStack((ItemStack)null);
                                }

                                slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
                            }
                            else
                            {
                                slot7.putStack((ItemStack)null);
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                        else if (slot7.isItemValid(itemstack12))
                        {
                            if (itemstack9.getItem() == itemstack12.getItem() && itemstack9.getMetadata() == itemstack12.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12))
                            {
                                int j2 = dragType == 0 ? itemstack12.stackSize : 1;

                                if (j2 > slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize)
                                {
                                    j2 = slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize;
                                }

                                if (j2 > itemstack12.getMaxStackSize() - itemstack9.stackSize)
                                {
                                    j2 = itemstack12.getMaxStackSize() - itemstack9.stackSize;
                                }

                                itemstack12.splitStack(j2);

                                if (itemstack12.stackSize == 0)
                                {
                                    inventoryplayer.setItemStack((ItemStack)null);
                                }

                                itemstack9.stackSize += j2;
                            }
                            else if (itemstack12.stackSize <= slot7.getItemStackLimit(itemstack12))
                            {
                                slot7.putStack(itemstack12);
                                inventoryplayer.setItemStack(itemstack9);
                            }
                        }
                        else if (itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && (!itemstack9.getHasSubtypes() || itemstack9.getMetadata() == itemstack12.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12))
                        {
                            int i2 = itemstack9.stackSize;

                            if (i2 > 0 && i2 + itemstack12.stackSize <= itemstack12.getMaxStackSize())
                            {
                                itemstack12.stackSize += i2;
                                itemstack9 = slot7.decrStackSize(i2);

                                if (itemstack9.stackSize == 0)
                                {
                                    slot7.putStack((ItemStack)null);
                                }

                                slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    slot7.onSlotChanged();
                }
            }
        }
        else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9)
        {
            Slot slot5 = (Slot)this.inventorySlots.get(slotId);
            ItemStack itemstack7 = inventoryplayer.getStackInSlot(dragType);

            if (itemstack7 != null && itemstack7.stackSize <= 0)
            {
                itemstack7 = null;
                inventoryplayer.setInventorySlotContents(dragType, (ItemStack)null);
            }

            ItemStack itemstack10 = slot5.getStack();

            if (itemstack7 != null || itemstack10 != null)
            {
                if (itemstack7 == null)
                {
                    if (slot5.canTakeStack(player))
                    {
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot5.putStack((ItemStack)null);
                        slot5.onPickupFromSlot(player, itemstack10);
                    }
                }
                else if (itemstack10 == null)
                {
                    if (slot5.isItemValid(itemstack7))
                    {
                        int k1 = slot5.getItemStackLimit(itemstack7);

                        if (itemstack7.stackSize > k1)
                        {
                            slot5.putStack(itemstack7.splitStack(k1));
                        }
                        else
                        {
                            slot5.putStack(itemstack7);
                            inventoryplayer.setInventorySlotContents(dragType, (ItemStack)null);
                        }
                    }
                }
                else if (slot5.canTakeStack(player) && slot5.isItemValid(itemstack7))
                {
                    int l1 = slot5.getItemStackLimit(itemstack7);

                    if (itemstack7.stackSize > l1)
                    {
                        slot5.putStack(itemstack7.splitStack(l1));
                        slot5.onPickupFromSlot(player, itemstack10);

                        if (!inventoryplayer.addItemStackToInventory(itemstack10))
                        {
                            player.dropItem(itemstack10, true);
                        }
                    }
                    else
                    {
                        slot5.putStack(itemstack7);
                        inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                        slot5.onPickupFromSlot(player, itemstack10);
                    }
                }
            }
        }
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && slotId >= 0)
        {
            Slot slot4 = (Slot)this.inventorySlots.get(slotId);

            if (slot4 != null && slot4.getHasStack())
            {
                if (slot4.getStack().stackSize > 0)
                {
                    ItemStack itemstack6 = slot4.getStack().copy();
                    itemstack6.stackSize = itemstack6.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack6);
                }
                else
                {
                    slot4.putStack((ItemStack)null);
                }
            }
        }
        else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack() == null && slotId >= 0)
        {
            Slot slot3 = (Slot)this.inventorySlots.get(slotId);

            if (slot3 != null && slot3.getHasStack() && slot3.canTakeStack(player))
            {
                ItemStack itemstack5 = slot3.decrStackSize(dragType == 0 ? 1 : slot3.getStack().stackSize);
                slot3.onPickupFromSlot(player, itemstack5);
                player.dropItem(itemstack5, true);
            }
        }
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
        {
            Slot slot2 = (Slot)this.inventorySlots.get(slotId);
            ItemStack itemstack4 = inventoryplayer.getItemStack();

            if (itemstack4 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player)))
            {
                int i1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                int j1 = dragType == 0 ? 1 : -1;

                for (int i3 = 0; i3 < 2; ++i3)
                {
                    for (int j3 = i1; j3 >= 0 && j3 < this.inventorySlots.size() && itemstack4.stackSize < itemstack4.getMaxStackSize(); j3 += j1)
                    {
                        Slot slot8 = (Slot)this.inventorySlots.get(j3);

                        if (slot8.getHasStack() && canAddItemToSlot(slot8, itemstack4, true) && slot8.canTakeStack(player) && this.canMergeSlot(itemstack4, slot8) && (i3 != 0 || slot8.getStack().stackSize != slot8.getStack().getMaxStackSize()))
                        {
                            int l = Math.min(itemstack4.getMaxStackSize() - itemstack4.stackSize, slot8.getStack().stackSize);
                            ItemStack itemstack2 = slot8.decrStackSize(l);
                            itemstack4.stackSize += l;

                            if (itemstack2.stackSize <= 0)
                            {
                                slot8.putStack((ItemStack)null);
                            }

                            slot8.onPickupFromSlot(player, itemstack2);
                        }
                    }
                }
            }

            this.detectAndSendChanges();
        }

        return itemstack;
    }
    
    /**
     * Reset the drag fields
     */
    @Override
    protected void resetDrag()
    {
        this.dragEventa = 0;
        this.dragSlotsa.clear();
    }
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/