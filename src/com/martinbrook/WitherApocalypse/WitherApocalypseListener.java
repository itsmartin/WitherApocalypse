package com.martinbrook.WitherApocalypse;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class WitherApocalypseListener implements Listener {

	private WitherApocalypse plugin;
	
	public WitherApocalypseListener(WitherApocalypse plugin) {
		this.plugin = plugin;
	}

	/**
	 * Ensure that biomes are kept updated at all times
	 */
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		plugin.updateBiome(e.getChunk());
	}
	
	/**
	 * The apocalypse begins when you spawn a wither
	 * 
	 * @param e
	 */
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getEntityType() == EntityType.WITHER) {
			plugin.beginApocalypse();
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.WITHER ||
				(e.getEntity() instanceof Player && plugin.isWither((Player) e.getEntity()))) {
			// Wither, or wither player killed
			
			ItemStack potionDrops = new Potion(PotionType.WATER).toItemStack(10);
			ItemMeta im = potionDrops.getItemMeta();
			im.setDisplayName("Potion of Withering");
			potionDrops.setItemMeta(im);
			
			e.getDrops().add(potionDrops);
			
			e.setDroppedExp(1000);
			
		}
	}
	
	/**
	 * Handle players becoming withers
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
		if (!plugin.isApocalypse()) return;
		ItemMeta im = e.getItem().getItemMeta();
		
		if (plugin.isWither(e.getPlayer())) return;
		if (im.hasDisplayName() && im.getDisplayName().equalsIgnoreCase("Potion of Withering"))
			plugin.setWither(e.getPlayer(), true);
	}
	
	
	/**
	 * Play sounds when skulls hit
	 * 
	 * @param e
	 */
	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent e) {
		if (!plugin.isApocalypse()) return;
		
		if (e.getEntityType() == EntityType.WITHER_SKULL) {
			LivingEntity shooter = (e.getEntity().getShooter());
			if (shooter != null && shooter instanceof Player) {
				Location l = e.getEntity().getLocation();
				l.getWorld().playSound(l, Sound.WITHER_HURT, 10, 1);
			}
		}
	}
	
	/**
	 * Turn snowballs into wither skulls
	 * 
	 * @param e
	 */
	@EventHandler
	public void onProjectileLaunchEvent(ProjectileLaunchEvent e) {
		if (!plugin.isApocalypse()) return;
		LivingEntity shooter = (e.getEntity().getShooter());
		if (shooter != null && shooter instanceof Player && plugin.isWither((Player) shooter)) {
			if (e.getEntityType() == EntityType.SNOWBALL) {
				e.setCancelled(true);
				shooter.launchProjectile(WitherSkull.class);
			}
		}
	}
	
	/**
	 * Increase the potency of wither skulls
	 */
	@EventHandler
	public void onExplosionPrimeEvent(ExplosionPrimeEvent e) {
		if (!plugin.isApocalypse()) return;

		if (e.getEntity().getType() == EntityType.WITHER_SKULL) {
			Projectile skull = (Projectile) e.getEntity();
			if (skull.getShooter() instanceof Player) {
				e.setRadius(10);
			}
		}
	}
	
	/**
	 * Remove drops from wither skull explosions
	 */
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		if (!plugin.isApocalypse()) return;

		if (e.getEntityType() == EntityType.WITHER_SKULL) {
			e.setYield(0);
		}
	}
	
	/**
	 * Remove fall damage for wither players, and wither damage for all players
	 * 
	 * @param event
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!plugin.isApocalypse()) return;

		if (!(e.getEntity() instanceof Player))
			return;
		if (e.getCause() == DamageCause.FALL) {
			if (e.getEntity() instanceof Player && plugin.isWither((Player) e.getEntity()))
				e.setCancelled(true);
		}
		if (e.getCause() == DamageCause.WITHER) {
			e.setCancelled(true);
			((LivingEntity)e.getEntity()).removePotionEffect(PotionEffectType.WITHER);
		}
	}
	
	/**
	 * Prevent wither players from catching fire
	 * 
	 * @param e
	 */
	@EventHandler
	public void onEntityCombust(EntityCombustEvent e) {
		if (!plugin.isApocalypse()) return;

		if (!(e.getEntity() instanceof Player))
			return;
		
		if (plugin.isWither((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Revert players to normal when they die
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		plugin.setWither(e.getEntity(),false);
	}




	/**
	 * Wither players can double-jump
	 * 
	 * @param event
	 */
	@EventHandler
    public void onFlightAttempt(PlayerToggleFlightEvent event) {
		if (!plugin.isApocalypse()) return;
 
        Player p = event.getPlayer();
        
        if (plugin.isWither(p)) {
	        p.playSound(p.getLocation(), Sound.WITHER_IDLE, 10, 1);
	        event.setCancelled(true);
	        Vector v = p.getLocation().getDirection().multiply(1).setY(1.5);
	        p.setVelocity(v);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 10), true);
        }
    }
	
	/**
	 * Adjust speed when sprinting
	 * 
	 * @param e
	 */
	@EventHandler
	public void onToggleSprint(PlayerToggleSprintEvent e) {
		if (!plugin.isApocalypse()) return;
		
		Player p = e.getPlayer();
		
		if (plugin.isWither(p)) {
			if (e.isSprinting()) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 10), true);
			} else {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
			}
		}
	}
	
	

}
