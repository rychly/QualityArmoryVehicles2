package me.zombie_striker.qav.hooks;

import me.zombie_striker.qav.Main;
import me.zombie_striker.qav.hooks.implementation.TownyHook;
import me.zombie_striker.qav.hooks.implementation.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ProtectionHandler {
    private final static Set<ProtectionHook> compatibilities = new HashSet<>();

    public static void init() {
        hook("WorldGuard", WorldGuardHook::new);
        hook("Towny", TownyHook::new);
    }

    public static boolean canMove(Player player, Location target) {
        return compatibilities.stream().allMatch(compatibility -> compatibility.canMove(player, target));
    }

    public static boolean canPlace(Player player, Location target) {
        return compatibilities.stream().allMatch(compatibility -> compatibility.canPlace(player, target));
    }

    public static boolean canRemove(Player player, Location target) {
        return compatibilities.stream().allMatch(compatibility -> compatibility.canRemove(player, target));
    }

    public static void hook(String plugin, CompatibilityConstructor constructor) {
        if (Bukkit.getPluginManager().getPlugin(plugin) != null && (boolean) Main.a("hooks." + plugin, true)) {
            compatibilities.add(constructor.create());
        }
    }

    @FunctionalInterface
    private interface CompatibilityConstructor {
        ProtectionHook create();
    }
}
