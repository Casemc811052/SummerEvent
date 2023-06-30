package net.besence.summerevent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class SummerEventPlugin extends JavaPlugin implements Listener {
    private boolean spawning = false;
    private int taskId = -1;
    private WorldEditPlugin worldEdit;

    @Override
    public void onEnable() {
        // WorldEditプラグインの取得
        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

        // イベントリスナーの登録
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // スポーンイベントが実行中なら停止
        stopSpawning();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("summerevent")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
                startSpawning();
                sender.sendMessage("スポーンイベントを開始しました。");
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
                stopSpawning();
                sender.sendMessage("スポーンイベントを停止しました。");
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("give") && args.length > 1) {
                if (args[1].equalsIgnoreCase("meronstick")) {
                    Player player = (Player) sender;
                    ItemStack stick = createMeronStick();
                    player.getInventory().addItem(stick);
                    player.sendMessage("スイカ割りの棒を入手しました。");
                    return true;
                }
            }
        }
        return false;
    }

    private void startSpawning() {
        if (!spawning) {
            spawning = true;
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::spawnZombie, 0L, 100L);
        }
    }

    private void stopSpawning() {
        if (spawning) {
            spawning = false;
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equalsIgnoreCase("スイカ割りの棒")) {
                event.setCancelled(true);
                player.sendMessage("スイカ割りの棒を使用しました！");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getDisplayName().equalsIgnoreCase("スイカ割りの棒")) {
                if (event.getRightClicked().getType() == EntityType.ZOMBIE) {
                    Zombie zombie = (Zombie) event.getRightClicked();
                    zombie.damage(2);
                    zombie.setFireTicks(20);
                }
            }
        }
    }

    private ItemStack createMeronStick() {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName("スイカ割りの棒");
        stick.setItemMeta(meta);
        return stick;
    }

    private void spawnZombie() {
        Selection selection = worldEdit.getSelection((Player) Bukkit.getOnlinePlayers().toArray()[0]);
        if (selection != null) {
            Location min = selection.getMinimumPoint();
            Location max = selection.getMaximumPoint();

            double minX = min.getX();
            double minY = min.getY();
            double minZ = min.getZ();
            double maxX = max.getX();
            double maxY = max.getY();
            double maxZ = max.getZ();

            double x = minX + Math.random() * (maxX - minX + 1);
            double y = minY + Math.random() * (maxY - minY + 1);
            double z = minZ + Math.random() * (maxZ - minZ + 1);

            Location spawnLocation = new Location(min.getWorld(), x, y, z);
            Zombie zombie = (Zombie) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
            zombie.getEquipment().setHelmet(new ItemStack(Material.WATERMELON));
        }
    }
}
