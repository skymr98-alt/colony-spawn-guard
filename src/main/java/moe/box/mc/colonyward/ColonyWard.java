package moe.box.mc.colonyward;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;

/** Unofficial Forge 1.20.1 adaptation of ColonyWard; see NOTICE.md. */
@Mod(ColonyWard.MOD_ID)
public class ColonyWard {
    public static final String MOD_ID = "colony_spawn_guard";
    private final Configuration config;

    public ColonyWard() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        config = new Configuration(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private boolean shouldBlock(Mob mob, MobSpawnType reason,
                                ServerLevelAccessor level, BlockPos pos) {
        return config.shouldBlock(mob, reason)
                && IColonyManager.getInstance().isCoordinateInAnyColony(level.getLevel(), pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (shouldBlock(event.getEntity(), event.getSpawnType(), event.getLevel(),
                BlockPos.containing(event.getX(), event.getY(), event.getZ()))) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (shouldBlock(event.getEntity(), event.getSpawnType(), event.getLevel(),
                BlockPos.containing(event.getX(), event.getY(), event.getZ()))) {
            event.setSpawnCancelled(true);
        }
    }
    /** Catch mods that add newly created mobs directly, bypassing Forge spawn checks. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()
                || !(event.getEntity() instanceof Mob mob)) return;
        if (config.shouldBlock(mob, mob.getSpawnType())
                && IColonyManager.getInstance().isCoordinateInAnyColony(event.getLevel(), mob.blockPosition())) {
            event.setCanceled(true);
        }
    }
}



