package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.imputstate.ImputStateCapabilityProvider;
import mods.flammpfeil.slashblade.capability.mobeffect.MobEffectCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateCapabilityProvider;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityAttachHandler {

    static public final ResourceLocation MOBEFFECT_KEY = new ResourceLocation(SlashBlade.modid, "mobeffect");
    static public final ResourceLocation IMPUTSTATE_KEY = new ResourceLocation(SlashBlade.modid, "imputstate");
    @SubscribeEvent
    public void AttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof LivingEntity)) return;

        event.addCapability(IMPUTSTATE_KEY,new ImputStateCapabilityProvider());
        event.addCapability(MOBEFFECT_KEY, new MobEffectCapabilityProvider());
    }

    static public final ResourceLocation BLADESTATE_KEY = new ResourceLocation(SlashBlade.modid, "bladestate");
    @SubscribeEvent
    public void AttachCapabilities_ItemStack(AttachCapabilitiesEvent<ItemStack> event){

        if(!(event.getObject().getItem() instanceof ItemSlashBlade))
            return;

        event.addCapability(BLADESTATE_KEY, new BladeStateCapabilityProvider());
    }
}
