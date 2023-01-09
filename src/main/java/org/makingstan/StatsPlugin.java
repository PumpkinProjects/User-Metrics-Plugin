package org.makingstan;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.ArrayList;

@Slf4j
@PluginDescriptor(
	name = "Stats"
)
public class StatsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private StatsConfig config;

	private JsonBuilder jsonBuilder;
	private boolean sendDatabaseCycleStarted = false;

	@Override
	protected void startUp() throws Exception
	{

	}

	@Override
	protected void shutDown() throws Exception
	{

	}

	/*
		When we receive loot we want to send the data over to our JsonBuilder instance so that class can handle the json
	*/
	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived) {
		jsonBuilder = new JsonBuilder(config.getNPCTimeTreshold());
		ArrayList<String> lootArray = new ArrayList<>();

		final NPC npc = npcLootReceived.getNpc();
		final ItemStack[] items = npcLootReceived.getItems().toArray(new ItemStack[0]);
		final String name = npc.getName();

		for(int i = 0; i < items.length; i++)
		{
			lootArray.add("id="+items[i].getId()+",quantity="+items[i].getQuantity()+",location="+items[i].getLocation().toString());
		}

		jsonBuilder.addNPCKill(name+",  ", String.valueOf(lootArray));
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && !sendDatabaseCycleStarted)
		{
			jsonBuilder.startNPCCycle();
			sendDatabaseCycleStarted = true;
		}
	}
	@Provides
	StatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StatsConfig.class);
	}
}
