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
            position = 2
    )
    default String identifierAction()
    {
        return null;
    }
    @ConfigItem(
            keyName = "fishingAction",
            name = "fishingAction",
            description = "Name of action to use at fishing spot",
            position = 3
    )
    default String fishingAction()
    {
        return null;
    }

    @ConfigItem(
            keyName = "fishingItem",
            name = "fishingItem",
            description = "Name of item to use at fishing spot",
            position = 4
    )
    default String fishingItem()
    {
        return null;
    }

    @ConfigItem(
            keyName = "setBankTile",
            name = "setBankTile",
            description = "Sets bank tile",
            position = 5)
    default Button setBankTile()
    {
        return new Button();
    }


    @ConfigItem(
            keyName = "cookedFish",
            name = "cookedFish",
            description = "Name of cooked fish seperated by comma",
            position = 6)
    default String cookedFish(){return "swordfish,tuna";}

    @ConfigItem(
            keyName = "toCook",
            name = "toCook",
            description = "Whether to cook the fish or not",
            position = 7)
    default boolean toCook(){return true;}

}
