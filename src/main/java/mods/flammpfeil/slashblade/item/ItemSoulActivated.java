package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSoulActivated extends Item {
    public ItemSoulActivated(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 1200;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if(entity != null && entity instanceof Player && ((Player)entity).getInventory().isHotbarSlot(slot)){
            Player player = (Player) entity;

            MobEffectInstance effect = new MobEffectInstance(MobEffects.HUNGER, 200, 1, false, false);
            player.addEffect(effect);

            if(itemStack.getMaxDamage() <= itemStack.getDamageValue()){
                itemStack.shrink(1);
                player.getInventory().setItem(slot, new ItemStack(SBItems.proudsoul_awakened));
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 2.0F, 0.25F);

            }else if(player.hasEffect(MobEffects.HUNGER) && 0 < player.getFoodData().getFoodLevel()){
                player.causeFoodExhaustion(0.002F);
                setDamage(itemStack, Math.min(getMaxDamage(itemStack) , itemStack.getDamageValue() + 1));

            }
        }
    }

    //reverse
    @Override
    public boolean isBarVisible(ItemStack p_150899_) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack p_150900_) {
        return Math.round(/*13.0F - */(float)p_150900_.getDamageValue() * 13.0F / (float)this.getMaxDamage(p_150900_));
    }

    @Override
    public int getBarColor(ItemStack p_150901_) {
        float stackMaxDamage = this.getMaxDamage(p_150901_);
        float f = Math.max(0.0F, (stackMaxDamage - (float)p_150901_.getDamageValue()) / stackMaxDamage);
        return Mth.hsvToRgb((1.0f - f) / 3.0F, 1.0F, 1.0F);
    }
}
