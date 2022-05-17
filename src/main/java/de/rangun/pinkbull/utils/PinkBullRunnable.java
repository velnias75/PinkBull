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

package de.rangun.pinkbull.utils;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import de.rangun.pinkbull.IPinkBullPlugin;

/**
 * @author heiko
 *
 */
public abstract class PinkBullRunnable extends BukkitRunnable {

	protected final IPinkBullPlugin plugin;
	protected final Player player;

	/**
	 * 
	 */
	protected PinkBullRunnable(final IPinkBullPlugin plugin, final Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	protected abstract void action();

	@Override
	public final void run() {

		final Long cur = player.getPersistentDataContainer().get(plugin.getPinkBullPotionKey(),
				PersistentDataType.LONG);

		if (cur <= 0L) {
			this.cancel();
		} else {
			action();
		}

	}
}
