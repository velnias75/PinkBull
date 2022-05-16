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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion.Target;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * @author heiko
 *
 */
@Plugin(name = "PinkBull", version = "0.0-SNAPSHOT")
@Description(value = "PinkBull verleiht Flügel!")
@Website(value = "https://github.com/velnias75/PinkBull")
@ApiVersion(Target.v1_17)
@Author(value = "Velnias75")
@Command(name = "fly", desc = "Setzt oder nimmt Spieler in den bzw. aus dem Flugmodus", usage = "/fly [player]", permission = "pinkbull.fly")
@Command(name = "pinkbull", desc = "Gibt dem Spieler einen PinkBull-Trank", usage = "/pinkbull", permission = "pinkbull.pinkbull")
public final class PinkBullPlugin extends JavaPlugin {

	private final static String PINK_BULL_TEXT = ChatColor.LIGHT_PURPLE + "Pink Bull" + ChatColor.RESET;

	private final NamespacedKey PINK_BULL_POTION_KEY = new NamespacedKey(this, "pink_bull_potion");
	private final FileConfiguration config = getConfig();
	private Scoreboard sb = null;

	@Override
	public void onEnable() {

		registerGlow();

		config.addDefault("fly_ticks", 18000L);
		config.options().copyDefaults(true);
		saveConfig();

		sb = Bukkit.getScoreboardManager().getMainScoreboard();

		final ShapedRecipe recipe = new ShapedRecipe(PINK_BULL_POTION_KEY, createPinkBullPotion());

		recipe.shape("SSS", "SWS", "MMM");
		recipe.setIngredient('S', Material.SUGAR);
		recipe.setIngredient('W', Material.WATER_BUCKET);
		recipe.setIngredient('M', Material.MAGMA_CREAM);

		Bukkit.addRecipe(recipe);

		getServer().getPluginManager().registerEvents(new JoinListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);

		final CommandFly fly = new CommandFly(this);
		final CommandPinkBull pb = new CommandPinkBull(this);

		getCommand("fly").setExecutor(fly);
		getCommand("fly").setTabCompleter(fly);

		getCommand("pinkbull").setExecutor(pb);

		final int pluginId = 15208;
		new Metrics(this, pluginId);
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

	ItemStack createPinkBullPotion() {

		final PotionEffect effect = new PotionEffect(PotionEffectType.LEVITATION, (int) getFlyTicks(), 0);
		final ItemStack potion = new ItemStack(Material.POTION);
		final PotionMeta meta = (PotionMeta) potion.getItemMeta();
		final Glow glow = new Glow(new NamespacedKey(this, getDescription().getName()));
		final List<String> lore = new ArrayList<>();

		lore.add(PINK_BULL_TEXT + " verleiht " + ChatColor.BLUE + "Flüüüüüüügel!" + ChatColor.RESET);
		lore.add("");
		lore.add(ChatColor.RESET + "Dieser berauschende Trank verleiht");
		lore.add(ChatColor.RESET + "Dir für " + ChatColor.BOLD + flyTicksToMinutes() + " Minuten" + ChatColor.RESET
				+ " Flügel.");
		lore.add("");

		meta.setLore(lore);
		meta.setColor(Color.RED);
		meta.addEnchant(glow, 1, true);
		meta.setDisplayName(PINK_BULL_TEXT);
		meta.addCustomEffect(effect, true);

		meta.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.BYTE, (byte) 1);

		potion.setItemMeta(meta);

		return potion;
	}

	boolean hasPlayerFlyAllowed(final Player player) {
		return player.getPersistentDataContainer().has(PINK_BULL_POTION_KEY, PersistentDataType.LONG)
				&& player.getPersistentDataContainer().get(PINK_BULL_POTION_KEY, PersistentDataType.LONG) > 0L;
	}

	void setPlayerFlyAllowed(final Player player, boolean allow) {
		setPlayerFlyAllowed(player, allow, null, true);
	}

	void setPlayerFlyAllowed(final Player player, boolean allow, final Player donor) {
		setPlayerFlyAllowed(player, allow, donor, true);
	}

	void setPlayerFlyAllowed(final Player player, boolean allow, final Player donor, final boolean flyEndMsg) {

		final KeyedBossBar bar;

		if (GameMode.CREATIVE.equals(player.getGameMode())) {
			return;
		}

		if (allow) {

			if (donor == null && !Environment.NORMAL.equals(player.getWorld().getEnvironment())) {

				player.sendMessage("" + ChatColor.RED + ChatColor.ITALIC + "Sorry mein Freund," + ChatColor.RESET
						+ ChatColor.RED + " Dein " + PINK_BULL_TEXT + ChatColor.RED
						+ " hat außerhalb der Oberwelt keinen Effekt!");
				return;
			}

			final String msg = getTeamFormattedPlayerDisplayName(player) + ChatColor.GREEN + " hat ";

			if (donor == null) {

				bar = getServer().createBossBar(createPlayerBossbarKey(player), PINK_BULL_TEXT, BarColor.PINK,
						BarStyle.SOLID);

				bar.setProgress(1.0d);
				bar.addPlayer(player);
				bar.setVisible(true);

				new PinkBullRunnable(this, player) {

					@Override
					public void action() {

						final Long cur = player.getPersistentDataContainer().get(PINK_BULL_POTION_KEY,
								PersistentDataType.LONG);

						final double val = (double) (cur - 20L) / ((double) getFlyTicks());
						bar.setProgress(val >= 0.0d ? val : 0.0d);

						player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
								cur < 20L ? 0L : (cur - 20L));

						if (!hasPlayerFlyAllowed(player)) {
							player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG, 0L);
							this.cancel();
						}

						if (cur == 400L) {

							player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);

							player.sendMessage(ChatColor.RED + "Dein " + ChatColor.BOLD + PinkBullPlugin.PINK_BULL_TEXT
									+ ChatColor.RED + "-Flugmodus wird " + ChatColor.BOLD + "in Kürze" + ChatColor.RESET
									+ ChatColor.RED + " beendet." + ChatColor.RESET);
						}

						if (cur <= 20L) {

							removeBossBar(player, flyEndMsg);
							player.setAllowFlight(false);
							this.cancel();
						}
					}

				}.runTaskTimer(this, 20L, 20L);

				getServer().broadcastMessage(msg + "ein " + PinkBullPlugin.PINK_BULL_TEXT + ChatColor.GREEN
						+ " getrunken und kann nun " + ChatColor.BOLD + flyTicksToMinutes() + " Minuten"
						+ ChatColor.RESET + ChatColor.GREEN + " fliegen!");

			} else {

				if (!donor.equals(player)) {
					getServer().broadcastMessage(
							msg + "von " + getTeamFormattedPlayerDisplayName(donor) + ChatColor.GREEN + " ein "
									+ PINK_BULL_TEXT + ChatColor.GREEN + "-Flugeffekt erhalten und kann nun fliegen!");
				} else {
					player.sendMessage(
							ChatColor.GREEN + "Du hast nun den " + PINK_BULL_TEXT + ChatColor.GREEN + "-Effekt.");
				}
			}

		} else if (hasPlayerFlyAllowed(player)) {
			removeBossBar(player, flyEndMsg);
		} else if (!allow && flyEndMsg) {
			flyEndMessage(player);
		}

		player.setAllowFlight(allow);
		player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
				allow ? (donor == null ? getFlyTicks() : -1L) : 0L);
	}

	private void flyEndMessage(final Player player) {
		player.sendMessage(ChatColor.RED + "Du hast " + ChatColor.BOLD + "kein " + ChatColor.RESET + PINK_BULL_TEXT
				+ ChatColor.RED + "-Flugeffekt mehr!" + ChatColor.RESET);

		player.getLocation().getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 1);
	}

	private void removeBossBar(final Player player, final boolean flyEndMsg) {

		if (flyEndMsg) {
			flyEndMessage(player);
		}

		final NamespacedKey bosskey = createPlayerBossbarKey(player);
		final KeyedBossBar bossbar = getServer().getBossBar(bosskey);

		if (bossbar != null) {

			bossbar.removePlayer(player);
			bossbar.setVisible(false);

			getServer().removeBossBar(bosskey);
		}
	}

	private NamespacedKey createPlayerBossbarKey(final Player player) {
		return new NamespacedKey(this, "pink_bull_bossbar_" + player.getUniqueId().toString());
	}

	private void registerGlow() {

		try {

			final Field f = Enchantment.class.getDeclaredField("acceptingNew");

			f.setAccessible(true);
			f.set(null, true);

		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception in registering Glow", e);
		}

		try {

			final NamespacedKey key = new NamespacedKey(this, getDescription().getName());
			final Glow glow = new Glow(key);

			Enchantment.registerEnchantment(glow);

		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "Exception in registering Glow", e);
		}
	}

	private String getTeamFormattedPlayerDisplayName(final Player player) {

		if (sb != null) {

			final Team team = sb.getEntryTeam(player.getName());

			if (team != null) {
				return "" + team.getColor() + ChatColor.BOLD + team.getPrefix() + player.getDisplayName()
						+ team.getSuffix() + ChatColor.RESET;
			}
		}

		return "" + ChatColor.AQUA + ChatColor.BOLD + player.getDisplayName() + ChatColor.RESET;
	}

	private long getFlyTicks() {
		return Math.max(800L, config.getLong("fly_ticks"));
	}

	private String flyTicksToMinutes() {

		final long minutes = getFlyTicks() / (20L * 60L);
		final long seconds = (getFlyTicks() - (minutes * 20L * 60L)) / 20L;

		return String.format("%d%s", minutes, seconds != 0L ? String.format(":%02d", seconds) : "");
	}
}
