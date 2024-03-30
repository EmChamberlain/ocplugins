package com.yfletch.ocflyfishing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yfletch.occore.v2.CoreContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
@Slf4j
@Singleton
public class FlyFishingContext extends CoreContext
{
	@Inject private Client client;

	private final int FISHING_ANIMATION = 623;
	private final int COOKING_ANIMATION = 897;

	public boolean isFishing()
	{
		log.info("Animation: " + client.getLocalPlayer().getAnimation());
		return client.getLocalPlayer().getAnimation() == FISHING_ANIMATION;
	}

	public boolean isCooking()
	{
		if (client.getLocalPlayer().getAnimation() == COOKING_ANIMATION)
		{
			flag("cooking", true, 2);
		}

		return flag("cooking");
	}
}
