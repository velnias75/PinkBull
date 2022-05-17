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

package de.rangun.pinkbull.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import de.rangun.pinkbull.IPinkBullPlugin;

/**
 * @author heiko
 *
 */
public final class CommandFly implements CommandExecutor, TabCompleter {

	final IPinkBullPlugin plugin;

	/**
	 * 
	 */
	public CommandFly(final IPinkBullPlugin plugin) {
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

				final boolean flyAllowed = plugin.hasPlayerFlyAllowed(player) || player.getPersistentDataContainer()
						.get(plugin.getPinkBullPotionKey(), PersistentDataType.LONG) == -1L;

				player.getPersistentDataContainer().set(plugin.getPinkBullPotionKey(), PersistentDataType.LONG,
						flyAllowed ? 0L : -1L);

				plugin.setPlayerFlyAllowed(player, !flyAllowed, (Player) sender);

			} else {
				sender.sendMessage(plugin.getMessage("PinkBull_player_not_found", args[0]));
			}

		} else {
			sender.sendMessage(plugin.getMessage("PinkBull_only_player_exec_fly"));
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
