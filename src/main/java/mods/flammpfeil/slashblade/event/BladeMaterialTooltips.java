package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
        return SingletonHolder.instance;
    }
    private BladeMaterialTooltips(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    static public final String BLADE_DATA = "BladeData";

    @SubscribeEvent
    public void onItemTooltipEvent(ItemTooltipEvent event) {
        List<ITextComponent> tooltip = event.getToolTip();

        ItemStack stack = event.getItemStack();

        if(stack.hasTag() && stack.getTag().contains(BLADE_DATA)){
            CompoundNBT bladeData = stack.getTag().getCompound(BLADE_DATA);

            String translationKey = NBTHelper.getNBTCoupler(bladeData)
                    .getChild("tag")
                    .getChild("ShareTag")
                    .getRawCompound().getString("translationKey");

            event.getToolTip().add(new TranslationTextComponent(translationKey));
        }
    }

}
