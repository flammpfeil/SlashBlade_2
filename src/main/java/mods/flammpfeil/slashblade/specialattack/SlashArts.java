package mods.flammpfeil.slashblade.specialattack;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.RangeAttack;
import mods.flammpfeil.slashblade.capability.slashblade.combo.Extra;
import mods.flammpfeil.slashblade.util.RegistryBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SlashArts extends RegistryBase<SlashArts> {
    static Map<ResourceLocation, SlashArts> registry = Maps.newHashMap();

    @Override
    public Map<ResourceLocation, SlashArts> getRegistry() {
        return SlashArts.registry;
    }

    static public final int ChargeTicks = 9;
    static public final int ChargeJustTicks = 3;
    static public final int ChargeJustTicksMax = 5;

    static public int getJustReceptionSpan(LivingEntity user){
        return Math.min(ChargeJustTicksMax , ChargeJustTicks + EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED,user));
    }

    public enum ArtsType{
        Fail,
        Success,
        Jackpot,
        Broken
    }

    public static final SlashArts NONE = new SlashArts(BaseInstanceName, (e)->ComboState.NONE);

    public static final SlashArts JUDGEMENT_CUT =
            new SlashArts("judgement_cut", (e)-> e.onGround() ? Extra.EX_JUDGEMENT_CUT : Extra.EX_JUDGEMENT_CUT_SLASH_AIR)
                    .setComboStateJust((e)->Extra.EX_JUDGEMENT_CUT_SLASH_JUST)
                    .setComboStateBroken((e)->Extra.EX_VOID_SLASH);

    private Function<LivingEntity,ComboState> comboState;
    private Function<LivingEntity,ComboState> comboStateJust;
    private Function<LivingEntity,ComboState> comboStateBroken;

    public ComboState doArts(ArtsType type, LivingEntity user) {
        switch (type){
            case Jackpot:
                return getComboStateJust(user);
            case Success:
                return getComboState(user);
            case Broken:
                return getComboStateBroken(user);
        }
        return ComboState.NONE;
    }

    public SlashArts(String name, Function<LivingEntity,ComboState> state) {
        super(name);

        this.comboState = state;
        this.comboStateJust = state;
        this.comboStateBroken = state;
    }

    @Override
    public String getPath() {
        return "slasharts";
    }

    @Override
    public SlashArts getNone() {
        return NONE;
    }

    public ComboState getComboState(LivingEntity user) {
        return this.comboState.apply(user);
    }

    public ComboState getComboStateJust(LivingEntity user) {
        return this.comboStateJust.apply(user);
    }
    public SlashArts setComboStateJust(Function<LivingEntity,ComboState> state){
        this.comboStateJust = state;
        return this;
    }

    public ComboState getComboStateBroken(LivingEntity user) {
        return this.comboStateBroken.apply(user);
    }
    public SlashArts setComboStateBroken(Function<LivingEntity,ComboState> state){
        this.comboStateBroken = state;
        return this;
    }
}
