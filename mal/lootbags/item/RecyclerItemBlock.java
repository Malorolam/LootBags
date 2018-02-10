package mal.lootbags.item;

import javax.annotation.Nullable;

import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

public class RecyclerItemBlock extends ItemBlock{

	public RecyclerItemBlock(Block block) {
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

                if (tileentity != null && tileentity instanceof TileEntityRecycler)
                {
                    if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock()))
                    {
                        return false;
                    }
                    
                    int count = nbttagcompound.getInteger("lootbagCount");
                    int value = nbttagcompound.getInteger("totalValue");
                    ((TileEntityRecycler)tileentity).setData(count, value);
                }
            }

            return false;
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