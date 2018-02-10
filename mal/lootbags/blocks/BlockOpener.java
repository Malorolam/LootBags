package mal.lootbags.blocks;

import mal.lootbags.LootBags;
import mal.lootbags.tileentity.TileEntityOpener;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockOpener extends BlockContainer{

	private final String name = "loot_opener";
	
	public BlockOpener() {
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
		return new TileEntityOpener();
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
	    	if(!(var10 instanceof TileEntityOpener))
	    		return false;
	    	else
	    		((TileEntityOpener)var10).activate(worldIn, pos, playerIn);
	    	return true;
	    }
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState blockstate)
	{
		TileEntityOpener te = (TileEntityOpener) world.getTileEntity(pos);
		InventoryHelper.dropInventoryItems(world, pos, te);
		super.breakBlock(world, pos, blockstate);
	}
}
/*******************************************************************************
* Copyright (c) 2018 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/