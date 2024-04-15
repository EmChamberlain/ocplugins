package com.yfletch.occatherby;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.yfletch.occore.v2.RunnerPlugin;
import com.yfletch.occore.v2.interaction.Walking;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
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

    @Inject private OCCatherbyOverlay overlay;

    @Inject private OverlayManager overlayManager;
    private boolean overlayEnabled;

    @Inject
    public void init(CatherbyConfig config, CatherbyContext context)
    {
        setConfig(config);
        setContext(context);
        setConfigGroup(CatherbyConfig.GROUP_NAME);
        actionsPerTick(1);
        fishingWorldPoint = context.client.getLocalPlayer().getWorldLocation();
    }

    private TileObject getNearestBankNPC(CatherbyContext c)
    {
        // get nearest npc that has a "Bank" option
        return c.objectHelper.getNearest(
                object -> object != null && object.getActions() != null && Arrays.asList(object.getActions()).contains("Bank")
        );
    }

    private NPC getNearestFishingSpotNPC(CatherbyContext c)
    {
        // get nearest npc that has a "Cage" and "Harpoon" option
        return c.npcHelper.getNearest(
                npc -> npc.getActions() != null && Arrays.asList(npc.getActions()).contains(config.identifierAction()) && Arrays.asList(npc.getActions()).contains(config.fishingAction())
        );
    }

    private TileObject getNearestRangeObject(CatherbyContext c)
    {
        // get nearest npc that has a "Bank" option
        return c.objectHelper.getNearest(
                object -> object != null && object.getActions() != null && Arrays.asList(object.getActions()).contains("Cook")
        );
    }

    public WorldPoint bankWorldPoint = new WorldPoint(2814, 3437, 0);

    //TODO: Implement below
    public WorldPoint cookWorldPoint = new WorldPoint(2814, 3440, 0);
    public WorldPoint fishingWorldPoint = new WorldPoint(2848, 3431, 0);

    @Override
    public void setup()
    {
        action().name("Drop fish")
                .oncePerTick()
                .when(c -> Inventory.contains(x -> x.getName().toLowerCase().contains("burnt")) && !c.isCooking() && !c.isFishing())
                .then(c -> item(x -> x.toLowerCase().contains("burnt")).drop())
                .until(c -> !Inventory.contains(x -> x.getName().toLowerCase().contains("burnt")))
                .many()
                .onClick(c -> c.clear("cooking"))
                .skipIfNull();

        action().name("Cook")
                .when(c -> widget(x -> {
                    String[] splitFish = config.cookedFish().split(",");
                    for (String fish : splitFish)
                    {
                        if (x.toLowerCase().contains(fish.toLowerCase()))
                            return true;
                    }
                    return false;
                }).exists())
                .then(c -> widget(x -> {
                    String[] splitFish = config.cookedFish().split(",");
                    for (String fish : splitFish)
                    {
                        if (x.toLowerCase().contains(fish.toLowerCase()))
                            return true;
                    }
                    return false;
                }).interact("Cook"))
                .until(c -> !Inventory.contains("Raw"))
                .repeat(2)
                .skipIfNull();


        action().name("Fishing spot")
                .oncePerTick()
                .when(c -> getNearestFishingSpotNPC(c) != null && !Inventory.isFull() && !c.isFishing() && !c.flag("WasFishing") && Inventory.contains(config.fishingItem()))
                .then(c -> npc(getNearestFishingSpotNPC(c).getId()).interact(config.fishingAction()))
                .until(c -> Inventory.isFull())
                .delay(2)
                .onComplete(c -> c.flag("WasFishing", true, 5))
                .skipIfNull();

        action().name("Move to bank")
                .when(c -> getNearestRangeObject(c) == null && Inventory.isFull())
                .then(c -> Walking.walkPathTo(bankWorldPoint, 3))
                .until(c -> getNearestRangeObject(c) != null)
                .delay(2)
                .many()
                .skipIfNull();

        action().name("Cook fish")
                .when(c -> config.toCook() && getNearestRangeObject(c) != null && Inventory.isFull() && !c.isCooking() && Inventory.contains("Raw"))
                .then(c -> object(getNearestRangeObject(c).getId()).interact("Cook"))
                .until(c -> widget(x -> {
                    String[] splitFish = config.cookedFish().split(",");
                    for (String fish : splitFish)
                    {
                        if (x.toLowerCase().contains(fish.toLowerCase()))
                            return true;
                    }
                    return false;
                }).exists())
                .delay(2)
                .repeat(2)
                .skipIfNull();

        action().name("Open bank")
                .oncePerTick()
                .when(c -> getNearestBankNPC(c) != null && !Bank.isOpen() && !Inventory.contains("Raw", "Burnt") && Inventory.getAll().size() > 1)
                .then(c -> object(getNearestBankNPC(c).getId()).interact("Bank"))
                .until(c -> Bank.isOpen())
                .delay(1)
                .repeat(3)
                .skipIfNull();

        action().name("Deposit other items")
                .oncePerTick()
                .when(c -> Bank.isOpen() && Inventory.contains(x -> {
                    String[] splitFish = config.cookedFish().split(",");
                    for (String fish : splitFish)
                    {
                        if (x.getName().toLowerCase().contains(fish.toLowerCase()))
                            return true;
                    }
                    return false;
                }))
                .then(c -> widget("Deposit inventory").interact())
                .until(c -> Inventory.isEmpty())
                .delay(3)
                .repeat(3)
                .skipIfNull();

        action().name("Withdraw harpoon")
                .oncePerTick()
                .when(c -> Bank.isOpen() && !Inventory.contains(config.fishingItem()))
                .then(c -> banked(config.fishingItem()).withdraw(1))
                .until(c -> Inventory.contains(config.fishingItem()))
                .delay(3)
                .skipIfNull();

        action().name("Close bank")
                .oncePerTick()
                .when(c -> Bank.isOpen() && Inventory.contains(config.fishingItem()))
                .then(c -> widget(WidgetID.BANK_GROUP_ID, "Close").interact())
                .until(c -> !Bank.isOpen())
                .delay(1)
                .repeat(3)
                .skipIfNull();

        action().name("Move to fishing spot")
                .when(c -> Inventory.contains( x -> x.getName().toLowerCase().contains(config.fishingItem().toLowerCase())) && getNearestFishingSpotNPC(c) == null && !Bank.isOpen() && Inventory.getAll().size() == 1 && !c.isFishing() && !Movement.isWalking() && !c.flag("WasFishing"))
                .then(c -> Walking.walkPathTo(fishingWorldPoint, 3))
                .until(c -> getNearestFishingSpotNPC(c) != null)
                .delay(2)
                .many()
                .skipIfNull();

    }

    @Provides
    CatherbyConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CatherbyConfig.class);
    }

    @Subscribe
    public void onConfigButtonPressed(ConfigButtonClicked event)
    {
        if (!event.getGroup().contains("oc-catherby"))
        {
            return;
        }

        if (event.getKey().toLowerCase().contains("setbanktile"))
        {
            bankWorldPoint = context.client.getLocalPlayer().getWorldLocation();
        }
        if (event.getKey().toLowerCase().contains("setfishtile"))
        {
            bankWorldPoint = context.client.getLocalPlayer().getWorldLocation();
        }
        if (event.getKey().toLowerCase().contains("setcooktile"))
        {
            bankWorldPoint = context.client.getLocalPlayer().getWorldLocation();
        }

    }

    @Override
    protected void startUp()
    {
        super.startUp();
        enableOverlay();
    }

    @Override
    protected void shutDown()
    {
        super.shutDown();
        disableOverlay();
    }

    private void enableOverlay()
    {
        if (overlayEnabled)
        {
            return;
        }

        overlayEnabled = true;
        overlayManager.add(overlay);
    }

    private void disableOverlay()
    {
        if (overlayEnabled)
        {
            overlayManager.remove(overlay);
        }
        overlayEnabled = false;
    }
}

