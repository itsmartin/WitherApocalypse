package com.martinbrook.WitherApocalypse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitherApocalypse extends JavaPlugin {

	private boolean enabled;
	private Biome globalBiome = null;
	private World world;
	private Set<String> witherPlayers = new HashSet<String>();
	private Map<String, Integer> playerChargeTime = new HashMap<String, Integer>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new WitherApocalypseListener(this), this);
		this.enabled = false;
		world = getServer().getWorld("world");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("tesseractapocalypse.apocalypse") && command.getName().equalsIgnoreCase("apocalypse")) {
			boolean newStatus = !enabled;
			if (newStatus) {
				sender.sendMessage(ChatColor.RED + "Apocalypse");
				beginApocalypse();
			} else {
				sender.sendMessage(ChatColor.GREEN + "Nopocalypse");
				endApocalypse();
			}
		}
		
		return true;
	}
	
	public boolean isApocalypse() {
		return this.enabled;
	}
	
	public void beginApocalypse() {
		if (enabled) return;
		enabled=true;
		world.strikeLightning(world.getSpawnLocation());
		setGlobalBiome(Biome.SKY);
		world.setTime(0);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				apocalypseTasks();
			}
			
		}, 10L, 10L);
	}
	public void endApocalypse() {
		if (!enabled) return;
		enabled=false;
		setGlobalBiome(Biome.DESERT);
		world.setTime(0);
	}
	
	public void setGlobalBiome(Biome biome) {
		globalBiome = biome;
		for (Chunk c : world.getLoadedChunks()) {
			updateBiome(c);
			world.refreshChunk(c.getX(), c.getZ());
		}
	}
	
	public void updateBiome(Chunk c) {
		if (globalBiome != null) {
			for(int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					c.getBlock(x,0,z).setBiome(globalBiome);
				}
			}
		}
	}
	
	public void setWither(Player p, boolean value) {
		if (value) {
			witherPlayers.add(p.getName().toLowerCase());
			getServer().broadcastMessage(ChatColor.RED + p.getDisplayName() + " became a Wither");
			equip(p);
			world.playSound(p.getLocation(), Sound.WITHER_SPAWN, 30, 1);
		}
		else {
			witherPlayers.remove(p.getName().toLowerCase());
			for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
		}
		
		p.setAllowFlight(value);
	}
	
	public boolean isWither(Player p) {
		return witherPlayers.contains(p.getName().toLowerCase());
	}
	
	public void equip(Player p) {
		PlayerInventory i = p.getInventory();
		
		ItemStack[] armor = new ItemStack[] {
				new ItemStack(Material.DIAMOND_BOOTS),
				new ItemStack(Material.DIAMOND_LEGGINGS),
				new ItemStack(Material.DIAMOND_CHESTPLATE),
				new ItemStack(Material.DIAMOND_HELMET)
		};

		for(int n=0; n < 4; n++) {
			armor[n].addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 2);
		}
		
		i.setArmorContents(armor);
		
		i.addItem(new ItemStack(Material.SNOW_BALL, 32));
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 4), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
	}
	
	private void apocalypseTasks() {
		for (Player p : getServer().getOnlinePlayers()) {
			if (isWither(p)) {
				// For wither players, slow them down if they are on the ground not sprinting
				if (((Entity)p).isOnGround() && !(p.isSprinting()))
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
			} else {
				if (((Entity)p).isOnGround()
						&& p.getLocation().getBlock().getState().getType() == Material.ENDER_PORTAL_FRAME) {
					
					// Player is standing on an end portal block
					Integer ct = playerChargeTime.get(p.getName());
					if (ct == null) {
						playerChargeTime.put(p.getName(), 0);
						world.playSound(p.getLocation(), Sound.FIZZ, 30, 1);

					} else {
						ct += 10;
						if (ct >= 80) {
							playerChargeTime.remove(p.getName());
							setWither(p, true);
						} else {
							if (ct % 20 == 0) {
								world.playSound(p.getLocation(), Sound.ORB_PICKUP, 5, 10);
							}
							playerChargeTime.put(p.getName(), ct);
						}
					}
				} else {
					playerChargeTime.remove(p.getName());
				}
			}
		}
	}

}
