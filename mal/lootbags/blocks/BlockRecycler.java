package mal.lootbags.blocks;

import mal.lootbags.LootBags;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockRecycler extends BlockContainer{

	private final String name = "loot_recycler";
	
	public BlockRecycler() {
		super(Material.rock);
		GameRegistry.registerBlock(this, name);
		this.setBlockName(LootBags.MODID + "_" + name);
		this.setHardness(1.5f);
		this.setResistance(20f);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	public String getName()
	{
		return name;
	}
	
/*	@Override
	public int getRenderType()
	{
		return 3;
	}*/
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityRecycler();
	}

	/**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z /*BlockPos pos, IBlockState state*/, EntityPlayer playerIn, int/*EnumFacing*/ side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntity var10 = world.getTileEntity(x, y, z);
        	if((!(var10 instanceof TileEntityRecycler)))
            {
            	return false;
            }

        	if(var10 instanceof TileEntityRecycler)
        		((TileEntityRecycler) var10).activate(world, x, y, z, playerIn);
            
            return true;
        }
    }
    
    @Override
	public void registerBlockIcons(IIconRegister ir)
	{
		this.blockIcon = ir.registerIcon("lootbags:recyclerTexture");
	}
}
/*******************************************************************************
* Copyright (c) 2016 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/