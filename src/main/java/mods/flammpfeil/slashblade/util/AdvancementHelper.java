package mods.flammpfeil.slashblade.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class AdvancementHelper {

    public static void grantCriterion(LivingEntity entity, ResourceLocation resourcelocation){
        if(entity instanceof ServerPlayer)
            grantCriterion((ServerPlayer) entity, resourcelocation);
    }

    public static void grantCriterion(ServerPlayer player, ResourceLocation resourcelocation){
        Advancement adv = player.getServer().getAdvancements().getAdvancement(resourcelocation);
        if(adv == null) return;

        AdvancementProgress advancementprogress = player.getAdvancements().getOrStartProgress(adv);
        if (advancementprogress.isDone()) return;

        for(String s : advancementprogress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, s);
        }
    }

}
