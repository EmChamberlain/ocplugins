package com.yfletch.occatherby;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.yfletch.occore.v2.RunnerPlugin;
import com.yfletch.occore.v2.interaction.Walking;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.Objects;

import static com.yfletch.occore.v2.interaction.Entities.*;
import static com.yfletch.occore.v2.util.Util.*;

@Slf4j
@Extension
@PluginDescriptor(
        name = "OC Catherby",
        description = "One-click Catherby fishing and banking",
        enabledByDefault = false
)
public class OCCatherbyPlugin extends RunnerPlugin<CatherbyContext>
{
    @Inject private CatherbyConfig config;
    @Inject private CatherbyContext context;

    @Inject
    public void init(CatherbyConfig config, CatherbyContext context)
    {
        setConfig(config);
        setContext(context);
        setConfigGroup(CatherbyConfig.GROUP_NAME);
        actionsPerTick(1);
    }

    private TileObject getNearestBankNPC(CatherbyContext c)
    {
        // get nearest npc that has a "Bank" option
        return c.objectHelper.getNearest(
                object -> Objects.equals(object.getName(), "Bank booth") && object.getActions() != null && Arrays.asList(object.getActions()).contains("Bank")
        );
    }

    private NPC getNearestHarpoonFishingSpotNPC(CatherbyContext c)
    {
        // get nearest npc that has a "Cage" and "Harpoon" option
        return c.npcHelper.getNearest(
                npc -> npc.getActions() != null && Arrays.asList(npc.getActions()).contains("Cage") && Arrays.asList(npc.getActions()).contains("Harpoon")
        );
    }

    private TileObject getNearestRangeObject(CatherbyContext c)
    {
        // get nearest npc that has a "Bank" option
        return c.objectHelper.getNearest(
                object -> Objects.equals(object.getName(), "Range") && object.getActions() != null && Arrays.asList(object.getActions()).contains("Cook")
        );
    }

    private static final WorldPoint bankWorldPoint = new WorldPoint(2814, 3437, 0);
    private static final WorldPoint fishingWorldPoint = new WorldPoint(2848, 3431, 0);
    @Override
    public void setup()
    {
        action().name("Drop fish")
                .when(c -> Inventory.contains("Burnt swordfish", "Burnt tuna") && !c.isCooking() && !c.isHarpooning())
                .then(c -> {
                    if (Inventory.contains("Burnt tuna"))
                        return item("Burnt tuna").drop();
                    else
                        return item("Burnt swordfish").drop();
                })
                .until(c -> !Inventory.contains("Burnt swordfish", "Burnt swordfish"))
                .many()
                .onClick(c -> c.clear("cooking"));

        action().name("Harpoon fishing spot")
                .oncePerTick()
                .when(c -> getNearestHarpoonFishingSpotNPC(c) != null && !Inventory.isFull() && !c.isHarpooning() && !c.flag("WasHarpooning") && Inventory.contains("Harpoon"))
                .then(c -> npc(getNearestHarpoonFishingSpotNPC(c).getId()).interact("Harpoon"))
                .until(c -> Inventory.isFull())
                .delay(2)
                .onComplete(c -> c.flag("WasHarpooning", true, 5));

        action().name("Move to bank")
                .when(c -> getNearestRangeObject(c) == null && Inventory.isFull())
                .then(c -> Walking.walkPathTo(bankWorldPoint, 3))
                .until(c -> getNearestRangeObject(c) != null)
                .delay(2)
                .many()
                .skipIfNull();

        action().name("Cook tuna")
                .when(c -> widget("Tuna", "Raw tuna").exists())
                .then(c -> widget("Tuna", "Raw tuna").interact("Cook"))
                .until(c -> !Inventory.contains("Raw tuna"))
                .repeat(2);

        action().name("Cook swordfish")
                .when(c -> widget("Swordfish", "Raw swordfish").exists())
                .then(c -> widget("Swordfish", "Raw swordfish").interact("Cook"))
                .until(c -> !Inventory.contains("Raw swordfish"))
                .repeat(2);

        action().name("Cook fish on range")
                .when(c -> getNearestRangeObject(c) != null && Inventory.isFull() && !c.isCooking() && Inventory.contains("Raw tuna", "Raw swordfish"))
                .then(c -> object(getNearestRangeObject(c).getId()).interact("Cook"))
                .until(c -> widget("Swordfish", "Raw swordfish").exists() || widget("Tuna", "Raw tuna").exists())
                .delay(2)
                .repeat(2);

        action().name("Open bank")
                .oncePerTick()
                .when(c -> getNearestBankNPC(c) != null && !Bank.isOpen() && !Inventory.contains("Raw tuna", "Raw swordfish") && Inventory.getAll().size() > 1)
                .then(c -> object(getNearestBankNPC(c).getId()).interact("Bank"))
                .until(c -> Bank.isOpen())
                .delay(1)
                .repeat(3);

        action().name("Deposit other items")
                .oncePerTick()
                .when(c -> Bank.isOpen() && Inventory.contains("Tuna", "Swordfish"))
                .then(c -> widget("Deposit inventory").interact())
                .until(c -> Inventory.isEmpty())
                .delay(3)
                .repeat(3)
                .skipIfNull();

        action().name("Withdraw harpoon")
                .oncePerTick()
                .when(c -> Bank.isOpen() && !Inventory.contains("Harpoon"))
                .then(c -> banked("Harpoon").withdraw(1))
                .until(c -> Inventory.contains("Harpoon"))
                .delay(3);

        action().name("Close bank")
                .oncePerTick()
                .when(c -> Bank.isOpen() && Inventory.contains("Harpoon"))
                .then(c -> widget(WidgetID.BANK_GROUP_ID, "Close").interact())
                .until(c -> !Bank.isOpen())
                .delay(1)
                .repeat(3);

        action().name("Move to fishing spot")
                .when(c -> Inventory.contains("Harpoon") && getNearestHarpoonFishingSpotNPC(c) == null && !Bank.isOpen() && !Inventory.isFull() && !c.isHarpooning() && !Movement.isWalking() && !c.flag("WasHarpooning"))
                .then(c -> Walking.walkPathTo(fishingWorldPoint, 3))
                .until(c -> getNearestHarpoonFishingSpotNPC(c) != null)
                .delay(2)
                .many()
                .skipIfNull();

    }

    @Provides
    CatherbyConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CatherbyConfig.class);
    }
}

