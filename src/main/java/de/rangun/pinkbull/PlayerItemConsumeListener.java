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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author heiko
 *
 */
final class PlayerItemConsumeListener extends PinkBullListener {

	/**
	 * @param plugin
	 */
	protected PlayerItemConsumeListener(PinkBullPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerItemConsume(final PlayerItemConsumeEvent ev) {

		final ItemStack item = ev.getItem();

		if (item.getItemMeta().getPersistentDataContainer().getKeys().contains(plugin.getPinkBullPotionKey())) {

			Player player = ev.getPlayer();

			if (GameMode.CREATIVE.equals(player.getGameMode())) {
				return;
			}

			ev.setItem(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.0f);

			if (!plugin.hasPlayerFlyAllowed(player)) {

				plugin.setPlayerFlyAllowed(player, true);

			} else {

				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD
						+ "Doppelt Fliegen lohnt sich nicht, sonst fällst Du auf Dein Gesicht.");
				plugin.setPlayerFlyAllowed(player, false);

			}
		}
	}
}
