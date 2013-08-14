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

import java.io.File;

public class Main extends JavaPlugin implements Listener {
	FileConfiguration config;
	Integer cost;
	Integer maxdist;
	Boolean itp;
	public static ItemStack pearls;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		config = getConfig();
		if (!new File(this.getDataFolder().getPath() + File.separatorChar
				+ "config.yml").exists())
			saveDefaultConfig();

		cost = config.getInt("cost");
		maxdist = config.getInt("maxdistance");
		itp = config.getBoolean("itp");
		pearls = new ItemStack(Material.ENDER_PEARL, cost);
	}

	// For checking if a string is a number
	public boolean isNumber(String n) {
		if (n.matches("-?\\d+(\\.\\d+)?"))
			return true;
		else
			return false;
	}

	// For removing pearls from their inventory
	public void removePearls(Player player) {
		Material m = pearls.getType();
		int amount = pearls.getAmount();
		for (int c = 0; c < 36; c++) {
			ItemStack slot = player.getInventory().getItem(c);
			if (slot != null) {
				if (slot.getType() == m) {
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

	// Command function
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("ptp")) {
			Player player = Bukkit.getServer().getPlayer(sender.getName());
			// If the command sender is a player
			if (sender instanceof Player) {
				if (sender.hasPermission("pearltp.teleport")) {
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("home")) {
							//Check if the player currently has a bed spawn location
							if (player.getBedSpawnLocation() != null) {
								if (player.getInventory().contains(Material.ENDER_PEARL, cost)){
									removePearls(player);
									Location target = player.getBedSpawnLocation();
									//Initiate teleport
									player.teleport(target);
								}else{
									sender.sendMessage(ChatColor.RED
											+ "You need "
											+ cost.toString()
											+ " ender pearl(s) to teleport.");
								}
							} else {
								sender.sendMessage(ChatColor.RED
										+ ("No bed spawn is currently set. No tp."));
							}
						} else {
							if (itp) {
								if (Bukkit.getServer().getPlayer(args[0]) != null) {
									// If sender is not trying to teleport to themself
									if (Bukkit.getServer().getPlayer(sender.getName()) != Bukkit.getServer().getPlayer(args[0])) {
										if (player.getInventory().contains(Material.ENDER_PEARL, cost)) {
											removePearls(player);
											Location target = Bukkit.getServer().getPlayer(args[0]).getLocation();

											// Initiate teleport
											player.teleport(target);
										} else {
											sender.sendMessage(ChatColor.RED
													+ "You need "
													+ cost.toString()
													+ " ender pearl(s) to teleport.");
										}
									} else {
										sender.sendMessage(ChatColor.RED
												+ "You can't teleport to yourself.");
									}
								} else {
									sender.sendMessage("Unable to find player "
											+ args[0]);
								}
							} else {
								sender.sendMessage(ChatColor.RED
										+ "Teleporting to other players is not allowed.");
							}
						}
					} else if (args.length == 3) {
						//Check if all three arguments are numbers
						if (isNumber(args[0]) && isNumber(args[1]) && isNumber(args[2])) {
							if (player.getInventory().contains(Material.ENDER_PEARL, cost)) {
								removePearls(player);
								Location target = player.getLocation();

								// Update target coordinates
								target.setX(Double.parseDouble(args[0]));
								target.setY(Double.parseDouble(args[1]));
								target.setZ(Double.parseDouble(args[2]));

								// Intiate teleport
								player.teleport(target);
							} else {
								sender.sendMessage(ChatColor.RED + "You need "
										+ cost.toString()
										+ " ender pearl(s) to teleport.");
							}
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			} else {
				// If the command sender is console
				sender.sendMessage("Console can't use this command!");
			}
			return true;
		} else {
			return false;
		}
	}
}