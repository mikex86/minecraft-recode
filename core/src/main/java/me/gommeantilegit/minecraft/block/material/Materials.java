package me.gommeantilegit.minecraft.block.material;

import me.gommeantilegit.minecraft.block.mapcolor.MapColor;

/**
 * Class storing all possible material instances
 */
public class Materials {

    public static final Material air = new MaterialTransparent(MapColor.airColor);
    public static final Material grass = new Material(MapColor.grassColor);
    public static final Material ground = new Material(MapColor.dirtColor);
    public static final Material wood = (new Material(MapColor.woodColor)).setCanBurn();
    public static final Material rock = (new Material(MapColor.stoneColor)).setRequiresTool();
    public static final Material iron = (new Material(MapColor.ironColor)).setRequiresTool();
    public static final Material anvil = (new Material(MapColor.ironColor)).setRequiresTool().setImmovableMobility();
    public static final Material water = (new MaterialLiquid(MapColor.waterColor)).setNoPushMobility();
    public static final Material lava = (new MaterialLiquid(MapColor.tntColor)).setNoPushMobility();
    public static final Material leaves = (new Material(MapColor.foliageColor)).setCanBurn().setTranslucent().setNoPushMobility();
    public static final Material plants = (new MaterialLogic(MapColor.foliageColor)).setNoPushMobility();
    public static final Material vine = (new MaterialLogic(MapColor.foliageColor)).setCanBurn().setNoPushMobility().setReplaceable();
    public static final Material sponge = new Material(MapColor.yellowColor);
    public static final Material cloth = (new Material(MapColor.clothColor)).setCanBurn();
    public static final Material fire = (new MaterialTransparent(MapColor.airColor)).setNoPushMobility();
    public static final Material sand = new Material(MapColor.sandColor);
    public static final Material circuits = (new MaterialLogic(MapColor.airColor)).setNoPushMobility();
    public static final Material carpet = (new MaterialLogic(MapColor.clothColor)).setCanBurn();
    public static final Material glass = (new Material(MapColor.airColor)).setTranslucent().setAdventureModeExempt();
    public static final Material redstoneLight = (new Material(MapColor.airColor)).setAdventureModeExempt();
    public static final Material tnt = (new Material(MapColor.tntColor)).setCanBurn().setTranslucent();
    public static final Material coral = (new Material(MapColor.foliageColor)).setNoPushMobility();
    public static final Material ice = (new Material(MapColor.iceColor)).setTranslucent().setAdventureModeExempt();
    public static final Material packedIce = (new Material(MapColor.iceColor)).setAdventureModeExempt();
    public static final Material snow = (new MaterialLogic(MapColor.snowColor)).setReplaceable().setTranslucent().setRequiresTool().setNoPushMobility();

    /**
     * The material for crafted snow.
     */
    public static final Material craftedSnow = (new Material(MapColor.snowColor)).setRequiresTool();
    public static final Material cactus = (new Material(MapColor.foliageColor)).setTranslucent().setNoPushMobility();
    public static final Material clay = new Material(MapColor.clayColor);
    public static final Material gourd = (new Material(MapColor.foliageColor)).setNoPushMobility();
    public static final Material dragonEgg = (new Material(MapColor.foliageColor)).setNoPushMobility();
    public static final Material portal = (new MaterialPortal(MapColor.airColor)).setImmovableMobility();
    public static final Material cake = (new Material(MapColor.airColor)).setNoPushMobility();
    public static final Material web = (new Material(MapColor.clothColor) {
        public boolean blocksMovement() {
            return false;
        }
    }).setRequiresTool().setNoPushMobility();

    /**
     * Pistons' material.
     */
    public static final Material piston = (new Material(MapColor.stoneColor)).setImmovableMobility();
    public static final Material barrier = (new Material(MapColor.airColor)).setRequiresTool().setImmovableMobility();

}
