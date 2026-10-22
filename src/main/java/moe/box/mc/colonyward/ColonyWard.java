package moe.box.mc.colonyward;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
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

    private boolean shouldBlock(EntityType<?> type, MobSpawnType reason,
                                ServerLevelAccessor level, BlockPos pos) {
        ResourceLocation id = EntityType.getKey(type);
        return config.isEntityInBlackList(id, reason)
                && IColonyManager.getInstance().isCoordinateInAnyColony(level.getLevel(), pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (shouldBlock(event.getEntity().getType(), event.getSpawnType(), event.getLevel(),
                BlockPos.containing(event.getX(), event.getY(), event.getZ()))) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (shouldBlock(event.getEntity().getType(), event.getSpawnType(), event.getLevel(),
                BlockPos.containing(event.getX(), event.getY(), event.getZ()))) {
            event.setSpawnCancelled(true);
        }
    }
}

