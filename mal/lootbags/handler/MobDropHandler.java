package mal.lootbags.handler;

import java.util.Random;

import mal.lootbags.LootBags;
import net.minecraft.entity.EntityLiving;
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
		int chance = random.nextInt(1000);
		boolean bagdrop = false;
		if (event.entityLiving instanceof EntityPlayer)
		{
			if(chance < LootBags.LPLAYERDROPCHANCE && LootBags.LPLAYERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.EPLAYERDROPCHANCE && LootBags.EPLAYERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.RPLAYERDROPCHANCE && LootBags.RPLAYERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.UPLAYERDROPCHANCE && LootBags.UPLAYERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.CPLAYERDROPCHANCE && LootBags.CPLAYERDROPCHANCE > 0)
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
		}
		if (event.entityLiving instanceof EntityAnimal)
		{
			if(chance < LootBags.LPASSIVEDROPCHANCE && LootBags.LPASSIVEDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.EPASSIVEDROPCHANCE && LootBags.EPASSIVEDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.RPASSIVEDROPCHANCE && LootBags.RPASSIVEDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.UPASSIVEDROPCHANCE && LootBags.UPASSIVEDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.CPASSIVEDROPCHANCE && LootBags.CPASSIVEDROPCHANCE > 0)
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
		}
		if (event.entityLiving instanceof EntityMob)
		{
			if(chance < LootBags.LMONSTERDROPCHANCE && LootBags.LMONSTERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.EMONSTERDROPCHANCE && LootBags.EMONSTERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.RMONSTERDROPCHANCE && LootBags.RMONSTERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.UMONSTERDROPCHANCE && LootBags.UMONSTERDROPCHANCE > 0)
			{
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
				if(LootBags.LIMITONEBAGPERDROP)
					bagdrop = true;
			}
			if(!bagdrop && chance < LootBags.CMONSTERDROPCHANCE && LootBags.CMONSTERDROPCHANCE > 0)
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
		}
		
		if(event.entityLiving instanceof EntityLiving && chance < 250)
		{
			if(((EntityLiving)event.entityLiving).getCustomNameTag().equalsIgnoreCase("bacon_donut"))
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 5), random.nextInt(2) + 1);
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
