package mods.flammpfeil.slashblade.capability.slashblade;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilitySlashBlade
{
    public static final Capability<ISlashBladeState> BLADESTATE = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ISlashBladeState.class);
    }
}