package com.yfletch.ocbankskills;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.yfletch.occore.v2.RunnerPlugin;
import static com.yfletch.occore.v2.interaction.Entities.banked;
import static com.yfletch.occore.v2.interaction.Entities.entity;
import static com.yfletch.occore.v2.interaction.Entities.item;
import static com.yfletch.occore.v2.interaction.Entities.widget;
import static com.yfletch.occore.v2.util.Util.join;
import static com.yfletch.occore.v2.util.Util.nameContaining;
import static com.yfletch.occore.v2.util.Util.nameNotMatching;
import static com.yfletch.occore.v2.util.Util.parseList;

import com.yfletch.occore.v2.interaction.DeferredEntityInteraction;
import com.yfletch.occore.v2.interaction.DeferredInteractable;
import com.yfletch.occore.v2.interaction.DeferredInteraction;
import com.yfletch.occore.v2.test.TestContext;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import org.pf4j.Extension;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Extension
@PluginDescriptor(
	name = "OC Bank Skills custom",
	enabledByDefault = false
)
public class OCBankSkillsPlugin extends RunnerPlugin<BankSkillsContext>
{
	@Inject BankSkillsConfig config;

	@Inject
	public void init(BankSkillsConfig config, BankSkillsContext context)
	{
		setConfig(config);
		setContext(context);
		setConfigGroup(BankSkillsConfig.GROUP_NAME);
		refreshOnConfigChange(true);
		actionsPerTick(1);
		//log.info("Bank skills init called 1");
	}


	@Override
	public void setup()
	{
		requirements().name("Config requirements")
			.must(c -> primary().length > 0, "Primary item(s) must be set")
			.must(c -> secondary().length > 0, "Secondary item(s) must be set")
			.must(c -> product().length > 0, "Product item(s) must be set")
			.mustBeNear(() -> entity(nameContaining("bank")), "any bank");

		action().name("Open bank")
			.oncePerTick()
			.when(c -> (!Inventory.contains(primary()) || !Inventory.contains(secondary()) || !Inventory.contains(tertiary())) && (!Bank.isOpen()))
			.then(c -> entity(nameContaining("bank")).interact("Use", "Bank"))
			.until(c -> Bank.isOpen());

		action().name("Deposit other items")
			.oncePerTick()
			.when(c -> Bank.isOpen() && !Inventory.isEmpty() && !Inventory.contains(primary()))
			.then(c -> widget("Deposit inventory").interact())
			.repeat(3);

		action().name("Withdraw primary")
			.oncePerTick()
			.when(c -> Bank.isOpen() && !Inventory.contains(primary()))
			.then(c -> banked(primary()).withdrawX())
			.until(c -> Inventory.contains(primary()))
			.delay(1);

		action().name("Withdraw secondary")
			.oncePerTick()
			.when(c -> Bank.isOpen() && !Inventory.contains(secondary()))
			.then(c -> banked(secondary()).withdrawX())
			.until(c -> Inventory.contains(secondary()))
			.delay(1);

		action().name("Withdraw tertiary")
			.oncePerTick()
			.when(c -> tertiary().length > 0 && Bank.isOpen() && !Inventory.contains(tertiary()))
			.then(c -> banked(tertiary()).withdrawX())
			.until(c -> Inventory.contains(tertiary()))
			.delay(1);

		action().name("Close bank")
			.oncePerTick()
			.when(c -> Bank.isOpen())
			.then(c -> widget(WidgetID.BANK_GROUP_ID, "Close").interact())
			.until(c -> !Bank.isOpen())
			.delay(1);

		action().name("Click make")
			.oncePerTick()
			.when(c -> {
				for (String widgetString : config.product().split(","))
				{
					var widget = widget(x -> x != null && !x.isEmpty() && x.toLowerCase().contains(widgetString.toLowerCase()));
					if (widget.exists())
						return true;
				}
				return false;
			})
			.then(c -> {
				for (String widgetString : config.product().split(","))
				{
					var widget = widget(x -> x != null && !x.isEmpty() && x.toLowerCase().contains(widgetString.toLowerCase()));
					if (widget.exists())
						return widget.interact("Make");
				}
				log.info("No widget in then. Returning null");
				return null;
			});

		action().name("Use items")
			.oncePerTick()
			.when(c -> /*!c.isAnimating() &&*/ Inventory.contains(primary()) && Inventory.contains(secondary()) && spam())
			.then(c -> item(primary()).useOn(item(secondary())))
			// doesn't work on the same tick the bank was opened
			//.delay(1)
			.repeat(27);

		action().name("Use items")
			//.oncePerTick()
			.when(c -> !c.isAnimating() && Inventory.contains(primary()) && Inventory.contains(secondary()) && !spam() && !c.flag("postdelay"))
			.then(c -> item(primary()).useOn(item(secondary())))
			// doesn't work on the same tick the bank was opened
			.delay(1)
			.repeat(2)
			.onComplete(c -> c.flag("postdelay", true, 20));
	}

	private String[] primary()
	{
		return parseList(config.primary());
	}

	private String[] secondary()
	{
		return parseList(config.secondary());
	}

	private String[] tertiary()
	{
		return parseList(config.tertiary());
	}

	private String[] product()
	{
		return parseList(config.product());
	}

	private boolean spam()
	{
		return config.spam();
	}

	@Provides
	BankSkillsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankSkillsConfig.class);
	}
}
