/* Copyright 2021-2022 by Heiko Schäfer <heiko@rangun.de>
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.ImmutableList;

import de.rangun.pinkbull.commands.CommandFly;
import de.rangun.pinkbull.commands.CommandPinkBull;
import de.rangun.pinkbull.listener.JoinListener;
import de.rangun.pinkbull.listener.PlayerChangedWorldListener;
import de.rangun.pinkbull.listener.PlayerItemConsumeListener;
import de.rangun.pinkbull.utils.PinkBullRunnable;
import de.rangun.spiget.PluginClient;
import github.scarsz.discordsrv.DiscordSRV;

/**
 * @author heiko
 *
 */
public final class PinkBullPlugin extends JavaPlugin implements IPinkBullPlugin {

	private final PluginClient spigetClient = new PluginClient(102050, getDescription().getVersion(),
			getDescription().getName(), getLogger());

	private final Enchantment PINKBULL_ENCHANTMENT = new EnchantmentWrapper("pinkbull");
	private final NamespacedKey PINK_BULL_POTION_KEY = new NamespacedKey(this, "pink_bull_potion");
	private FileConfiguration config = getConfig();
	private FileConfiguration messages = null;
	private Scoreboard sb = null;
	private boolean discordSRVavailable = false;

	private final static String PINK_BULL_TEXT = ChatColor.LIGHT_PURPLE + "Pink Bull" + ChatColor.RESET;

	@Override
	public void onEnable() {

		loadMessages();
		saveDefaultConfig();
		updateConfig();

		sb = Bukkit.getScoreboardManager().getMainScoreboard();

		final ShapedRecipe recipe = new ShapedRecipe(PINK_BULL_POTION_KEY, createPinkBullPotion());

		recipe.shape("SSS", "SWS", "MMM");
		recipe.setIngredient('S', Material.SUGAR);
		recipe.setIngredient('W', Material.WATER_BUCKET);
		recipe.setIngredient('M', Material.MAGMA_CREAM);

		Bukkit.addRecipe(recipe);

		discordSRVavailable = getServer().getPluginManager().getPlugin("DiscordSRV") != null;

		getServer().getPluginManager().registerEvents(new JoinListener(this, spigetClient), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);

		final CommandFly fly = new CommandFly(this);
		final CommandPinkBull pb = new CommandPinkBull(this);

		getCommand("fly").setExecutor(fly);
		getCommand("fly").setTabCompleter(fly);

		getCommand("pinkbull").setExecutor(pb);
		getCommand("pinkbull").setTabCompleter(pb);

		final int pluginId = 15208;
		new Metrics(this, pluginId);

		new BukkitRunnable() {

			@Override
			public void run() {
				spigetClient.checkVersion();
			}

		}.runTaskAsynchronously(this);
	}

	@Override
	public void onDisable() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			setPlayerFlyAllowed(player, false);
		}
	}

	@Override
	public NamespacedKey getPinkBullPotionKey() {
		return PINK_BULL_POTION_KEY;
	}

	@Override
	public ItemStack createPinkBullPotion() {
		return createPinkBullPotion((int) getFlyTicks());
	}

	@Override
	public ItemStack createPinkBullPotion(final int duration) {

		final PotionEffect effect = new PotionEffect(PotionEffectType.LEVITATION, duration, 0, false, false, false);
		final ItemStack potion = new ItemStack(Material.POTION);
		final PotionMeta meta = (PotionMeta) potion.getItemMeta();
		final List<String> lore = ImmutableList.of(getMessage("PinkBull_slogan"), "",
				getMessage("PinkBull_lore_duration_line_1"), getMessage("PinkBull_lore_duration_line_2", duration), "");

		meta.setLore(lore);
		meta.setColor(Color.fromRGB(255, 192, 203));
		meta.addEnchant(PINKBULL_ENCHANTMENT, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName(PINK_BULL_TEXT);
		meta.addCustomEffect(effect, true);
		meta.setCustomModelData(config.getInt("custom_model_data", 1));

		meta.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.BYTE, (byte) 1);

		potion.setItemMeta(meta);

		return potion;
	}

	@Override
	public boolean hasPlayerFlyAllowed(final Player player) {
		return player.getPersistentDataContainer().has(PINK_BULL_POTION_KEY, PersistentDataType.LONG)
				&& player.getPersistentDataContainer().get(PINK_BULL_POTION_KEY, PersistentDataType.LONG) > 0L;
	}

	@Override
	public void setPlayerFlyAllowed(final Player player, final boolean allow) {
		setPlayerFlyAllowed(player, allow, null, true, -1L);
	}

	@Override
	public void setPlayerFlyAllowed(final Player player, final boolean allow, final long duration) {
		setPlayerFlyAllowed(player, allow, null, true, duration);
	}

	@Override
	public void setPlayerFlyAllowed(final Player player, final boolean allow, final Player donor) {
		setPlayerFlyAllowed(player, allow, donor, true, -1L);
	}

	@Override
	public void setPlayerFlyAllowed(final Player player, final boolean allow, final Player donor,
			final boolean flyEndMsg, long duration) {

		final KeyedBossBar bar;

		if (GameMode.CREATIVE.equals(player.getGameMode())) {
			return;
		}

		if (allow) {

			if (donor == null && !Environment.NORMAL.equals(player.getWorld().getEnvironment())) {
				player.sendMessage(getMessage("PinkBull_dimension"));
				return;
			}

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

						final double val = (double) (cur - 20L)
								/ ((double) (duration == -1L ? getFlyTicks() : duration));

						bar.setProgress(val >= 0.0d ? val : 0.0d);

						player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
								cur < 20L ? 0L : (cur - 20L));

						if (!hasPlayerFlyAllowed(player)) {
							player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG, 0L);
							this.cancel();
						}

						if (cur == 400L) {

							player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
							player.sendMessage(getMessage("PinkBull_fly_end_warn"));
						}

						if (cur <= 20L) {

							removeBossBar(player, flyEndMsg);
							player.setAllowFlight(false);
							this.cancel();
						}
					}

				}.runTaskTimer(this, 20L, 20L);

				final String pbQuaffed = getMessage("Pinkbull_quaffed", player, duration);

				getServer().broadcastMessage(pbQuaffed);

				if (discordSRVavailable) {
					sendToDiscordSRV(pbQuaffed, player);
				}

			} else {

				if (!donor.equals(player)) {
					getServer().broadcastMessage(getMessage("PinkBull_donor", player, donor));
				} else {
					player.sendMessage(getMessage("PinkBull_effect"));
				}
			}

		} else if (hasPlayerFlyAllowed(player)) {
			removeBossBar(player, flyEndMsg);
		} else if (!allow && flyEndMsg) {
			flyEndMessage(player);
		}

		player.setAllowFlight(allow);
		player.getPersistentDataContainer().set(PINK_BULL_POTION_KEY, PersistentDataType.LONG,
				allow ? (donor == null ? (duration == -1L ? getFlyTicks() : duration) : -1L) : 0L);
	}

	private void sendToDiscordSRV(final String message, Player player) {

		if (discordSRVavailable) {

			DiscordSRV.getPlugin().processChatMessage(player, message, DiscordSRV.getPlugin().getMainChatChannel(),
					false, null);

			// WebhookUtil.deliverMessage(DiscordSRV.getPlugin().getMainTextChannel(),
			// player, message);
		}
	}

	private void flyEndMessage(final Player player) {

		player.sendMessage(getMessage("PinkBull_fly_end"));
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

	@Override
	public String getMessage(final String key) {
		return getMessage(key, null, null, null, -1L, null);
	}

	@Override
	public String getMessage(final String key, final String string, final boolean isString) {
		return getMessage(key, null, null, null, -1L, string);
	}

	private String getMessage(final String key, final long duration) {
		return getMessage(key, null, null, null, duration, null);
	}

	@Override
	public String getMessage(final String key, final String player) {
		return getMessage(key, null, null, player, -1L, null);
	}

	@Override
	public String getMessage(final String key, final Player player) {
		return getMessage(key, player, null, null, -1L, null);
	}

	@Override
	public String getMessage(final String key, final Player player, final long duration) {
		return getMessage(key, player, null, null, duration, null);
	}

	@Override
	public String getMessage(final String key, final Player player, final Player donor) {
		return getMessage(key, player, donor, null, -1L, null);
	}

	private String getMessage(final String key, final Player player, final Player donor, final String player_s,
			long duration, final String string) {
		return (messages.getString(key) + ChatColor.RESET).replace("\\n", "\n").replace("{PinkBull}", PINK_BULL_TEXT)
				.replace("{fly_ticks}", flyTicksToMinutes(duration))
				.replace("{string}", string == null ? "<null>" : string)
				.replace("{player}",
						player == null ? (player_s == null ? "<null>" : player_s)
								: getTeamFormattedPlayerDisplayName(player))
				.replace("{donor}", donor == null ? "<null>" : getTeamFormattedPlayerDisplayName(donor));
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

	private String flyTicksToMinutes(final long duration) {

		final long minutes = (duration == -1L ? getFlyTicks() : duration) / (20L * 60L);
		final long seconds = ((duration == -1L ? getFlyTicks() : duration) - (minutes * 20L * 60L)) / 20L;

		return String.format("%d%s", minutes, seconds != 0L ? String.format(":%02d", seconds) : "");
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		config = getConfig();
		loadMessages();
	}

	private void updateConfig() {

		try {

			final FileConfiguration jarConf = YamlConfiguration.loadConfiguration(this.getTextResource("config.yml"));

			boolean changed = false;

			for (final String key : jarConf.getKeys(true)) {

				if (!config.contains(key, true)) {
					config.set(key, jarConf.get(key));
					changed = true;
				}
			}

			if (changed) {
				config.save(new File(getDataFolder() + "/config.yml"));
				Bukkit.getLogger().warning("[" + getName()
						+ "] Configuration updated. Please read the documention for information on the new options.");
			}

		} catch (IllegalArgumentException | IOException e) {
			getLogger().log(Level.WARNING, "Could not update config", e);
		}
	}

	private void loadMessages() {

		Reader messageStream = null;

		try {

			InputStream is = this.getResource("messages_" + config.getString("language", "en") + ".yml");

			if (is == null) {
				is = this.getResource("messages_en.yml");
				Bukkit.getLogger().warning("[" + getName() + "] Language \"" + config.getString("language")
						+ "\" not supported, falling back to \"en\".");
			}

			messageStream = new InputStreamReader(is, "UTF8");

		} catch (UnsupportedEncodingException e) {
		}

		if (messageStream != null) {
			messages = YamlConfiguration.loadConfiguration(messageStream);
		}
	}
}
