package com.yfletch.occatherby;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.yfletch.occore.v2.RunnerPlugin;
import com.yfletch.occore.v2.interaction.Walking;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import org.pf4j.Extension;

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

    private static final WorldPoint bankWorldPoint = new WorldPoint(2809, 3441, 0);
    private static final WorldPoint fishingWorldPoint = new WorldPoint(2848, 3431, 0);
    @Override
    public void setup()
    {
        requirements().mustHave("Harpoon");

        action().name("Drop fish")
                .when(c -> Inventory.contains("Raw tuna", "Burnt fish") && Inventory.isFull() && !c.isCooking() && !c.isChopping() && !c.isFire() && !c.isFishing())
                .then(c -> item("Raw tuna", "Burnt fish").drop())
                .until(c -> !Inventory.contains("Raw tuna", "Burnt fish"))
                .many()
                .onClick(c -> c.clear("cooking"));

        action().name("Harpoon fishing spot")
                .oncePerTick()
                .when(c -> npc("Fishing spot").exists() && !Inventory.isFull() && !c.isFishing())
                .then(c -> npc("Fishing spot").interact("Harpoon"))
                .until(c -> Inventory.isFull())
                .resetsOnTick(true)
                .delay(2)
                .repeat(2);

        action().name("Move to bank")
                .when(c -> !entity(nameContaining("bank")).exists() && Inventory.isFull())
                .then(c -> Walking.walkPathTo(bankWorldPoint, 3))
                .until(c -> entity(nameContaining("bank")).exists())
                .delay(2)
                .many()
                .skipIfNull();

        action().name("Open bank")
                .oncePerTick()
                .when(c -> entity(nameContaining("bank")).exists() && !Bank.isOpen() && Inventory.isFull())
                .then(c -> entity(nameContaining("bank")).interact("Use", "Bank"))
                .until(c -> Bank.isOpen())
                .delay(1)
                .repeat(3);

        action().name("Deposit other items")
                .oncePerTick()
                .when(c -> Bank.isOpen() && Inventory.contains(nameNotMatching("Raw tuna", "Raw swordfish")))
                .then(c -> widget("Deposit inventory").interact())
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
                .when(c -> Inventory.contains("Harpoon") && !npc("Fishing spot").exists() && !Bank.isOpen() && !Inventory.isFull() && !c.isFishing() && !Movement.isWalking())
                .then(c -> Walking.walkPathTo(fishingWorldPoint, 3))
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

