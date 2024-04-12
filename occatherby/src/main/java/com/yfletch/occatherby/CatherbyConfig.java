package com.yfletch.occatherby;

import com.yfletch.occore.v2.CoreConfig;
import net.runelite.client.config.*;

@ConfigGroup(CatherbyConfig.GROUP_NAME)
public interface CatherbyConfig extends CoreConfig
{
    String GROUP_NAME = "oc-catherby";
    @ConfigItem(
            keyName = "identifierAction",
            name = "identifierAction",
            description = "Name of action to fish at",
            position = 0
    )
    default String identifierAction()
    {
        return null;
    }
    @ConfigItem(
            keyName = "fishingAction",
            name = "fishingAction",
            description = "Name of action to use at fishing spot",
            position = 1
    )
    default String fishingAction()
    {
        return null;
    }

    @ConfigItem(
            keyName = "fishingItem",
            name = "fishingItem",
            description = "Name of item to use at fishing spot",
            position = 2
    )
    default String fishingItem()
    {
        return null;
    }

    @ConfigItem(
            keyName = "setBankTile",
            name = "setBankTile",
            description = "Sets bank tile",
            position = 3)
    default Button setBankTile()
    {
        return new Button();
    }

    @ConfigItem(
            keyName = "widgetsToClick",
            name = "widgetsToClick",
            description = "Names of widgets to click",
            position = 4)
    default String[] widgetsToClick()
    {
        return new String[5];
    }

    @ConfigItem(
            keyName = "cookedFish",
            name = "cookedFish",
            description = "Name of cooked fish",
            position = 5)
    default String[] cookedFish()
    {
        return new String[5];
    }
}
