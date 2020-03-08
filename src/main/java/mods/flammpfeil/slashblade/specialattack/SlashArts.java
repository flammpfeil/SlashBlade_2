package mods.flammpfeil.slashblade.specialattack;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.RangeAttack;
import mods.flammpfeil.slashblade.util.RegistryBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Map;

public class SlashArts extends RegistryBase<SlashArts> {
    public static final SlashArts NONE = new SlashArts(BaseInstanceName);

    public SlashArts(String name) {
        super(name);
    }

    @Override
    public String getPath() {
        return "slasharts";
    }

    @Override
    public SlashArts getNone() {
        return NONE;
    }

    public ComboState getComboState() {
        return ComboState.NONE;
    }
}
