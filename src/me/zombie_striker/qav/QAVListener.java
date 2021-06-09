package me.zombie_striker.qav;

import me.zombie_striker.qav.api.QualityArmoryVehicles;
import me.zombie_striker.qav.api.events.VehicleDamageEvent;
import me.zombie_striker.qav.api.events.VehicleDestroyEvent;
import me.zombie_striker.qav.menu.MenuHandler;
import me.zombie_striker.qav.qamini.ParticleHandlers;
import me.zombie_striker.qav.vehicles.AbstractCar;
import me.zombie_striker.qav.vehicles.AbstractHelicopter;
import me.zombie_striker.qav.vehicles.AbstractPlane;
import me.zombie_striker.qav.vehicles.AbstractVehicle;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class QAVListener implements Listener {

	private Main main;

	public QAVListener(Main main) {
		this.main = main;
	}

	@EventHandler
	public void onClickVehicle(PlayerInteractEvent e) {
		if (e.getPlayer().getVehicle() == null) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				VehicleEntity ve = QualityArmoryVehicles.getVehiclePlayerLookingAt(e.getPlayer());
				if (ve != null) {
					if (e.getPlayer().isSneaking()) {
						MenuHandler.openOverview(e.getPlayer(), ve);
					} else {
						ve.getDriverSeat().setPassenger(e.getPlayer());
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlace(PlayerInteractEvent e) {
		if(e.getHand() == EquipmentSlot.OFF_HAND)
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (QualityArmoryVehicles.isVehicleByItem(e.getItem())) {
				AbstractVehicle vehicle = QualityArmoryVehicles.getVehicleByItem(e.getItem());
				VehicleEntity ve = new VehicleEntity(vehicle, e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), e.getPlayer().getUniqueId());
				ve.spawn();

				if(e.getPlayer().getGameMode() != GameMode.CREATIVE){
					e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
				}
			}
		}
	}

	@EventHandler
	public void oninteractEntity(PlayerInteractEntityEvent e) {
		if (e.getPlayer().getVehicle() == null) {
			VehicleEntity ve = QualityArmoryVehicles.getVehicleEntityByEntity(e.getRightClicked());
			if (ve != null) {
				e.setCancelled(true);
				if (e.getPlayer().isSneaking()) {
					MenuHandler.openOverview(e.getPlayer(), ve);
				} else {
					ve.getDriverSeat().setPassenger(e.getPlayer());
					return;
				}
			}
		}
	}

	@EventHandler
	public void onManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getPlayer().getVehicle() == null) {
			VehicleEntity ve = QualityArmoryVehicles.getVehicleEntityByEntity(e.getRightClicked());
			if (ve != null) {
				e.setCancelled(true);
				if (e.getPlayer().isSneaking()) {
					MenuHandler.openOverview(e.getPlayer(), ve);
				} else {
					ve.getDriverSeat().setPassenger(e.getPlayer());
					return;
				}
			}
		}
	}


	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (Main.enableVehiclePlayerCollision)
			for (Entity ent : e.getPlayer().getNearbyEntities(10, 10, 10)) {
				if (QualityArmoryVehicles.isVehicle(ent)) {
					VehicleEntity ve = QualityArmoryVehicles.getVehicleEntityByEntity(ent);
					if (ve != null) {
						if (QualityArmoryVehicles.isWithinVehicle(e.getTo(), ve)
								&& !QualityArmoryVehicles.isWithinVehicle(e.getFrom(), ve)) {
							if (e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) {
								if (e.getPlayer().getVelocity().getY() < -0.05) {
									e.getPlayer().setVelocity(e.getPlayer().getVelocity().setY(0.3));
								}
							}
							e.setCancelled(true);
							break;

						} else {
							Location to = e.getTo().clone().add(0, 1, 0);
							Location from = e.getFrom().clone().add(0, 1, 0);
							if (QualityArmoryVehicles.isWithinVehicle(to, ve)
									&& !QualityArmoryVehicles.isWithinVehicle(from, ve)) {
								if (e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) {
									if (e.getPlayer().getVelocity().getY() < -0.05) {
										e.getPlayer().setVelocity(e.getPlayer().getVelocity().setY(0.3));
									}
								}
								e.setCancelled(true);
								break;

							}
						}
					}
				}
			}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity().getVehicle() != null && QualityArmoryVehicles.isVehicle(e.getEntity().getVehicle()) || QualityArmoryVehicles.isPassager(e.getEntity().getVehicle())) {
			VehicleEntity ve = QualityArmoryVehicles.getVehicleEntityByEntity(e.getEntity().getVehicle());
			if (ve != null && ve.getType() != null)
				if ((ve.getType() instanceof AbstractHelicopter || ve.getType() instanceof AbstractPlane
						|| ve.getType() instanceof AbstractCar) && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
					e.setCancelled(true);
					return;
				}
		}

		VehicleEntity ve = null;
		// Get the vehicle
		if (QualityArmoryVehicles.isVehicle(e.getEntity())) {
			ve = QualityArmoryVehicles.getVehicleEntityByEntity(e.getEntity());
		} else if (e.getEntity().getVehicle() != null && QualityArmoryVehicles.isVehicle(e.getEntity().getVehicle())) {
			ve = QualityArmoryVehicles.getVehicleEntityByEntity(e.getEntity().getVehicle());
		}

		if (ve == null) {
			return;
		}

		if (!Main.enableVehicleDamage) {
			e.setCancelled(true);
			return;
		}

		if (e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || e.getCause() == EntityDamageEvent.DamageCause.DROWNING
				|| e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			e.setCancelled(true);
			return;
		}

		if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e instanceof EntityDamageByEntityEvent) {
			if (((EntityDamageByEntityEvent) e).getDamager().getType() == EntityType.ENDERMITE) {
				e.setCancelled(true);
				return;
			}
		}

		VehicleDamageEvent vde = new VehicleDamageEvent(ve, e.getDamage());
		Bukkit.getPluginManager().callEvent(vde);

		if (vde.isCanceled()) {
			e.setCancelled(true);
			return;
		}

		Main.DEBUG("Damaged vehicle: " + vde.getDamage() + " || Health= " + ve.getHealth() + " || Cause= "
				+ e.getCause().name());

		e.setDamage(vde.getDamage());
		ve.setHealth((float) (ve.getHealth() - vde.getDamage()));
		e.setCancelled(true);

		try {
			e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1);
		} catch (Error | Exception e4) {
			try {
				e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("HURT"), 1f, 1);
			} catch (Error | Exception ignored) {
			}
		}

		if (ve.getHealth() <= 0) {

			VehicleDestroyEvent vehicleDestroyEvent = new VehicleDestroyEvent(ve);
			Bukkit.getPluginManager().callEvent(vehicleDestroyEvent);

			if (vehicleDestroyEvent.isCanceled()) {
				e.setCancelled(true);
				return;
			}

			ve.deconstruct(null,"Destroy");

			try {
				ParticleHandlers.spawnMushroomCloud(e.getEntity().getLocation());
			} catch (Error | Exception ignored) {}

			try {
				e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.5f,
						1);
			} catch (Error | Exception e4) {
				try {
					e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.valueOf("EXPLODE"), 2.5f,
							1);
				} catch (Error | Exception ignored) {
				}
			}
		}
	}

}
