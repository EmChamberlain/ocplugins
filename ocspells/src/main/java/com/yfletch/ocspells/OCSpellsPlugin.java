package com.yfletch.ocspells;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.yfletch.occore.v2.RunnerPlugin;
import static com.yfletch.occore.v2.interaction.Entities.banked;
import static com.yfletch.occore.v2.interaction.Entities.entity;
import static com.yfletch.occore.v2.interaction.Entities.item;
import static com.yfletch.occore.v2.interaction.Entities.spell;
import static com.yfletch.occore.v2.interaction.Entities.widget;
import static com.yfletch.occore.v2.util.Util.getSpellByName;
import static com.yfletch.occore.v2.util.Util.nameContaining;
import static com.yfletch.occore.v2.util.Util.parseList;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import org.pf4j.Extension;

@Slf4j
@Extension
@PluginDescriptor(
	name = "OC Spells",
	description = "One-click bankstanding spells",
	enabledByDefault = false
)
public class OCSpellsPlugin extends RunnerPlugin<SpellsContext>
{
	@Inject private SpellsConfig config;
	@Inject private SpellsContext context;

	@Inject private Client client;
	@Inject private ConfigManager configManager;

	@Inject
	public void init(SpellsConfig config, SpellsContext context)
	{
		setConfig(config);
		setContext(context);
		setConfigGroup(SpellsConfig.GROUP_NAME);

		refreshOnConfigChange(true);
	}

	@Override
	public void setup()
	{
		statistics.addDisplays("Casts", "Casts left");
		statistics.addPerHourDisplays("Casts");

		// convert spell string to spell
		final var spell = getSpellByName(config.spell());
		final var item = parseList(config.item());

		if (spell == null)
		{
			requirements().must(c -> false, "Invalid spell \"" + config.spell() + "\"");
			return;
		}

		requirements()
			.mustBeAbleToCast(spell)
			.mustHaveBanked(item);

		action().name("Open bank")
			.when(c -> !Inventory.contains(item) && !Bank.isOpen())
			.then(c -> entity(nameContaining("Bank")).interact("Use", "Bank"))
			.until(c -> Bank.isOpen())
			.delay(2);

		action().name("Deposit other items")
			.when(c -> Bank.isOpen()
				&& !Inventory.contains(item)
				&& c.getBankableItems().length > 0)
			.then(c -> item(c.getBankableItems()).depositX())
			.until(c -> c.getBankableItems().length == 0)
			.many()
			.delay(2);

		action().name("Withdraw items")
			.when(c -> Bank.isOpen() && !Inventory.contains(item))
			.then(c -> banked(item).withdrawX())
			.until(c -> Inventory.contains(item))
			.delay(2);

		action().name("Close bank")
			.when(c -> Bank.isOpen() && Inventory.contains(item))
			.until(c -> !Bank.isOpen())
			.then(c -> widget(WidgetID.BANK_GROUP_ID, "Close").interact())
			.delay(2);

		action().name("Cast spell on item")
			.when(c -> !Bank.isOpen() && config.castOnItem() && !c.flag("casting") && Inventory.contains(item))
			.then(c -> spell(spell).castOn(item(item)))
			.onClick(c -> c.flag("casting", true, 5));

		action().name("Cast spell")
			.when(c -> !Bank.isOpen() && !config.castOnItem() && !c.flag("casting") && Inventory.contains(item))
			.then(c -> spell(spell).cast())
			.onClick(c -> c.flag("casting", true, 5));

		action().name("Casting spell")
			.message("Casting spell...")
			.noop();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INVENTORY.getId())
		{
			final var items = parseList(config.item());

			statistics.add("Casts", 1);
			context.clear("casting");

			final var count = Math.max(Inventory.getCount(items), Inventory.getCount(true, items));
			statistics.set("Casts left", count + Bank.getCount(true, items));
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getOption().equals("Cast") && !event.getTarget().contains("->"))
		{
			client.createMenuEntry(-1)
				.setOption("One-click spell")
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e -> {
					final var spell = Text.removeTags(event.getTarget());
					configManager.setConfiguration(
						SpellsConfig.GROUP_NAME,
						"spell",
						spell
					);
				});
		}

		if (event.getItemId() > 0 && event.getOption().equals("Use"))
		{
			client.createMenuEntry(-1)
				.setOption("One-click item")
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e -> {
					final var item = Text.removeTags(event.getTarget());
					configManager.setConfiguration(
						SpellsConfig.GROUP_NAME,
						"item",
						item
					);
				});
		}
	}

	@Provides
	SpellsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpellsConfig.class);
	}
}
