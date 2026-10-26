package io.github.skymr98alt.tests;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@net.minecraftforge.fml.common.Mod("colony_spawn_guard_tests")
@GameTestHolder("colony_spawn_guard")
@PrefixGameTestTemplate(false)
public class SpawnSmokeTests {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "colony_spawn_guard_tests");
    private static final RegistryObject<EntityType<Zombie>> MODDED_ENEMY = TYPES.register("unlisted_enemy",
            () -> EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.CREATURE).sized(0.6F, 1.8F).build("unlisted_enemy"));
    private static final RegistryObject<EntityType<CategoryMonster>> MODDED_MONSTER = TYPES.register("unlisted_monster",
            () -> EntityType.Builder.<CategoryMonster>of(CategoryMonster::new, MobCategory.MONSTER).sized(0.6F, 1.8F).build("unlisted_monster"));

    public static class CategoryMonster extends PathfinderMob {
        public CategoryMonster(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) { super(type, level); }
    }

    public SpawnSmokeTests() {
        var bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        TYPES.register(bus);
        bus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) -> {
            event.put(MODDED_ENEMY.get(), Zombie.createAttributes().build());
            event.put(MODDED_MONSTER.get(), Mob.createMobAttributes().build());
        });
    }
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void colonySpawnProtection(GameTestHelper helper) {
        var level = helper.getLevel();
        var manager = IColonyManager.getInstance();
        var pos = helper.absolutePos(BlockPos.ZERO);
        helper.assertTrue(!manager.isCoordinateInAnyColony(level, pos), "Test must start outside a colony");
        Mob zombie = EntityType.ZOMBIE.create(level);
        zombie.moveTo(pos, 0, 0);
        var outside = new MobSpawnEvent.PositionCheck(zombie, level, MobSpawnType.NATURAL, null);
        MinecraftForge.EVENT_BUS.post(outside);
        helper.assertTrue(outside.getResult() == Event.Result.DEFAULT, "Outside spawn must remain unchanged");
        var outsideFinal = finalizeEvent(zombie, helper, MobSpawnType.NATURAL);
        MinecraftForge.EVENT_BUS.post(outsideFinal);
        helper.assertTrue(!outsideFinal.isSpawnCancelled(), "Outside finalization must remain allowed");

        var colony = manager.createColony(level, pos, FakePlayerFactory.getMinecraft(level),
                "Spawn Guard integration test", "minecolonies:medievaloak");
        helper.assertTrue(colony != null, "MineColonies must create a real test colony");
        helper.succeedWhen(() -> {
            helper.assertTrue(manager.isCoordinateInAnyColony(level, pos), "Test colony must claim its center");
            var inside = new MobSpawnEvent.PositionCheck(zombie, level, MobSpawnType.NATURAL, null);
            MinecraftForge.EVENT_BUS.post(inside);
            helper.assertTrue(inside.getResult() == Event.Result.DENY, "Natural zombie spawn inside colony must be denied");
            var insideFinal = finalizeEvent(zombie, helper, MobSpawnType.NATURAL);
            MinecraftForge.EVENT_BUS.post(insideFinal);
            helper.assertTrue(insideFinal.isSpawnCancelled(), "Inside finalization must cancel the spawn");
            var egg = new MobSpawnEvent.PositionCheck(zombie, level, MobSpawnType.SPAWN_EGG, null);
            MinecraftForge.EVENT_BUS.post(egg);
            helper.assertTrue(egg.getResult() == Event.Result.DEFAULT, "Empty spawn-egg blacklist must allow the spawn");
            Mob pig = EntityType.PIG.create(level);
            pig.moveTo(pos, 0, 0);
            var peaceful = new MobSpawnEvent.PositionCheck(pig, level, MobSpawnType.NATURAL, null);
            MinecraftForge.EVENT_BUS.post(peaceful);
            helper.assertTrue(peaceful.getResult() == Event.Result.DEFAULT, "Unlisted pig must remain allowed inside colony");
            for (EntityType<? extends Mob> type : java.util.List.of(MODDED_ENEMY.get(), MODDED_MONSTER.get())) {
                Mob modded = type.create(level);
                modded.moveTo(pos, 0, 0);
                var natural = new MobSpawnEvent.PositionCheck(modded, level, MobSpawnType.NATURAL, null);
                MinecraftForge.EVENT_BUS.post(natural);
                helper.assertTrue(natural.getResult() == Event.Result.DENY, "Unlisted modded hostile must be blocked: " + EntityType.getKey(type));
                var customEvent = finalizeEvent(modded, helper, MobSpawnType.EVENT);
                MinecraftForge.EVENT_BUS.post(customEvent);
                helper.assertTrue(customEvent.isSpawnCancelled(), "Modded event spawn must be cancelled");
                Mob direct = type.create(level);
                direct.moveTo(pos, 0, 0);
                helper.assertTrue(!level.addFreshEntity(direct), "Direct insertion bypassing spawn checks must be blocked");
                var saved = new EntityJoinLevelEvent(direct, level, true);
                MinecraftForge.EVENT_BUS.post(saved);
                helper.assertTrue(!saved.isCanceled(), "Existing saved mobs must not be blocked on reload");
                Mob eggMob = type.spawn(level, pos, MobSpawnType.SPAWN_EGG);
                helper.assertTrue(eggMob != null && level.getEntity(eggMob.getUUID()) != null, "Explicit spawn eggs must remain allowed");
                eggMob.discard();
            }
            helper.assertTrue(level.addFreshEntity(pig), "Peaceful direct insertion must remain allowed");
            pig.discard();
            manager.deleteColonyByWorld(colony.getID(), false, level);
        });
    }

    private static MobSpawnEvent.FinalizeSpawn finalizeEvent(Mob mob, GameTestHelper helper, MobSpawnType reason) {
        return new MobSpawnEvent.FinalizeSpawn(mob, helper.getLevel(), mob.getX(), mob.getY(), mob.getZ(),
                helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()), reason, null, null, null);
    }
}








