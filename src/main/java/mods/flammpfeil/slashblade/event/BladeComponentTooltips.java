package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;

public class BladeComponentTooltips {
    private static final class SingletonHolder {
        private static final BladeComponentTooltips instance = new BladeComponentTooltips();
    }
    public static BladeComponentTooltips getInstance() {
        return SingletonHolder.instance;
    }
    private BladeComponentTooltips(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemTooltipEvent(ItemTooltipEvent event) {
        List<Component> tooltip = event.getToolTip();

        ItemStack stack = event.getItemStack();

        AnvilCraftingRecipe recipe = AnvilCraftingRecipe.getRecipe(stack);

        if(recipe == null) return;

        ItemStack blade = ItemStack.EMPTY;

        boolean hasAnvil = false;

        if(event.getEntity() != null){
            if(event.getEntity().containerMenu instanceof AnvilMenu){
                hasAnvil = true;
                blade = event.getEntity().containerMenu.getSlot(0).getItem();
            }
        }

        tooltip.add(Component.translatable(
                "slashblade.tooltip.material").withStyle(ChatFormatting.DARK_AQUA));

        tooltip.add(getRequirements(
                "slashblade.tooltip.material.requiredobjects.anvil"
                ,hasAnvil));

        tooltip.add(getRequirements(recipe.getTranslationKey()
                ,recipe.getTranslationKey().equals(blade.getDescriptionId())));

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
                    ,event.getEntity() != null && recipe.getLevel() <= event.getEntity().experienceLevel
                    ,recipe.getLevel()));
    }

    private boolean checkEnchantments(Map<Enchantment, Integer> requirements, ItemStack stack) {
        if(stack.isEmpty()) return false;
        if(!stack.isEnchanted()) return false;

        for(Map.Entry<Enchantment, Integer> entry : requirements.entrySet()){
            if(entry.getValue() > EnchantmentHelper.getItemEnchantmentLevel(entry.getKey(), stack)){
                return false;
            }
        }

        return true;
    }

    Component getRequirements(String key, boolean check, Object... args){
        Component tc = Component.translatable(key, args);

        if(check){
            tc = Component.empty().append(tc).withStyle(ChatFormatting.GREEN);
        }

        return tc;
    }
}
