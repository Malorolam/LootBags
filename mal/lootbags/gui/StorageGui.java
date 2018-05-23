package mal.lootbags.gui;

import org.lwjgl.opengl.GL11;

import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.handler.BagHandler;
import mal.lootbags.tileentity.TileEntityStorage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("FieldCanBeLocal")
public class StorageGui extends GuiContainer{

	private TileEntityStorage bench;
	private GuiButton outBtn;
	
	public StorageGui(InventoryPlayer player, TileEntityStorage te) {
		super(new StorageContainer(player, te));

		bench = te;
		ySize = 186;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		
		this.buttonList.add(this.outBtn = new GuiButton(1, this.width/2-27, this.height/2-42, 54, 12, "Cycle Bag"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch(button.id)
		{
		case 1:
			bench.cycleOutputID(true);
			break;
		}
	}
	
    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        
        if(LootbagsUtil.isPointInRegion(135, 16, 16, 16, mouseX, mouseY, guiLeft, guiTop))
        {
        	this.renderToolTip(new ItemStack(LootBags.lootbagItem, 1, bench.getID()), mouseX, mouseY);
        }
        
        if(LootbagsUtil.isPointInRegion(44, 26, 40, 8, mouseX, mouseY, guiLeft, guiTop))
        {
        	this.drawHoveringText(Integer.toString(bench.getStorage()), mouseX, mouseY);
        }
    }
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("lootbags", "textures/gui/storage_gui.png"));
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
	}

	/**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	this.fontRenderer.drawString("Lootbag Storage", 52, 5, 4210752);
    	this.fontRenderer.drawString("Stored: ", 44, 16, 4210752);
    	this.fontRenderer.drawString(LootbagsUtil.formatSciNot(bench.getStorage()), 44, 26, 4210752);
    	this.fontRenderer.drawString("Needed: ", 96, 16, 4210752);
    	this.fontRenderer.drawString(LootbagsUtil.formatSciNot(BagHandler.getBagValue(bench.getID())[1]), 96, 26, 4210752);
    	//this.fontRenderer.drawString(Integer.toString(bench.getID()), 52, 25, 4210752);
    	
    	this.itemRender.renderItemIntoGUI(new ItemStack(LootBags.lootbagItem,1,bench.getID()), 135, 16);
    	if(bench.getStorage() < BagHandler.getBagValue(bench.getID())[1])
    	{
            this.mc.renderEngine.bindTexture(new ResourceLocation("lootbags", "textures/gui/storage_gui.png"));
    		this.drawTexturedModalRect(135, 16, 176, 0, 16, 16);
    	}
/*
    	
		int red = (int)Math.floor(255*((float)bench.getStorage())/((float)Integer.MAX_VALUE));
		int green = 155-(int)Math.floor(155*((float)bench.getStorage())/((float)Integer.MAX_VALUE));
		Color color1 = new Color(red, green, 0, 255);
		int width = (int) Math.floor(162*((float)bench.getStorage())/((float)Integer.MAX_VALUE));
		this.drawRect(7, 37, 7+width, 41, color1.getRGB());*/
    	
    }

}
/*******************************************************************************
* Copyright (c) 2018 Malorolam.
* 
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the included license
* 
*********************************************************************************/