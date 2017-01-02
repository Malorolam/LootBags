package mal.lootbags.blocks;

import javax.annotation.Nullable;

import mal.lootbags.LootBags;
import mal.lootbags.item.RecyclerItemBlock;
import mal.lootbags.tileentity.TileEntityRecycler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
//import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockRecycler extends BlockContainer{

	private final String name = "loot_recycler";
	
	public BlockRecycler() {
		super(Material.ROCK);
		GameRegistry.register(this.setRegistryName(name));
		GameRegistry.register(new RecyclerItemBlock(this), getRegistryName());
		this.setUnlocalizedName(LootBags.MODID + "_" + name);
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
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityRecycler();
	}

	/**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntity var10 = worldIn.getTileEntity(pos);
        	if((!(var10 instanceof TileEntityRecycler)))
        		return false;
            else
        		((TileEntityRecycler) var10).activate(worldIn, pos, playerIn);
            
            return true;
        }
    }
}
/*******************************************************************************
* Copyright (c) 2017 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/