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

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import de.rangun.pinkbull.IPinkBullPlugin;
import de.rangun.spiget.MessageRetriever;

/**
 * @author heiko
 *
 */
public final class JoinListener extends PinkBullListener {

	private final MessageRetriever msgs;

	/**
	 * @param plugin
	 */
	public JoinListener(IPinkBullPlugin plugin, final MessageRetriever msgs) {
		super(plugin);
		this.msgs = msgs;
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {

		plugin.setPlayerFlyAllowed(event.getPlayer(), false, null, false, -1);

		if (event.getPlayer().isOp()) {

			for (String jm : msgs.getJoinMessages()) {
				event.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.ITALIC + "["
						+ plugin.getDescription().getName() + ": " + jm + "]");
			}
		}
	}
}
