package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

public class BladeMaterialTooltips {
    private static final class SingletonHolder {
        private static final BladeMaterialTooltips instance = new BladeMaterialTooltips();
    }
    public static BladeMaterialTooltips getInstance() {
        return BladeMaterialTooltips.SingletonHolder.instance;
    }
    private BladeMaterialTooltips(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeathEvent(ItemTooltipEvent event) {
        List<ITextComponent> tooltip = event.getToolTip();

        ItemStack stack = event.getItemStack();

        AnvilCraftingRecipe recipe = AnvilCraftingRecipe.getRecipe(stack);

        if(recipe == null) return;

        ItemStack blade = ItemStack.EMPTY;


        if(event.getEntityPlayer() != null){
            if(event.getEntityPlayer().openContainer instanceof RepairContainer){
                blade = event.getEntityPlayer().openContainer.getSlot(0).getStack();
            }
        }

        tooltip.add(new TranslationTextComponent(
                "slashblade.tooltip.material").applyTextStyle(TextFormatting.DARK_AQUA));

        tooltip.add(getRequirements(recipe.getTranslationKey()
                ,recipe.getTranslationKey().equals(blade.getTranslationKey())));

        if(0 < recipe.getKillcount())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.killcount"
                    ,blade.getCapability(ItemSlashBlade.BLADESTATE).filter(s->recipe.getKillcount() <= s.getKillCount()).isPresent()
                    ,recipe.getKillcount()));

        if(0 < recipe.getRefine())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.refine"
                    ,blade.getCapability(ItemSlashBlade.BLADESTATE).filter(s->recipe.getRefine() <= s.getRefine()).isPresent()
                    ,recipe.getRefine()));

        if(recipe.isBroken())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.broken"
                    ,blade.getCapability(ItemSlashBlade.BLADESTATE).filter(s->s.isBroken()).isPresent()));

        if(recipe.isNoScabbard())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.noscabbard"
                    ,blade.getCapability(ItemSlashBlade.BLADESTATE).filter(s->s.isNoScabbard()).isPresent()));


        if(0 < recipe.getEnchantments().size())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.enchantments"
                    ,checkEnchantments(recipe.getEnchantments(), blade)
                    ,recipe.getLevel()));

        if(0 < recipe.getLevel())
            tooltip.add(getRequirements(
                    "slashblade.tooltip.material.level"
                    ,event.getEntityPlayer() != null && recipe.getLevel() <= event.getEntityPlayer().experienceLevel
                    ,recipe.getLevel()));
    }

    private boolean checkEnchantments(Map<Enchantment, Integer> requirements, ItemStack stack) {
        if(stack.isEmpty()) return false;
        if(!stack.isEnchanted()) return false;

        for(Map.Entry<Enchantment, Integer> entry : requirements.entrySet()){
            if(entry.getValue() > EnchantmentHelper.getEnchantmentLevel(entry.getKey(), stack)){
                return false;
            }
        }

        return true;
    }

    ITextComponent getRequirements(String key, boolean check, Object... args){
        ITextComponent tc = new TranslationTextComponent(key, args);

        if(check){
            tc.applyTextStyle(TextFormatting.GREEN);
        }

        return tc;
    }
}
