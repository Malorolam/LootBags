package mal.lootbags.handler;

import java.util.Random;

import mal.lootbags.Bag;
import mal.lootbags.LootBags;
import mal.lootbags.LootbagsUtil;
import mal.lootbags.config.BagEntitySource;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
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
		
		for(Bag b: BagHandler.getBagListRandomized())
		{
			//Get the drop roll for the drop resolution
			int chance = random.nextInt(LootBags.DROPRESOLUTION);
			
			//Get the weight 
			int weight;
			if(event.entityLiving instanceof IBossDisplayData)
				weight = b.getBossDropWeight();
			else if (event.entityLiving instanceof EntityMob || event.entityLiving instanceof IMob)
				weight = b.getMonsterDropWeight();
			else if(event.entityLiving instanceof EntityPlayer)
				weight = b.getPlayerDropWeight();
			else if (event.entityLiving instanceof EntityAnimal || event.entityLiving instanceof IAnimals)
				weight = b.getPassiveDropWeight();
			else
			{
				LootbagsUtil.LogInfo("Found entity of class: " + event.entityLiving.toString() + "; This is probably an error somewhere so going to assume this is a monster.");
				weight = b.getMonsterDropWeight();
			}
			
			//limit out the mob black/whitelists
			boolean state = true;//true is a drop
			if(!b.getEntityList().isEmpty())
			{
				if(b.getEntityExlusion())//true means only matches allow for drops
				{
					state = false;
					for(BagEntitySource bs: b.getEntityList())
					{
						if(bs.getIsVisibleName() && bs.getName().equalsIgnoreCase(event.entityLiving.getCommandSenderName()))
							state = true;
						else if(bs.getName().equalsIgnoreCase(EntityList.getEntityString(event.entityLiving)))
							state = true;
					}
				}
				else//false means a match prevents a drop
				{
					for(BagEntitySource bs: b.getEntityList())
					{
						if(bs.getIsVisibleName() && bs.getName().equalsIgnoreCase(event.entityLiving.getCommandSenderName()))
							state = false;
						else if(bs.getName().equalsIgnoreCase(EntityList.getEntityString(event.entityLiving)))
							state = false;
					}
				}
			}
			
			//Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Command Sender Name: " + event.entityLiving.getCommandSenderName() + ": EventList Name: " + EntityList.getEntityString(event.entityLiving)));
			
			if(chance <= weight && state && weight > 0)
			{
				event.entityLiving.entityDropItem(b.getBagItem(), random.nextInt(2)+1);
				if(LootBags.LIMITONEBAGPERDROP)
					return;
			}
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
