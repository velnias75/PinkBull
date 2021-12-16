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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author heiko
 *
 */
@Plugin(name = "PinkBull", version = "0.0-SNAPSHOT")
@Description(value = "PinkBull verleiht Flügel!")
@Website(value = "https://github.com/velnias75/PinkBull")
@ApiVersion(Target.v1_17)
@Author(value = "Velnias75")
@Commands(@Command(name = "fly", desc = "Setzt oder nimmt Spieler in den bzw. aus dem Flugmodus", usage = "/fly [player]", permission = "pinkbull.fly"))
public final class PinkBullPlugin extends JavaPlugin {

	private final NamespacedKey PINK_BULL_POTION_KEY = new NamespacedKey(this, "pink_bull_potion");

	final static String PINK_BULL_TEXT = ChatColor.LIGHT_PURPLE + "Pink Bull" + ChatColor.RESET;
	final static long FLY_TICKS = 18000L;

	@Override
	public void onEnable() {

		PotionEffect effect = new PotionEffect(PotionEffectType.LEVITATION, 18000, 0);

		final ItemStack potion = new ItemStack(Material.POTION);
		final PotionMeta meta = (PotionMeta) potion.getItemMeta();
		final List<String> lore = new ArrayList<>();

		lore.add(PINK_BULL_TEXT + " verleiht " + ChatColor.BLUE + "Flüüüüüüügel!" + ChatColor.RESET);
		lore.add("");
		lore.add(ChatColor.RESET + "Dieser berauschende Trank verleiht");
		lore.add("Dir für " + ChatColor.BOLD + "15 Minuten" + ChatColor.RESET + " Flügel.");
		lore.add("");

		meta.setLore(lore);
		meta.setColor(Color.RED);
		meta.setDisplayName(PINK_BULL_TEXT);
		meta.addCustomEffect(effect, true);

		meta.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.BYTE, (byte) 1);

		potion.setItemMeta(meta);

		ShapedRecipe recipe = new ShapedRecipe(PINK_BULL_POTION_KEY, potion);

		recipe.shape("SSS", "SWS", "MMM");
		recipe.setIngredient('S', Material.SUGAR);
		recipe.setIngredient('W', Material.WATER_BUCKET);
		recipe.setIngredient('M', Material.MAGMA_CREAM);

		Bukkit.addRecipe(recipe);

		getServer().getPluginManager().registerEvents(new JoinListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);

		final CommandFly fly = new CommandFly(this);

		getCommand("fly").setExecutor(fly);
		getCommand("fly").setTabCompleter(fly);
	}

	@Override
	public void onDisable() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			setPlayerFlyAllowed(player, false);
		}
	}

	NamespacedKey getPinkBullPotionKey() {
		return PINK_BULL_POTION_KEY;
	}

	boolean hasPlayerFlyAllowed(final Player player) {
		return player.getPersistentDataContainer().has(PINK_BULL_POTION_KEY, PersistentDataType.LONG)
				&& player.getPersistentDataContainer().get(PINK_BULL_POTION_KEY, PersistentDataType.LONG) > 0L;
	}

	void setPlayerFlyAllowed(final Player player, boolean allow) {
		setPlayerFlyAllowed(player, allow, null);
	}

	void setPlayerFlyAllowed(final Player player, boolean allow, final Player donor) {

		final KeyedBossBar bar;

		if (allow) {

			final String msg = "" + ChatColor.AQUA + ChatColor.BOLD + player.getDisplayName() + ChatColor.RESET
					+ ChatColor.GREEN + " hat ";

			if (donor == null) {

				bar = getServer().createBossBar(createPlayerBossbarKey(player), PINK_BULL_TEXT, BarColor.PINK,
						BarStyle.SOLID);

				bar.setProgress(1.0d);
				bar.addPlayer(player);
				bar.setVisible(true);

				new BukkitRunnable() {

					@Override
					public void run() {

						final Long cur = player.getPersistentDataContainer().get(PINK_BULL_POTION_KEY,
								PersistentDataType.LONG);

						final double val = (double) (cur - 20L) / ((double) FLY_TICKS);
						bar.setProgress(val >= 0.0d ? val : 0.0d);

						player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
								cur < 20L ? 0L : (cur - 20L));

						if (!hasPlayerFlyAllowed(player)) {
							player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
									FLY_TICKS);
							this.cancel();
						}
					}

				}.runTaskTimer(this, 20L, 20L);

				getServer().broadcastMessage(
						msg + "ein " + PinkBullPlugin.PINK_BULL_TEXT + ChatColor.GREEN + " getrunken und kann nun "
								+ ChatColor.BOLD + "15 Minuten" + ChatColor.RESET + ChatColor.GREEN + " fliegen!");

			} else {

				getServer().broadcastMessage(msg + "von " + ChatColor.AQUA + ChatColor.BOLD + donor.getDisplayName()
						+ ChatColor.RESET + ChatColor.GREEN + " ein " + PINK_BULL_TEXT + ChatColor.GREEN
						+ "-Flugeffekt erhalten und kann nun fliegen!");
			}

		} else if (hasPlayerFlyAllowed(player)) {

			player.sendMessage(ChatColor.RED + "Du hast " + ChatColor.BOLD + "kein " + ChatColor.RESET + PINK_BULL_TEXT
					+ ChatColor.RED + "-Flugeffekt mehr!" + ChatColor.RESET);

			player.getLocation().getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 1);

			final NamespacedKey bosskey = createPlayerBossbarKey(player);

			bar = getServer().getBossBar(bosskey);

			if (bar != null) {

				bar.removePlayer(player);
				bar.setVisible(false);

				getServer().removeBossBar(bosskey);
			}
		}

		player.setAllowFlight(allow);
		player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG, allow ? FLY_TICKS : 0L);
	}

	private NamespacedKey createPlayerBossbarKey(final Player player) {
		return new NamespacedKey(this, "pink_bull_bossbar_" + player.getUniqueId().toString());
	}
}
