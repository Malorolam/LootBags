package mal.lootbags.item;

import javax.annotation.Nullable;

import mal.lootbags.LootbagsUtil;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StorageItemBlock extends ItemBlock{

	public StorageItemBlock(Block block) {
		super(block);
	}

	public static boolean setTileEntityNBT(World worldIn, @Nullable EntityPlayer player, BlockPos pos, ItemStack stackIn)
    {
        MinecraftServer minecraftserver = worldIn.getMinecraftServer();

        if (minecraftserver == null)
        {
            return false;
        }
        else
        {
            NBTTagCompound nbttagcompound = stackIn.getTagCompound();

            if (nbttagcompound != null)
            {
                TileEntity tileentity = worldIn.getTileEntity(pos);

                if (tileentity != null && tileentity instanceof TileEntityStorage)
                {
                    if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock()))
                    {
                        return false;
                    }
                    
                    int ID = nbttagcompound.getInteger("outputID");
                    int value = nbttagcompound.getInteger("stored_value");
                    int index = nbttagcompound.getInteger("outputindex");
                    ((TileEntityStorage)tileentity).setDataClient(value, ID, index);
                }
            }

            return false;
        }
    }

    @Override
    public void addInformation(ItemStack is, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
	    if(is.getTagCompound() != null)
        {
            list.add("Stored Value: " + LootbagsUtil.formatSciNot(is.getTagCompound().getInteger("stored_value")));
        }
        if(Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54))
        {
            list.add("This block is capable of storing most Lootbags and convert between configured types.  It will save its contents when broken.");
        }
        else
        {
            list.add("Press Shift for more info.");
        }

    }

    @Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        if (!world.setBlockState(pos, newState, 11)) return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block)
        {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof EntityPlayerMP)
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
        }

        return true;
    }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/