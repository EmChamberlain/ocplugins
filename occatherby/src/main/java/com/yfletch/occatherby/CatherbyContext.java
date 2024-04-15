package com.yfletch.occatherby;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yfletch.occore.util.NpcHelper;
import com.yfletch.occore.util.ObjectHelper;
import com.yfletch.occore.v2.CoreContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import java.util.ArrayList;

@Slf4j
@Singleton
public class CatherbyContext extends CoreContext
{
    @Inject protected Client client;
    @Inject protected ObjectHelper objectHelper;
    @Inject protected NpcHelper npcHelper;

    private final ArrayList<Integer> fishingAnimations = new ArrayList<>();
    private final ArrayList<Integer> cookingAnimations = new ArrayList<>();



    public CatherbyContext()
    {
        fishingAnimations.add(618);
//        fishingAnimations.add(619);
//        fishingAnimations.add(622);
//        fishingAnimations.add(6703);
//        fishingAnimations.add(6704);
//        fishingAnimations.add(6707);
//        fishingAnimations.add(6708);
//        fishingAnimations.add(6709);
//        fishingAnimations.add(7261);

//        cookingAnimations.add(883);
        cookingAnimations.add(896);
//        cookingAnimations.add(897);
    }

    public boolean isFishing()
    {
        if (client.getLocalPlayer().getAnimation() == -1)
            return false;
        //log.info("Animation: " + client.getLocalPlayer().getAnimation());
        return fishingAnimations.contains(client.getLocalPlayer().getAnimation());
    }

    public boolean isCooking()
    {
        if (client.getLocalPlayer().getAnimation() != -1 && cookingAnimations.contains(client.getLocalPlayer().getAnimation()))
        {
            flag("cooking", true, 2);
        }

        return flag("cooking");
    }

}

