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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author heiko
 *
 */
final class CommandPinkBull implements CommandExecutor {

	final PinkBullPlugin plugin;

	/**
	 * 
	 */
	public CommandPinkBull(final PinkBullPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player) {

			final Player player = (Player) sender;
			player.getInventory().addItem(plugin.createPinkBullPotion());

		} else {
			sender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "/pinkbull" + ChatColor.RESET + ChatColor.YELLOW
					+ " kann nur von einem Spieler ausgeführt werden.");
		}

		return true;
	}

}
