package org.makingstan;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

import javax.swing.*;

@ConfigGroup("Stats")
public interface StatsConfig extends Config
{
	@ConfigItem(
			keyName = "The interval that the client will send your data to the server. (in minutes)",
			name = "Sending Interval",
			description = "The interval that the client will send your data to the server. (in minutes)"
	)
	@Range(
			min = 1,
			max = 15
	)
	default int getNPCTimeTreshold() { return 1; }

	@ConfigItem(
			name = "Privacy",
			keyName = "Decides if you want your data to be public or not.",
			description = "Decides if you want your data to be public or not."
	)
	default boolean getPrivacy() { return false; }
}
