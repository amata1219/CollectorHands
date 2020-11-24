package amata1219.collector.hands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectorHands extends JavaPlugin implements Listener, CommandExecutor {

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("givemc").setExecutor(this);
    }

    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "ゲーム内から実行して下さい。");
            return true;
        } else {
            Player player = (Player)sender;
            int i;
            if (args.length == 0) {
                i = 1;
            } else {
                try {
                    i = Integer.valueOf(args[0]);
                    if (i > 64) {
                        i = 64;
                    }
                } catch (NumberFormatException var8) {
                    player.sendMessage(ChatColor.RED + "個数は半角数字で入力して下さい。");
                    return true;
                }
            }

            this.giveMobCatcher(player, i);
            player.sendMessage(ChatColor.GRAY + "Given [MobCatcher] * " + i + " to " + player.getName());
            return true;
        }
    }

    public void giveMobCatcher(Player player, int amount) {
        if (amount > 64) {
            amount = 64;
        }

        ItemStack item = new ItemStack(Material.MONSTER_EGG);
        item.setAmount(amount);
        SpawnEggMeta meta = (SpawnEggMeta)item.getItemMeta();
        meta.setSpawnedType(EntityType.SNOWMAN);
        meta.setDisplayName("§fモブキャッチャー");
        meta.setLore(new ArrayList(Arrays.asList("§7友好的なMobをLShift+右クリックで捕獲！")));
        item.setItemMeta(meta);
        player.getInventory().addItem(new ItemStack[]{item});
    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!e.isCancelled()) {
            Entity damager = e.getDamager();
            if (damager instanceof Player) {
                Player player = (Player)damager;
                if (player.isSneaking()) {
                    PlayerInventory inventory = player.getInventory();
                    ItemStack hand = inventory.getItemInMainHand();
                    if (this.isMobCatcher(hand)) {
                        e.setCancelled(true);
                        boolean one = hand.getAmount() == 1;
                        if (!one || inventory.firstEmpty() != -1) {
                            Entity entity = e.getEntity();
                            if (entity instanceof LivingEntity) {
                                LivingEntity target = (LivingEntity)entity;
                                UUID uuid = player.getUniqueId();
                                ItemStack item = null;
                                switch(e.getEntityType().ordinal()) {
                                    case 31:
                                    case 32:
                                    case 74:
                                    case 76:
                                    case 79:
                                    case 81:
                                        Tameable ent1 = (Tameable)entity;
                                        if (!this.canCatch(uuid, ent1.getOwner())) {
                                            return;
                                        }

                                        item = Util.mobToItemStack(target);
                                        break;
                                    case 61:
                                    case 66:
                                    case 67:
                                    case 68:
                                    case 69:
                                    case 70:
                                    case 72:
                                    case 77:
                                    case 82:
                                        item = Util.mobToItemStack(target);
                                        break;
                                    case 71:
                                        Wolf wolf = (Wolf)entity;
                                        if (wolf.isAngry()) {
                                            return;
                                        }

                                        if (!this.canCatch(uuid, wolf.getOwner())) {
                                            return;
                                        }

                                        item = Util.mobToItemStack(target);
                                        break;
                                    case 78:
                                        if (this.isTarget(player, ((Creature)entity).getTarget())) {
                                            return;
                                        }

                                        item = Util.mobToItemStack(target);
                                        break;
                                    default:
                                        return;
                                }

                                target.remove();
                                if (one) {
                                    inventory.setItemInMainHand(item);
                                } else {
                                    hand.setAmount(hand.getAmount() - 1);
                                    inventory.addItem(new ItemStack[]{item});
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.hasBlock()) {
                if (e.hasItem()) {
                    Block block = e.getClickedBlock();
                    Material material = block.getType();
                    if (block != null && material != Material.AIR) {
                        ItemStack item = e.getItem();
                        if (item != null && item.getType() == Material.MONSTER_EGG) {
                            if (item.hasItemMeta()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta.hasLore()) {
                                    List<String> lore = meta.getLore();
                                    if (meta.getLore().contains("§7友好的なMobをLShift+右クリックで捕獲！")) {
                                        e.setCancelled(true);
                                    } else {
                                        if (material == Material.MOB_SPAWNER && lore.contains("§7ブロックに向かって右クリックで捕獲したMobを召喚！")) {
                                            e.setCancelled(true);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDispench(BlockDispenseEvent e) {
        Block block = e.getBlock();
        if (block != null && block.getType() == Material.DISPENSER) {
            if (this.isMobCatcher(e.getItem())) {
                e.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (inventory != null && inventory instanceof AnvilInventory) {
            if (this.isMobCatcher(e.getCurrentItem())) {
                e.setCancelled(true);
            }

        }
    }

    public boolean isMobCatcher(ItemStack item) {
        if (item != null && item.getType() == Material.MONSTER_EGG) {
            if (!item.hasItemMeta()) {
                return false;
            } else {
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasLore()) {
                    return false;
                } else {
                    List<String> lore = meta.getLore();
                    if (meta.getLore().contains("§7ブロックに向かって右クリックで捕獲したMobを召喚！")) {
                        return false;
                    } else {
                        return lore.contains("§7友好的なMobをLShift+右クリックで捕獲！");
                    }
                }
            }
        } else {
            return false;
        }
    }

    private boolean canCatch(UUID uuid, AnimalTamer tamer) {
        return tamer == null || tamer.getUniqueId().equals(uuid);
    }

    private boolean isTarget(Player player, LivingEntity target) {
        return target != null && target.equals(player);
    }
}
