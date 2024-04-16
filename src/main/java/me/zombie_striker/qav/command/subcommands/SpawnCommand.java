package me.zombie_striker.qav.command.subcommands;

import me.zombie_striker.qav.Main;
import me.zombie_striker.qav.MessagesConfig;
import me.zombie_striker.qav.api.QualityArmoryVehicles;
import me.zombie_striker.qav.command.QAVCommand;
import me.zombie_striker.qav.command.SubCommand;
import me.zombie_striker.qav.perms.PermissionHandler;
import me.zombie_striker.qav.vehicles.AbstractVehicle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand extends SubCommand {

    public SpawnCommand(QAVCommand command) {
        super(command);
    }

    @Override
    public String getName() {
        return "spawnVehicle";
    }

    @Override
    public @Nullable String getDescription(@NotNull CommandSender sender) {
        return sender.hasPermission(PermissionHandler.PERM_SPAWN) ? MessagesConfig.subcommand_removeNearbyVehicles : null;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissionHandler.PERM_SPAWN)) {
            sender.sendMessage(Main.prefix + MessagesConfig.COMMANDMESSAGES_NO_PERM);
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(Main.prefix + "Usage /qav " + getName() + " <vehicle> [x] [y] [z]");
            return;
        }

        String name = args[0];
        Location location = null;

        if (args.length == 4) {
            try {
                location = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3]));
            } catch (NumberFormatException ignored) {}
        } else if (sender instanceof Player) {
            location = ((Player) sender).getLocation();
        } else {
            sender.sendMessage(Main.prefix + MessagesConfig.COMMANDMESSAGES_ONLY_PLAYERs);
            return;
        }

        AbstractVehicle ab = QualityArmoryVehicles.getVehicle(name);
        if (ab == null) {
            sender.sendMessage(Main.prefix + MessagesConfig.COMMANDMESSAGES_VALID_VEHICLE);
            return;
        }

        assert location != null;
        QualityArmoryVehicles.spawnVehicle(ab,location,sender instanceof Player ? (Player) sender : null);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 0 || args.length == 1) {
            for (AbstractVehicle vehicle : Main.vehicleTypes) {
                if (vehicle.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(vehicle.getName());
                }
            }
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 2) {
                list.add(player.getLocation().getX() + "");
            }
            if (args.length == 3) {
                list.add(player.getLocation().getY() + "");
            }
            if (args.length == 4) {
                list.add(player.getLocation().getZ() + "");
            }
        }

        return list;
    }
}
