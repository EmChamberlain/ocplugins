package com.yfletch.occatherby;

import com.yfletch.occore.v2.CoreConfig;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CatherbyConfig.GROUP_NAME)
public interface CatherbyConfig extends CoreConfig
{
    String GROUP_NAME = "oc-catherby";
}
