package mal.lootbags.blocks;

import javax.annotation.Nullable;

import mal.lootbags.LootBags;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStorage extends BlockContainer{

	private final String name = "loot_storage";
	
	public BlockStorage() {
		super(Material.ROCK);
		this.setUnlocalizedName(LootBags.MODID + "_" + name);
		this.setRegistryName(LootBags.MODID, name);
		this.setHardness(1.5f);
		this.setResistance(20f);
		this.setCreativeTab(CreativeTabs.DECORATIONS);
	}

	public String getName()
	{
		return name;
	}
	
    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityStorage();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
	    if (worldIn.isRemote)
	    {
	        return true;
	    }
	    else
	    {
	    	TileEntity var10 = worldIn.getTileEntity(pos);
	    	if(!(var10 instanceof TileEntityStorage))
	    		return false;
	    	else
	    		((TileEntityStorage)var10).activate(worldIn, pos, playerIn);
	    	return true;
	    }
	}
	
    /**
     * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
     * Block.removedByPlayer
     */
    @Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        if (te instanceof TileEntityStorage)
        {
            player.addStat(StatList.getBlockStats(this));
            player.addExhaustion(0.005F);

            if (worldIn.isRemote)
            {
                return;
            }

            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            Item item = this.getItemDropped(state, worldIn.rand, i);

            if (item == Items.AIR)
            {
                return;
            }

            ItemStack itemstack = new ItemStack(item, this.quantityDropped(worldIn.rand));
            itemstack.setTagCompound(((TileEntityStorage) te).getDropNBT());
            spawnAsEntity(worldIn, pos, itemstack);
        }
        else
        {
            super.harvestBlock(worldIn, player, pos, state, (TileEntity)null, stack);
        }
    }
}
/*******************************************************************************
* Copyright (c) 2018 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/