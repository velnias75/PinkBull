/*
 * Copyright 2022 by Heiko Schäfer <heiko@rangun.de>
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

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * @author heiko
 *
 */
public interface IPinkBullPlugin {

	NamespacedKey getPinkBullPotionKey();

	ItemStack createPinkBullPotion();

	ItemStack createPinkBullPotion(final int duration);

	boolean hasPlayerFlyAllowed(Player player);

	void setPlayerFlyAllowed(final Player player, final boolean allow);

	void setPlayerFlyAllowed(final Player player, final boolean allow, final long duration);

	void setPlayerFlyAllowed(final Player player, final boolean allow, final Player donor);

	void setPlayerFlyAllowed(final Player player, final boolean allow, final Player donor, final boolean flyEndMsg,
			long duration);

	String getMessage(final String key);

	String getMessage(final String key, final String string, final boolean isString);

	String getMessage(final String key, final String player);

	String getMessage(final String key, final Player player);

	String getMessage(final String key, final Player player, final long duration);

	String getMessage(final String key, final Player player, final Player donor);

	List<String> getJoinMessages();

	PluginDescriptionFile getDescription();
}
