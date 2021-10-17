package mods.flammpfeil.slashblade.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadedValue;

import java.util.function.Supplier;

public class ItemTierSlashBlade implements Tier {


    private final LazyLoadedValue<Ingredient> repairMaterial;

    public ItemTierSlashBlade(Supplier<Ingredient> repairMaterialIn){
        repairMaterial = new LazyLoadedValue<>(repairMaterialIn);
    }

    @Override
    public int getUses() {
        return 100;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairMaterial.get();
    }
}
