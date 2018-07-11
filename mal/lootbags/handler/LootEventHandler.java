package mal.lootbags.handler;

import mal.lootbags.LootBags;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class LootEventHandler {

    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(LootBags.configMismatch)
            event.player.sendMessage(new TextComponentString("Lootbags Config Version mismatch detected, looking for " + LootBags.CONFIGVERSION + "  This means " +
                    "that the default bag config changed.  Please delete the lootbags_BagConfig.cfg file to get the changes or update the config version in the file to remove this message."));
    }
}
/*******************************************************************************
 * Copyright (c) 2018 Malorolam.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the included license.
 *
 *********************************************************************************/
