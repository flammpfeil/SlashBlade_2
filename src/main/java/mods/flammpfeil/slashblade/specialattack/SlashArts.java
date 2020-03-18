package mods.flammpfeil.slashblade.specialattack;

import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.util.RegistryBase;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class SlashArts extends RegistryBase<SlashArts> {
    public enum ArtsType{
        Fail,
        Success,
        Jackpot
    }

    public static final SlashArts NONE = new SlashArts(BaseInstanceName);

    public static final SlashArts JUDGEMENT_CUT = new SlashArts("judgement_cut")
            .setComboState(ComboState.SLASH_ARTS_JC)
            .setArts(JudgementCut::doJudgementCut)
            .setArtsJust(JudgementCut::doJudgementCutJust);

    private ComboState comboState = ComboState.NONE;
    private ComboState comboStateJust = ComboState.NONE;

    private Consumer<LivingEntity> arts;
    private Consumer<LivingEntity> arts_just;

    public ComboState doArts(ArtsType type, LivingEntity user) {
        switch (type){
            case Jackpot:
                return doArtsJust(user);
            case Success:
                return doArts(user);
        }
        return ComboState.NONE;
    }

    public ComboState doArts(LivingEntity user) {
        arts.accept(user);
        return this.getComboState();
    }

    public SlashArts setArts(Consumer<LivingEntity> arts) {
        this.arts = arts;
        return this;
    }

    public ComboState doArtsJust(LivingEntity user) {
        arts_just.accept(user);
        ComboState result = this.getComboStateJust();
        return result != ComboState.NONE ? result : this.getComboState();
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
    public SlashArts setComboStateJust(ComboState state){
        this.comboStateJust = state;
        return this;
    }
    public ComboState getComboStateJust() {
        return this.comboStateJust;
    }
}
