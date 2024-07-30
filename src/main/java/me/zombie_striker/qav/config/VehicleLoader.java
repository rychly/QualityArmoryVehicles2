package me.zombie_striker.qav.config;

import me.zombie_striker.qav.attachments.Attachment;
import me.zombie_striker.qav.attachments.AttachmentType;
import me.zombie_striker.qav.attachments.Wheel;
import me.zombie_striker.qav.customitemmanager.MaterialStorage;
import me.zombie_striker.qav.Main;
import me.zombie_striker.qav.ModelSize;
import me.zombie_striker.qav.VehicleTypes;
import me.zombie_striker.qav.api.QualityArmoryVehicles;
import me.zombie_striker.qav.exceptions.InvalidVehicleException;
import me.zombie_striker.qav.finput.FInput;
import me.zombie_striker.qav.finput.FInputManager;
import me.zombie_striker.qav.hooks.model.Animation;
import me.zombie_striker.qav.qamini.QAMini;
import me.zombie_striker.qav.vehicles.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VehicleLoader {

	public static void loadVehicleFiles() {
		int count = 0;
		for (File f : Objects.requireNonNull(Main.carData.listFiles())) {
			try {
				if(loadVehicleFile(f))count++;
			} catch (Error | Exception e4) {
				QualityArmoryVehicles.getPlugin().getLogger().warning("Could not load file " + f.getName());
				e4.printStackTrace();
			}
		}
		if(!Main.verboseLogging){
			QualityArmoryVehicles.getPlugin().getLogger().info("Loaded "+count+" Vehicle types");
		}
	}

	public static boolean loadVehicleFile(File f) {
		if (QualityArmoryVehicles.getPlugin().getConfig().getBoolean("unsafe.useTurtles", false)) {
			new VehicleYML(f)
					.setModelSize(ModelSize.TURTLE)
					.save();
		}

		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		String name = c.getString("name");
		if (Main.verboseLogging)
			QualityArmoryVehicles.getPlugin().getLogger().info("Loading vehicle \"" + name + "\"");
		int id = c.getInt("id");
		VehicleTypes v = VehicleTypes.getTypeByName(c.getString("vehicle_type"));
		AbstractVehicle vehicle;
		switch (v) {
			case BOAT:
				vehicle = new AbstractBoat(name,id);
				break;
			case CAR:
				vehicle = new AbstractCar(name,id);
				break;
			case HELI:
				vehicle = new AbstractHelicopter(name,id);
				break;
			case PLANE:
				vehicle = new AbstractPlane(name,id);
				break;
			case TRAIN:
				vehicle = new AbstractTrain(name,id);
				break;
			case DRILL:
				vehicle = new AbstractDrill(name,id);
				break;
			case TRACTOR:
				vehicle = new AbstractTractor(name,id);
				break;
			default:
				throw new InvalidVehicleException("Vehicle type does not exist.");
		}
		if (c.contains("canDeconstructByEnvironment")) {
			vehicle.setDeconstructable(c.getBoolean("canDeconstructByEnvironment"));
		}

		if (c.contains("sound")) {
			vehicle.setSound(c.getString("sound"));
		}

		if (vehicle instanceof AbstractHelicopter && c.contains("descentSpeed"))
			((AbstractHelicopter) vehicle).setDescentSpeed(c.getDouble("descentSpeed"));
		if (c.contains("allowedInShop"))
			vehicle.setAllowInShop(c.getBoolean("allowedInShop"));
		if (c.contains("playDrivingSounds"))
			vehicle.setPlayCustomSounds(c.getBoolean("playDrivingSounds"));
		if (c.contains("cost"))
			vehicle.setPrice(c.getInt("cost"));
		if (c.contains("jumpHeight"))
			vehicle.setJumpHeight(c.getDouble("jumpHeight"));
		if (c.contains("maxHealth")) {
			double health = c.getDouble("maxHealth");
			vehicle.setMaxHealth(health == -1 ? Integer.MAX_VALUE : health);
		}
		if (c.contains("soundVolume"))
			vehicle.setSoundVolume(Float.parseFloat("" + c.getDouble("soundVolume")));
		if (c.contains("heightOffset"))
			vehicle.setHeight(c.getDouble("heightOffset"));
		if (c.contains("widthOffset"))
			vehicle.setWidthRadius(c.getDouble("widthOffset"));
		if (c.contains("vehicle_texture_material"))
			vehicle.setMaterial(Material.matchMaterial(c.getString("vehicle_texture_material", "DIAMOND_AXE")));
		if (c.contains("trunksize"))
			vehicle.setTrunkSize(c.getInt("trunksize"));
		if (c.contains("enablePlayerBodyDirectionFix"))
			vehicle.setBodyFix(c.getBoolean("enablePlayerBodyDirectionFix"));
		if (c.contains("ItemLore")) {
			List<String> lore = new ArrayList<>();
			for (String k : c.getStringList("ItemLore")) {
				lore.add(ChatColor.translateAlternateColorCodes('&', k));
			}
			vehicle.setLore(lore);
		}
		if (c.contains("canJumpOnBlocks"))
			vehicle.setCanJump(c.getBoolean("canJumpOnBlocks"));
		if (c.contains("RequiresFuel"))
			vehicle.setEnableFuel(c.getBoolean("RequiresFuel"));
		if (c.contains("TurnSpeedInRadians"))
			vehicle.setTurnRate(c.getDouble("TurnSpeedInRadians"));

		if(c.contains("model.ModelSize")){
			vehicle.setModelSize(ModelSize.valueOf(c.getString("model.ModelSize")));
		}else if (c.contains("increaseSize"))
			vehicle.setModelSize(ModelSize.ADULT_ARMORSTAND_HEAD);

		if (c.contains("baseAcceleration"))
			vehicle.setAccerlationSpeed(c.getDouble("baseAcceleration"));
		if (c.contains("maxAcceleration"))
			vehicle.setMaxSpeed(c.getDouble("maxAcceleration"));
		if (c.contains("maxReverseAcceleration"))
			vehicle.setMaxBackupSpeed(c.getDouble("maxReverseAcceleration"));
		if (c.contains("displayname"))
			vehicle.setDisplayname(c.getString("displayname"));
		if (c.contains("RequiresFuel"))
			vehicle.setEnableFuel(c.getBoolean("RequiresFuel"));
		if (c.contains("passagers")) {
			@SuppressWarnings("unchecked")
			List<Vector> list = (List<Vector>) c.getList("passagers", new ArrayList<>());

			HashMap<Vector,Integer> sizes = new HashMap<>();
			for(Vector v3 : list){
				double size = Math.min(2,v3.getY());
				double offset = v3.getY()-size;
				Vector v2 = v3.clone();
				v2.setY(offset);
				sizes.put(v2,(int)size);
			}

			vehicle.setPassagerSpots(sizes);
		}
/*		if (c.contains("attachments")) {
			List<Attachment> attachments = new ArrayList<>();

			for (String s : c.getConfigurationSection("attachments").getKeys(false)) {
				ConfigurationSection c2 = c.getConfigurationSection("attachments." + s);

				switch (AttachmentType.valueOf(c2.getString("type").toUpperCase())) {
					case WHEEL:
						attachments.add(new Wheel(c2.getString("model.name"), c2.getStringList("model.lore"), c2.getInt("model.id"), Material.getMaterial(c2.getString("model.material").toUpperCase()), c2.getVector("position")));
				}

			}

			vehicle.setAttachments(attachments);
		}*/
		if (c.contains("driverseat.Offset"))
			vehicle.setDriverSeat(c.getVector("driverseat.Offset"));
		if (c.contains("stopProjectileDamage"))
			vehicle.setStopsProjectileDamage(c.getBoolean("stopProjectileDamage"));
		if (c.contains("stopMeleeDamage"))
			vehicle.setStopsMeleeDamage(c.getBoolean("stopMeleeDamage"));
		if (c.contains("center"))
			vehicle.setCenter(c.getVector("center"));
		if (c.contains("rotationMultiplier"))
			vehicle.setRotationMultiplier(c.getDouble("rotationMultiplier"));
		if (c.contains("model.Animations")) {
			c.getStringList("model.Animations").forEach(animation -> {
				String[] split = animation.split(":");
				if (split.length >= 2) {
					Animation.AnimationType type = Animation.AnimationType.getType(split[0]);
					if (type != null) {
						vehicle.getAnimations().add(new Animation(type, split[1], split.length > 2 ? split[2] : null));
					} else {
						QualityArmoryVehicles.getPlugin().getLogger().warning("Invalid animation: " + split[0] + " for vehicle: " + vehicle.getName());
					}
				}
			});
		}
		registerInput(vehicle, FInput.ClickType.RIGHT, c);
		registerInput(vehicle, FInput.ClickType.F, c);
		registerInput(vehicle, FInput.ClickType.LEFT, c);

		Main.vehicleTypes.add(vehicle);
		try {
			me.zombie_striker.qg.api.QualityArmory.registerNewUsedExpansionItem(vehicle.getMaterial(),
					vehicle.getItemData());
		} catch (Error | Exception e) {
			QAMini.registeredItems.add(MaterialStorage.getMS(vehicle.getMaterial(), vehicle.getItemData(), 0));
		}
		return true;
	}

	private static void registerInput(AbstractVehicle vehicle, FInput.@NotNull ClickType type, @NotNull ConfigurationSection config) {
		if (!config.contains("InputManager.keys."+type.getId())) return;

		String input = config.getString("InputManager.keys."+type.getId(), "none");

		if (!input.equalsIgnoreCase("none")) {
			vehicle.getInputs().put(type.toString(), FInputManager.getHandler(input));
		}
	}
}
