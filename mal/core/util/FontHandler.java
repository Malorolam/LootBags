package mal.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

//a handler for font because blarg
public class FontHandler {

	public final static FontHandler normal = new FontHandler(false);
	
	private FontRenderer fontRenderer;
	private static final ResourceLocation vanillaFontLocation = new ResourceLocation("textures/font/ascii.png");
	
	private FontHandler(boolean size)
	{
		Minecraft mc = Minecraft.getMinecraft();
		fontRenderer = new FontRenderer(mc.gameSettings, vanillaFontLocation, mc.getTextureManager(), false);
		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(fontRenderer);
	}
	
	public void print(Object o, int x, int y)
	{
		fontRenderer.drawString(String.valueOf(o), x, y, 8, false);
	}
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/