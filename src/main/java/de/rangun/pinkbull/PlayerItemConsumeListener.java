/*
 * Copyright 2021 by Heiko Schäfer <heiko@rangun.de>
 *
 * This file is part of PinkBull.
 *
 * PinkBull is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * PinkBull is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PinkBull.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rangun.pinkbull;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author heiko
 *
 */
public final class PlayerItemConsumeListener implements Listener {

	private final PinkBullPlugin plugin;
	private final long FLY_TICKS = 800L; // 18000L;

	/**
	 * 
	 */
	public PlayerItemConsumeListener(final PinkBullPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerItemConsume(final PlayerItemConsumeEvent ev) {

		final ItemStack item = ev.getItem();

		if (item.getItemMeta().getPersistentDataContainer().getKeys().contains(plugin.getPinkBullPotionKey())) {

			Player player = ev.getPlayer();

			ev.setItem(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 100.0f, 0.0f);

			plugin.setPlayerFlyAllowed(player, true);

			new BukkitRunnable() {

				@Override
				public void run() {
					player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 100.0f, 0.0f);
					player.sendMessage(ChatColor.RED + "Dein " + ChatColor.BOLD + PinkBullPlugin.PINK_BULL_TEXT
							+ ChatColor.RED + "-Flugmodus wird in 20 Sekunden beendet." + ChatColor.RESET);
				}

			}.runTaskLater(plugin, FLY_TICKS - 400L);

			new BukkitRunnable() {

				@Override
				public void run() {
					plugin.setPlayerFlyAllowed(player, false);
				}

			}.runTaskLater(plugin, FLY_TICKS);
		}
	}
}
