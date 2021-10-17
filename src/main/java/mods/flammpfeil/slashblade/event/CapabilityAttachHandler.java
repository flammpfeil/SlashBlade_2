package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.inputstate.InputStateCapabilityProvider;
import mods.flammpfeil.slashblade.capability.mobeffect.MobEffectCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityAttachHandler {

    static public final ResourceLocation MOBEFFECT_KEY = new ResourceLocation(SlashBlade.modid, "mobeffect");
    static public final ResourceLocation INPUTSTATE_KEY = new ResourceLocation(SlashBlade.modid, "inputstate");
    static public final ResourceLocation RANK_KEY = new ResourceLocation(SlashBlade.modid, "concentration");
    @SubscribeEvent
    public void AttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
        if(!(event.getObject() instanceof LivingEntity)) return;

        event.addCapability(INPUTSTATE_KEY,new InputStateCapabilityProvider());
        event.addCapability(MOBEFFECT_KEY, new MobEffectCapabilityProvider());
        event.addCapability(RANK_KEY, new ConcentrationRankCapabilityProvider());
    }

    static public final ResourceLocation BLADESTATE_KEY = new ResourceLocation(SlashBlade.modid, "bladestate");
    @SubscribeEvent
    public void AttachCapabilities_ItemStack(AttachCapabilitiesEvent<ItemStack> event){

        if(!(event.getObject().getItem() instanceof ItemSlashBlade))
            return;

        event.addCapability(BLADESTATE_KEY, new BladeStateCapabilityProvider());
    }
}
