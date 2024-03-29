/*
 * Copyright 2021-2022 by Heiko Schäfer <heiko@rangun.de>
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

package de.rangun.pinkbull.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import de.rangun.pinkbull.IPinkBullPlugin;

/**
 * @author heiko
 *
 */
public final class PlayerItemConsumeListener extends PinkBullListener {

	/**
	 * @param plugin
	 */
	public PlayerItemConsumeListener(IPinkBullPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPlayerItemConsume(final PlayerItemConsumeEvent ev) {

		final ItemStack item = ev.getItem();

		if (item.getItemMeta().getPersistentDataContainer().getKeys().contains(plugin.getPinkBullPotionKey())) {

			final Player player = ev.getPlayer();
			final long duration = ((PotionMeta) item.getItemMeta()).getCustomEffects().get(0).getDuration();

			if (GameMode.CREATIVE.equals(player.getGameMode()) || GameMode.SPECTATOR.equals(player.getGameMode())) {
				ev.setCancelled(true);
				return;
			}

			ev.setItem(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.0f);

			if (!plugin.hasPlayerFlyAllowed(player)) {

				plugin.setPlayerFlyAllowed(player, true, duration);

			} else {

				player.sendMessage(plugin.getMessage("PinkBull_double_quaff"));
				plugin.setPlayerFlyAllowed(player, false);
			}
		}
	}
}
