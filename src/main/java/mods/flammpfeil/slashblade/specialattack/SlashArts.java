package mods.flammpfeil.slashblade.specialattack;

import mods.flammpfeil.slashblade.ability.slasharts.JudgementCut;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.util.RegistryBase;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class SlashArts extends RegistryBase<SlashArts> {
    public static final SlashArts NONE = new SlashArts(BaseInstanceName);

    public static final SlashArts JUDGEMENT_CUT = new SlashArts("judgement_cut")
            .setComboState(ComboState.SLASH_ARTS_JC)
            .setArts(JudgementCut::doJudgementCut)
            .setArtsJust(JudgementCut::doJudgementCut);

    private ComboState comboState = ComboState.NONE;

    private Consumer<LivingEntity> arts;
    private Consumer<LivingEntity> arts_just;

    public void doArts(LivingEntity user) {
        arts.accept(user);
    }

    public SlashArts setArts(Consumer<LivingEntity> arts) {
        this.arts = arts;
        return this;
    }

    public void doArtsJust(LivingEntity user) {
        arts_just.accept(user);
    }

    public SlashArts setArtsJust(Consumer<LivingEntity> arts) {
        this.arts_just = arts;
        return this;
    }

    public SlashArts(String name) {
        super(name);
    }

    @Override
    public String getPath() {
        return "slasharts";
    }

    @Override
    public SlashArts getNone() {
        return NONE;
    }

    public SlashArts setComboState(ComboState state){
        this.comboState = state;
        return this;
    }
    public ComboState getComboState() {
        return this.comboState;
    }
}
