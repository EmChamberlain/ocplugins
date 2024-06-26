package com.yfletch.ocbankskills;

import com.yfletch.occore.v2.CoreConfig;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(BankSkillsConfig.GROUP_NAME)
public interface BankSkillsConfig extends CoreConfig
{
	String GROUP_NAME = "oc-bankskills";

	@ConfigSection(
		name = "Bank Skills",
		description = "Plugin settings",
		position = 2
	)
	String bankSkills = "bankSkills";

	@ConfigItem(
		keyName = "primary",
		name = "Primary item names",
		description = "Primary item names, comma separated",
		section = bankSkills,
		position = 1
	)
	@Range(min = -1)
	default String primary()
	{
		return null;
	}

	@ConfigItem(
		keyName = "secondary",
		name = "Secondary item names",
		description = "Secondary item names, comma separated",
		section = bankSkills,
		position = 2
	)
	@Range(min = -1)
	default String secondary()
	{
		return null;
	}

	@ConfigItem(
			keyName = "tertiary",
			name = "tertiary item names",
			description = "tertiary item names, comma separated",
			section = bankSkills,
			position = 3
	)
	@Range(min = -1)
	default String tertiary()
	{
		return null;
	}

	@ConfigItem(
		keyName = "product",
		name = "Product item name",
		description = "Option to click in the skill interface",
		section = bankSkills,
		position = 4
	)
	default String product()
	{
		return null;
	}

	@ConfigItem(
			keyName = "spam",
			name = "Spam use",
			description = "Whether or not to spam use",
			section = bankSkills,
			position = 5
	)
	default boolean spam()
	{
		return true;
	}
}
