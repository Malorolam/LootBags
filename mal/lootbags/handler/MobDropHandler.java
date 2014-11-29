package mal.lootbags.handler;

import java.util.Random;

import mal.lootbags.LootBags;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Adds the loot bag to mob drops, configurable through config
 * 
 * @author Mal
 * 
 */
public class MobDropHandler {

	public static Random random = new Random();

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) {
		if (event.entityLiving instanceof EntityPlayer
				&& LootBags.PLAYERDROPCHANCE > 0
				&& random.nextInt(100) < LootBags.PLAYERDROPCHANCE) {
			event.entityLiving.entityDropItem(
					new ItemStack(LootBags.lootbag, 1), random.nextInt(2) + 1);
		}
		if (event.entityLiving instanceof EntityAnimal
				&& LootBags.PASSIVEDROPCHANCE > 0
				&& random.nextInt(100) < LootBags.PASSIVEDROPCHANCE) {
			event.entityLiving.entityDropItem(
					new ItemStack(LootBags.lootbag, 1), random.nextInt(2) + 1);
		}
		if (event.entityLiving instanceof EntityMob
				&& LootBags.MONSTERDROPCHANCE > 0
				&& random.nextInt(100) < LootBags.MONSTERDROPCHANCE) {
			event.entityLiving.entityDropItem(
					new ItemStack(LootBags.lootbag, 1), random.nextInt(2) + 1);
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
