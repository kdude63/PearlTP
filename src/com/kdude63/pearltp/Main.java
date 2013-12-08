package com.kdude63.pearltp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
	Logger logger = Logger.getLogger("Minecraft");
	FileConfiguration config;

	Integer cost;
	Integer maxdist;

	Boolean p2p;
	Boolean p2c;
	Boolean p2b;

	public static ItemStack pearls;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		config = getConfig();
		if (!new File(this.getDataFolder().getPath() + File.separatorChar
				+ "config.yml").exists())
			saveDefaultConfig();

		// Load values from configuration
		cost = config.getInt("cost");
		maxdist = config.getInt("maxdistance");

		p2p = config.getBoolean("teleport.playertoplayer");
		p2c = config.getBoolean("teleport.playertocoordinates");
		p2b = config.getBoolean("teleport.playertobed");


		// We only need to initialize this once
		pearls = new ItemStack(Material.ENDER_PEARL, cost);

		// Start metrics
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	// Remove pearls from the player's inventory
	public void removePearls(Player player) {
		int amount = cost;
		for (int c = 0; c < 36; c++) {
			ItemStack slot = player.getInventory().getItem(c);
			if (slot != null) {
				if (slot.getType() == Material.ENDER_PEARL) {
					if (slot.getAmount() > amount) {
						slot.setAmount(slot.getAmount() - amount);
						return;
					} else {
						amount -= slot.getAmount();
						player.getInventory().clear(c);
					}
				}
			}
		}
	}

	// Initiate teleport 'n shit
	public void initTeleport(Player origin, Location target) {
		Double dist = origin.getLocation().distance(target);

		if (dist < maxdist) {
			origin.teleport(target);
			removePearls(origin);
		} else {
			origin.sendMessage(ChatColor.RED + "Target is too far away! You need to be " + dist.intValue() +  " blocks closer.");
		}
	}

	// Command function
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ptp")) {
			// If the command sender is a player
			if (sender instanceof Player) {
				if (sender.hasPermission("pearltp.teleport")) {
					if (args.length == 1) {
						if (!args[0].equalsIgnoreCase("home")) {
							if (p2p) {
								Player playerFrom = Bukkit.getServer().getPlayer(sender.getName());
								Player playerTo = Bukkit.getServer().getPlayer(args[0]);

								if (playerTo != null) {
									initTeleport(playerFrom, playerTo.getLocation());
								} else {
									sender.sendMessage(ChatColor.RED + "Could not find player " + args[0] + ".");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Player teleportation is disabled.");
							}
						} else {
							Player playerFrom = Bukkit.getServer().getPlayer(sender.getName());
							Location target = playerFrom.getBedSpawnLocation();

							if (p2b) {
								if (target != null) {
									target.setY(target.getY() + 1);
									initTeleport(playerFrom, target);
								} else {
									sender.sendMessage(ChatColor.RED + "Bed not set. No TP.");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Bed teleportation is disabled.");
							}
						}
					} else if (args.length == 3) {
						if (p2c) {
							if ((args[0]+args[1]+args[2]).matches("-?\\d+(\\.\\d+)?")) {

								Player playerFrom = Bukkit.getServer().getPlayer(sender.getName());

								Location target = Bukkit.getServer().getPlayer(sender.getName()).getLocation();
								target.setX(Double.parseDouble(args[0]));
								target.setY(Double.parseDouble(args[1]));
								target.setZ(Double.parseDouble(args[2]));

								initTeleport(playerFrom, target);
							} else {
								return false;
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Coordinate teleportation is disabled.");
						}
					} else {
						return false;
					}
				}
			}
		} else {
			// If the command sender is console
			sender.sendMessage("Console can't use this command!");
		}
		return true;
	}
}
