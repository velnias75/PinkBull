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

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

/**
 * @author heiko
 *
 */
public final class CommandFly implements CommandExecutor, TabCompleter {

	final PinkBullPlugin plugin;

	/**
	 * 
	 */
	public CommandFly(final PinkBullPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player) {

			Player player = (Player) sender;

			if (args.length > 0) {
				player = Bukkit.getPlayer(args[0]);
			}

			if (player != null) {

				plugin.setPlayerFlyAllowed(player, !plugin.hasPlayerFlyAllowed(player), (Player) sender);

			} else {
				sender.sendMessage(ChatColor.RED + "Spieler " + ChatColor.AQUA + ChatColor.BOLD + args[0]
						+ ChatColor.RESET + ChatColor.RED + " nicht gefunden.");
			}

		} else {
			sender.sendMessage("Befehl kann nur von einem Spieler ausgeführt werden.");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		List<String> proposals = Lists.newArrayList();

		if (args.length == 1) {

			for (Player p : Bukkit.getOnlinePlayers()) {

				if (StringUtil.startsWithIgnoreCase(p.getName(), args[0])) {
					proposals.add(p.getName());
				}
			}
		}

		return proposals;
	}

}
