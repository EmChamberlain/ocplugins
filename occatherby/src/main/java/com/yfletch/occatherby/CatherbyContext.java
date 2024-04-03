package com.yfletch.occatherby;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yfletch.occore.util.NpcHelper;
import com.yfletch.occore.util.ObjectHelper;
import com.yfletch.occore.v2.CoreContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
@Slf4j
@Singleton
public class CatherbyContext extends CoreContext
{
    @Inject protected Client client;
    @Inject protected ObjectHelper objectHelper;
    @Inject protected NpcHelper npcHelper;

    private final int FISHING_ANIMATION = 623;
    private final int COOKING_ANIMATION = 897;

    private final int CHOPPING_ANIMATION = 999;

    private final int FIRE_ANIMATION = 999;

    public boolean isFishing()
    {
        //log.info("Animation: " + client.getLocalPlayer().getAnimation());
        return client.getLocalPlayer().getAnimation() == FISHING_ANIMATION || client.getLocalPlayer().getAnimation() == 622;
    }

    public boolean isCooking()
    {
        if (client.getLocalPlayer().getAnimation() == COOKING_ANIMATION)
        {
            flag("cooking", true, 2);
        }

        return flag("cooking");
    }

    public boolean isChopping()
    {
        return client.getLocalPlayer().getAnimation() == CHOPPING_ANIMATION;
    }

    public boolean isFire()
    {
        return client.getLocalPlayer().getAnimation() == FIRE_ANIMATION;
    }
}

