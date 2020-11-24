package amata1219.collector.hands;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {
    private static final Method getHandle;
    private static final Method asNMSCopy;
    private static final Method getTag;
    private static final Method b;
    private static final Method set;
    private static final Method setString;
    private static final Method setBoolean;
    private static final Method setInt;
    private static final Method setByte;
    private static final Method setTag;
    private static final Method asBukkitCopy;
    private static final Field name;

    static {
        String nms = getNMSPackageName();
        String obc = getOBCPackageName();
        Method arg0 = null;
        Method arg1 = null;
        Method arg2 = null;
        Method arg3 = null;
        Method arg4 = null;
        Method arg5 = null;
        Method arg6 = null;
        Method arg7 = null;
        Field arg8 = null;
        Method arg9 = null;
        Method arg10 = null;
        Method arg11 = null;

        try {
            Class<?> CraftLivingEntity = Class.forName(obc + ".entity.CraftLivingEntity");
            arg0 = CraftLivingEntity.getMethod("getHandle");
            Class<?> CraftItemStack = Class.forName(obc + ".inventory.CraftItemStack");
            arg1 = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Class<?> NMSItemStack = Class.forName(nms + ".ItemStack");
            arg2 = NMSItemStack.getMethod("getTag");
            Class<?> NBTTagCompound = Class.forName(nms + ".NBTTagCompound");
            Class<?> EntityLiving = Class.forName(nms + ".EntityLiving");
            arg3 = EntityLiving.getMethod("b", NBTTagCompound);
            Class<?> NBTBase = Class.forName(nms + ".NBTBase");
            arg4 = NBTTagCompound.getMethod("set", String.class, NBTBase);
            arg7 = NBTTagCompound.getMethod("setString", String.class, String.class);
            arg9 = NBTTagCompound.getMethod("setBoolean", String.class, Boolean.TYPE);
            arg10 = NBTTagCompound.getMethod("setInt", String.class, Integer.TYPE);
            arg11 = NBTTagCompound.getMethod("setByte", String.class, Byte.TYPE);
            arg5 = NMSItemStack.getMethod("setTag", NBTTagCompound);
            arg6 = CraftItemStack.getMethod("asBukkitCopy", NMSItemStack);
            arg8 = EntityType.class.getDeclaredField("name");
            arg8.setAccessible(true);
        } catch (ClassNotFoundException var20) {
            var20.printStackTrace();
        } catch (NoSuchMethodException var21) {
            var21.printStackTrace();
        } catch (SecurityException var22) {
            var22.printStackTrace();
        } catch (NoSuchFieldException var23) {
            var23.printStackTrace();
        }

        getHandle = arg0;
        asNMSCopy = arg1;
        getTag = arg2;
        b = arg3;
        set = arg4;
        setString = arg7;
        setBoolean = arg9;
        setInt = arg10;
        setByte = arg11;
        setTag = arg5;
        asBukkitCopy = arg6;
        name = arg8;
    }

    public Util() {
    }

    public static String getNMSPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "net.minecraft.server");
    }

    public static String getOBCPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    public static ItemStack mobToItemStack(LivingEntity entity) {
        ItemStack item = new ItemStack(Material.MONSTER_EGG);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(new ArrayList(Arrays.asList("§7ブロックに向かって右クリックで捕獲したMobを召喚！")));
        item.setItemMeta(meta);

        try {
            Object nmsItemStack = asNMSCopy.invoke((Object)null, item);
            Object nmsNBTTagCompound = getTag.invoke(nmsItemStack);
            if (nmsNBTTagCompound == null) {
                nmsNBTTagCompound = Class.forName(getNMSPackageName() + ".NBTTagCompound").getConstructor().newInstance();
            }

            Object nmsEntityLiving = getHandle.invoke(entity);
            Object nbtTag = nmsNBTTagCompound.getClass().getConstructor().newInstance();
            setString.invoke(nbtTag, "id", name.get(entity.getType()));
            b.invoke(nmsEntityLiving, nbtTag);
            if (entity.getCustomName() != null) {
                setString.invoke(nbtTag, "CustomName", entity.getCustomName());
                if (entity.isCustomNameVisible()) {
                    setInt.invoke(nbtTag, "CustomNameVisible", 1);
                }
            }

            if (entity.isGlowing()) {
                setByte.invoke(nbtTag, "Glowing", 1);
            }

            if (entity.isInvulnerable()) {
                setInt.invoke(nbtTag, "Invulnerable", 1);
            }

            if (entity.isSilent()) {
                setInt.invoke(nbtTag, "Silent", 1);
            }

            if (!entity.hasAI()) {
                setInt.invoke(nbtTag, "NoAI", 1);
            }

            if (!entity.hasGravity()) {
                setByte.invoke(nbtTag, "NoGravity", 1);
            }

            if (entity.getCanPickupItems()) {
                setByte.invoke(nbtTag, "CanPickUpLoot", 1);
            }

            if (entity.getFireTicks() > 0) {
                setInt.invoke(nbtTag, "Fire", entity.getFireTicks());
            }

            set.invoke(nmsNBTTagCompound, "EntityTag", nbtTag);
            setTag.invoke(nmsItemStack, nmsNBTTagCompound);
            item = (ItemStack)asBukkitCopy.invoke((Object)null, nmsItemStack);
        } catch (IllegalAccessException var7) {
            var7.printStackTrace();
        } catch (IllegalArgumentException var8) {
            var8.printStackTrace();
        } catch (InvocationTargetException var9) {
            var9.printStackTrace();
        } catch (InstantiationException var10) {
            var10.printStackTrace();
        } catch (NoSuchMethodException var11) {
            var11.printStackTrace();
        } catch (SecurityException var12) {
            var12.printStackTrace();
        } catch (ClassNotFoundException var13) {
            var13.printStackTrace();
        }

        return item;
    }
}
