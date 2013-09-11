package com.martinbrook.TesseractApocalypse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class TesseractApocalypseListener implements Listener {

	private TesseractApocalypse plugin;
	
	public TesseractApocalypseListener(TesseractApocalypse plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent e) {
		
		Location l = e.getEntity().getLocation();
		l.getWorld().playSound(l, Sound.WITHER_HURT, 10, 1);
	}
	
	@EventHandler
	public void onProjectileLaunchEvent(ProjectileLaunchEvent e) {
		LivingEntity shooter = (e.getEntity().getShooter());
		if (shooter != null && shooter instanceof Player) {
			if (e.getEntityType() == EntityType.SNOWBALL) {
				e.setCancelled(true);
				shooter.launchProjectile(WitherSkull.class);
			}
		}
	}
	
	@EventHandler
	public void onExplosionPrimeEvent(ExplosionPrimeEvent e) {
		if (e.getEntity().getType() == EntityType.WITHER_SKULL) {
			e.setFire(true);
			e.setRadius(10);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
		if (event.getCause() == DamageCause.WITHER) {
			event.setCancelled(true);
			((LivingEntity)event.getEntity()).removePotionEffect(PotionEffectType.WITHER);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				equip(e.getPlayer());
			}

		});
		
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		equip(e.getPlayer());
	}
	
	
	@EventHandler
    public void onFlightAttempt(PlayerToggleFlightEvent event) {
 
        Player p = event.getPlayer();   
        p.playSound(p.getLocation(), Sound.IRONGOLEM_THROW, 10, -10);
        event.setCancelled(true);
        Vector v = p.getLocation().getDirection().multiply(1).setY(1.5);
        p.setVelocity(v);
 
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
			armor[n].addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
		}
		
		i.setArmorContents(armor);
		
		i.addItem(new ItemStack(Material.SNOW_BALL, 64));
		i.addItem(new ItemStack(Material.SNOW_BALL, 64));
		i.addItem(new ItemStack(Material.SNOW_BALL, 64));
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 20), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3), true);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 10), true);
		p.setAllowFlight(true);
	}
	
}
