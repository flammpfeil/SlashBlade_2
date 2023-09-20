package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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

    static final ResourceLocation EXEFFECT_ENCHANTMENT = new ResourceLocation(SlashBlade.modid, "enchantment/");
    static public void grantedIf(Enchantment enchantment, LivingEntity owner){
        int level = EnchantmentHelper.getEnchantmentLevel(enchantment, owner);
        if(0 < level) {
            grantCriterion(owner, EXEFFECT_ENCHANTMENT.withSuffix("root"));
            grantCriterion(owner, EXEFFECT_ENCHANTMENT.withSuffix(BuiltInRegistries.ENCHANTMENT.getKey(enchantment).getPath()));
        }
    }
}
