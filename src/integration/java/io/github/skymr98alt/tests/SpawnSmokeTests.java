package io.github.skymr98alt.tests;

import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
            manager.deleteColonyByWorld(colony.getID(), false, level);
        });
    }

    private static MobSpawnEvent.FinalizeSpawn finalizeEvent(Mob mob, GameTestHelper helper, MobSpawnType reason) {
        return new MobSpawnEvent.FinalizeSpawn(mob, helper.getLevel(), mob.getX(), mob.getY(), mob.getZ(),
                helper.getLevel().getCurrentDifficultyAt(mob.blockPosition()), reason, null, null, null);
    }
}






