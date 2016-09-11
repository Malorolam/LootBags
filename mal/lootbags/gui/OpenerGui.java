package mal.lootbags.gui;

import org.lwjgl.opengl.GL11;

import mal.lootbags.LootBags;
import mal.lootbags.tileentity.TileEntityOpener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class OpenerGui extends GuiContainer {

	TileEntityOpener bench;
	
	public OpenerGui(InventoryPlayer player, TileEntityOpener te) {
		super(new OpenerContainer(player, te));

		bench = te;
		ySize = 186;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("lootbags", "textures/gui/openerGui.png"));
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
	}

	/**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
    	this.fontRendererObj.drawString("Lootbag Opener", 52, 5, 4210752);
    	
    	if(LootBags.OPENERMAXCOOLDOWN>0)
    	{
    		GL11.glPushMatrix();
    		GL11.glScalef(0.7f, 0.7f, 0.7f);
    		this.fontRendererObj.drawString("Cooldown: " + bench.getCooldown(), 103, 52, 4210752);
    		GL11.glPopMatrix();
    	}
    	
    }
}
/*******************************************************************************
 * Copyright (c) 2016 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/