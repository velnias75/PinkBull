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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import de.rangun.pinkbull.IPinkBullPlugin;

/**
 * @author heiko
 *
 */
public final class CommandPinkBull implements CommandExecutor, TabCompleter {

	final IPinkBullPlugin plugin;

	public CommandPinkBull(final IPinkBullPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label,
			final String[] args) {

		if (args.length > 0 && "reload".equalsIgnoreCase(args[0]) && sender.hasPermission("pinkbull.reload")) {

			plugin.reloadConfig();
			sender.sendMessage(plugin.getMessage("PinkBull_reloaded"));

		} else {

			if (sender instanceof Player) {

				ItemStack is;

				if (args.length > 0) {

					try {

						final int duration = Math.min(Integer.parseInt(args[0]), Integer.MAX_VALUE / 20) * 20;

						if (duration < 20) {
							sender.sendMessage(plugin.getMessage("PinkBull_wrong_duration"));
							is = plugin.createPinkBullPotion();
						} else {
							is = plugin.createPinkBullPotion(duration);
						}

					} catch (NumberFormatException e) {
						is = plugin.createPinkBullPotion();
						sender.sendMessage(plugin.getMessage("PinkBull_invalid_integer", args[0], true));
					}

				} else {
					is = plugin.createPinkBullPotion();
				}

				final Player player = (Player) sender;

				player.getInventory().addItem(is);

			} else {
				sender.sendMessage(plugin.getMessage("PinkBull_only_player_exec_pinkbull"));
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		return args.length == 1 ? ImmutableList.of("reload") : ImmutableList.of();
	}
}
