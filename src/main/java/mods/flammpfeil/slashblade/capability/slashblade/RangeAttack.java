package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.util.RegistryBase;

public class RangeAttack extends RegistryBase<RangeAttack> {
    public static final RangeAttack NONE = new RangeAttack(BaseInstanceName);

    RangeAttack(String name) {
        super(name);
    }

    @Override
    public String getPath() {
        return "rangeattack";
    }

    @Override
    public RangeAttack getNone() {
        return NONE;
    }
}
