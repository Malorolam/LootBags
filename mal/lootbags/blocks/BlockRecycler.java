package mal.lootbags.blocks;

import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRecycler extends BlockContainer{

	public BlockRecycler() {
		super(Material.rock);
		this.setBlockName("recyclerblock");
		this.setHardness(1.5f);
		this.setResistance(20f);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityRecycler();
	}

	/**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntity var10 = world.getTileEntity(x,y,z);
        	if((!(var10 instanceof TileEntityRecycler)))
            {
            	return false;
            }

        	if(var10 instanceof TileEntityRecycler)
        		((TileEntityRecycler) var10).activate(world, x, y, z, par5EntityPlayer);
            
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
* Copyright (c) 2015 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/