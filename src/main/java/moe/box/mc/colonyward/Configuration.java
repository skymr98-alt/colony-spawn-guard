package moe.box.mc.colonyward;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Configuration {
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> breeding;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> bucket;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> chunk_generation;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> command;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> conversion;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> dispenser;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> event;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> jockey;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> natural;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> patrol;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> reinforcement;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> spawnEgg;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> spawner;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> structure;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> summoned;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> triggered;

    public Configuration(ForgeConfigSpec.Builder builder) {
        builder.comment("Blacklist (All mobs or mods defined here will be prevented from spawning inside a colony)").push("general");

        breeding = builder.comment("Mobs spawned from breeding")
                .defineList("breeding", Lists.newArrayList(), o -> o instanceof String);

        bucket = builder.comment("Mobs spawned from buckets")
                .defineList("bucket", Lists.newArrayList(), o -> o instanceof String);

        chunk_generation = builder.comment("Mobs spawned from chunk generation")
                .defineList("chunk_generation", Lists.newArrayList(), o -> o instanceof String);

        command = builder.comment("Mobs spawned from commands")
                .defineList("command", Lists.newArrayList(), o -> o instanceof String);

        conversion = builder.comment("Mobs spawned from conversions (Like a creeper becoming a charge creeper)")
                .defineList("conversion", Lists.newArrayList(), o -> o instanceof String);

        dispenser = builder.comment("Mobs spawned from dispensers")
                .defineList("dispenser", Lists.newArrayList(), o -> o instanceof String);

        event = builder.comment("Mobs spawned from events")
                .defineList("event", Lists.newArrayList("lycanitesmobs"), o -> o instanceof String);

        jockey = builder.comment("Mobs spawned as a jockey (Like the spider jockey with a skeleton)")
                .defineList("jockey", Lists.newArrayList(
                        "minecraft:spider",
                        "minecraft:chicken",
                        "minecraft:skeleton_horse"), o -> o instanceof String);

        natural = builder.comment("Mobs spawned naturally")
                .defineList("natural", Lists.newArrayList(
                        "minecraft:enderman",
                        "minecraft:spider",
                        "minecraft:zombified_piglin",
                        "minecraft:polar_bear",
                        "minecraft:blaze",
                        "minecraft:creeper",
                        "minecraft:drowned",
                        "minecraft:ghast",
                        "minecraft:hoglin",
                        "minecraft:husk",
                        "minecraft:magma_cube",
                        "minecraft:skeleton",
                        "minecraft:skeleton_horse",
                        "minecraft:stray",
                        "minecraft:zombie",
                        "minecraft:zombie_villager",
                        "minecraft:witch",
                        "minecraft:piglin",
                        "lycanitesmobs",
                        "rottencreatures",
                        "quark",
                        "alexsmobs",
                        "creeperoverhaul",
                        "savage_and_ravage",
                        "buzzier_bees:grizzly_bear",
                        "quark:foxhound",
                        "cnb:sporeling",
                        "ars_nouveau:wilden_hunter",
                        "ars_nouveau:wilden_stalker"
                ), o -> o instanceof String);

        patrol = builder.comment("Mobs spawned as patrol (Like the pillagers)")
                .defineList("patrol", Lists.newArrayList("minecraft:pillager", "minecraft:vindicator"), o -> o instanceof String);

        reinforcement = builder.comment("Mobs spawned as reinforcement (Like Zombified Piglin calling for reinforcement when attacked)")
                .defineList("reinforcement", Lists.newArrayList(), o -> o instanceof String);

        spawnEgg = builder.comment("Mobs spawned from a spawn egg")
                .defineList("spawnEgg", Lists.newArrayList(), o -> o instanceof String);

        spawner = builder.comment("Mobs spawned from spawners")
                .defineList("spawner", Lists.newArrayList(), o -> o instanceof String);

        structure = builder.comment("Mobs spawned inside a structure (With the structure block)")
                .defineList("structure", Lists.newArrayList(), o -> o instanceof String);

        summoned = builder.comment("Mobs summoned")
                .defineList("summoned", Lists.newArrayList(), o -> o instanceof String);

        triggered = builder.comment("Mobs spawned from a trigger (Like with the skeleton trap (skeleton horse))")
                .defineList("triggered", Lists.newArrayList("minecraft:skeleton_horse"), o -> o instanceof String);

builder.pop();
    }

    public boolean isEntityInBlackList(ResourceLocation entityResource, MobSpawnType spawnReason) {
        var list = getBlacklist(spawnReason);
        if (list == null) return false;

        return list.contains(entityResource.toString()) || list.contains(entityResource.getNamespace());
    }

    private List<? extends String> getBlacklist(MobSpawnType spawnReason) {
        return switch (spawnReason) {
            case NATURAL -> natural.get();
            case CHUNK_GENERATION -> chunk_generation.get();
            case SPAWNER -> spawner.get();
            case STRUCTURE -> structure.get();
            case BREEDING -> breeding.get();
            case MOB_SUMMONED -> summoned.get();
            case JOCKEY -> jockey.get();
            case EVENT -> event.get();
            case CONVERSION -> conversion.get();
            case REINFORCEMENT -> reinforcement.get();
            case TRIGGERED -> triggered.get();
            case BUCKET -> bucket.get();
            case SPAWN_EGG -> spawnEgg.get();
            case COMMAND -> command.get();
            case DISPENSER -> dispenser.get();
            case PATROL -> patrol.get();
        };
    }
}

