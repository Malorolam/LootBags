package mal.lootbags.handler;

import java.util.Random;

import mal.lootbags.LootBags;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.common.util.FakePlayer;

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
		if(LootBags.BAGFROMPLAYERKILL == 1)
		{
			if(event.source.getEntity() == null || !(event.source.getEntity() instanceof EntityPlayer))
				return;
		}
		else if(LootBags.BAGFROMPLAYERKILL == 2)
		{
			if(event.source.getEntity() == null ||  event.source.getEntity() instanceof FakePlayer || !(event.source.getEntity() instanceof EntityPlayer))
				return;
		}
		
		int chance = random.nextInt(LootBags.DROPRESOLUTION);
		boolean bagdrop = false;
		if(LootBags.LIMITONEBAGPERDROP)
		{
			if (event.entityLiving instanceof EntityPlayer)
			{
				//try a weighting system instead so there is a better distribution of bags
				int totalweight = LootBags.PLAYERDROPCHANCES[4]+LootBags.PLAYERDROPCHANCES[3]+LootBags.PLAYERDROPCHANCES[2]+LootBags.PLAYERDROPCHANCES[1]+LootBags.PLAYERDROPCHANCES[0];
				
				if(chance < totalweight)//getting a bag
				{
					if(chance < LootBags.PLAYERDROPCHANCES[0] && LootBags.PLAYERDROPCHANCES[0] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PLAYERDROPCHANCES[0] > 0)
						chance -= LootBags.PLAYERDROPCHANCES[0];
					
					if(chance < LootBags.PLAYERDROPCHANCES[1] && LootBags.PLAYERDROPCHANCES[1] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PLAYERDROPCHANCES[1] > 0)
						chance -= LootBags.PLAYERDROPCHANCES[1];
					
					if(chance < LootBags.PLAYERDROPCHANCES[2] && LootBags.PLAYERDROPCHANCES[2] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PLAYERDROPCHANCES[2] > 0)
						chance -= LootBags.PLAYERDROPCHANCES[2];
					
					if(chance < LootBags.PLAYERDROPCHANCES[3] && LootBags.PLAYERDROPCHANCES[3] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PLAYERDROPCHANCES[3] > 0)
						chance -= LootBags.PLAYERDROPCHANCES[3];
					
					if(chance < LootBags.PLAYERDROPCHANCES[4] && LootBags.PLAYERDROPCHANCES[4] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PLAYERDROPCHANCES[4] > 0)
						chance -= LootBags.PLAYERDROPCHANCES[4];
				}
			}
			if (event.entityLiving instanceof EntityAnimal)
			{
				int totalweight = LootBags.PASSIVEDROPCHANCES[4]+LootBags.PASSIVEDROPCHANCES[3]+LootBags.PASSIVEDROPCHANCES[2]+LootBags.PASSIVEDROPCHANCES[1]+LootBags.PASSIVEDROPCHANCES[0];
				
				if(chance < totalweight)//getting a bag
				{
					if(chance < LootBags.PASSIVEDROPCHANCES[0] && LootBags.PASSIVEDROPCHANCES[0] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PASSIVEDROPCHANCES[0] > 0)
						chance -= LootBags.PASSIVEDROPCHANCES[0];
					
					if(chance < LootBags.PASSIVEDROPCHANCES[1] && LootBags.PASSIVEDROPCHANCES[1] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PASSIVEDROPCHANCES[1] > 0)
						chance -= LootBags.PASSIVEDROPCHANCES[1];
					
					if(chance < LootBags.PASSIVEDROPCHANCES[2] && LootBags.PASSIVEDROPCHANCES[2] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PASSIVEDROPCHANCES[2] > 0)
						chance -= LootBags.PASSIVEDROPCHANCES[2];
					
					if(chance < LootBags.PASSIVEDROPCHANCES[3] && LootBags.PASSIVEDROPCHANCES[3] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PASSIVEDROPCHANCES[3] > 0)
						chance -= LootBags.PASSIVEDROPCHANCES[3];
					
					if(chance < LootBags.PASSIVEDROPCHANCES[4] && LootBags.PASSIVEDROPCHANCES[4] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.PASSIVEDROPCHANCES[4] > 0)
						chance -= LootBags.PASSIVEDROPCHANCES[4];
				}
			}
			if (event.entityLiving instanceof EntityMob)
			{
				int totalweight = LootBags.MONSTERDROPCHANCES[4]+LootBags.MONSTERDROPCHANCES[3]+LootBags.MONSTERDROPCHANCES[2]+LootBags.MONSTERDROPCHANCES[1]+LootBags.MONSTERDROPCHANCES[0];
				//System.out.println(chance + "/" + totalweight);
				
				if(chance < totalweight)//getting a bag
				{
					if(chance < LootBags.MONSTERDROPCHANCES[0] && LootBags.MONSTERDROPCHANCES[0] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.MONSTERDROPCHANCES[0] > 0)
						chance -= LootBags.MONSTERDROPCHANCES[0];
					
					if(chance < LootBags.MONSTERDROPCHANCES[1] && LootBags.MONSTERDROPCHANCES[1] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.MONSTERDROPCHANCES[1] > 0)
						chance -= LootBags.MONSTERDROPCHANCES[1];
					
					if(chance < LootBags.MONSTERDROPCHANCES[2] && LootBags.MONSTERDROPCHANCES[2] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.MONSTERDROPCHANCES[2] > 0)
						chance -= LootBags.MONSTERDROPCHANCES[2];
					
					if(chance < LootBags.MONSTERDROPCHANCES[3] && LootBags.MONSTERDROPCHANCES[3] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.MONSTERDROPCHANCES[3] > 0)
						chance -= LootBags.MONSTERDROPCHANCES[3];
					
					if(chance < LootBags.MONSTERDROPCHANCES[4] && LootBags.MONSTERDROPCHANCES[4] > 0)
					{
						event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
						return;
					}
					else if(LootBags.MONSTERDROPCHANCES[4] > 0)
						chance -= LootBags.MONSTERDROPCHANCES[4];
				}
			}
		}
		else
		{
			if (event.entityLiving instanceof EntityPlayer)
			{
				if(!bagdrop && chance < LootBags.PLAYERDROPCHANCES[0] && LootBags.PLAYERDROPCHANCES[0] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PLAYERDROPCHANCES[1] && LootBags.PLAYERDROPCHANCES[1] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PLAYERDROPCHANCES[2] && LootBags.PLAYERDROPCHANCES[2] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PLAYERDROPCHANCES[3] && LootBags.PLAYERDROPCHANCES[3] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(chance < LootBags.PLAYERDROPCHANCES[4] && LootBags.PLAYERDROPCHANCES[4] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
			}
			if (event.entityLiving instanceof EntityAnimal)
			{
				if(!bagdrop && chance < LootBags.PASSIVEDROPCHANCES[0] && LootBags.PASSIVEDROPCHANCES[0] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PASSIVEDROPCHANCES[1] && LootBags.PASSIVEDROPCHANCES[1] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PASSIVEDROPCHANCES[2] && LootBags.PASSIVEDROPCHANCES[2] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.PASSIVEDROPCHANCES[3] && LootBags.PASSIVEDROPCHANCES[3] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(chance < LootBags.PASSIVEDROPCHANCES[4] && LootBags.PASSIVEDROPCHANCES[4] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
			}
			if (event.entityLiving instanceof EntityMob)
			{
				if(!bagdrop && chance < LootBags.MONSTERDROPCHANCES[0] && LootBags.MONSTERDROPCHANCES[0] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 0), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.MONSTERDROPCHANCES[1] && LootBags.MONSTERDROPCHANCES[1] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 1), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.MONSTERDROPCHANCES[2] && LootBags.MONSTERDROPCHANCES[2] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 2), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(!bagdrop && chance < LootBags.MONSTERDROPCHANCES[3] && LootBags.MONSTERDROPCHANCES[3] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 3), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
				chance = random.nextInt(1000);
				if(chance < LootBags.MONSTERDROPCHANCES[4] && LootBags.MONSTERDROPCHANCES[4] > 0)
				{
					event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 4), random.nextInt(2) + 1);
					chance = random.nextInt(1000);
				}
			}
		}

		if(event.entityLiving instanceof EntityLiving && chance < LootBags.SPECIALDROPCHANCE && LootBags.SPECIALDROPCHANCE>0)
		{
			if(((EntityLiving)event.entityLiving).getCustomNameTag().equalsIgnoreCase("bacon_donut"))
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 5), random.nextInt(2) + 1);
			if(((EntityLiving)event.entityLiving).getCustomNameTag().equalsIgnoreCase("soaryn"))
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 7), random.nextInt(2) + 1);
			if(((EntityLiving)event.entityLiving).getCustomNameTag().equalsIgnoreCase("wyld"))
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 8), random.nextInt(2) + 1);
			if(((EntityLiving)event.entityLiving).getCustomNameTag().equalsIgnoreCase("giantwaffle"))
				event.entityLiving.entityDropItem(new ItemStack(LootBags.lootbag, 1, 9), random.nextInt(2) + 1);
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2015 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 * 
 *********************************************************************************/
