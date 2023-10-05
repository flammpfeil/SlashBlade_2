package mods.flammpfeil.slashblade.capability.slashblade.combo;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.event.FallHandler;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialattack.JudgementCut;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mods.flammpfeil.slashblade.init.DefaultResources.ExMotionLocation;

import net.minecraft.world.entity.Entity.RemovalReason;

public class Extra {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>(){});

    static List<Map.Entry<EnumSet<InputCommand>, Supplier<ComboState>>> ex_standbyMap =
            new HashMap<EnumSet<InputCommand>, Supplier<ComboState>>(){{
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.SNEAK, InputCommand.FORWARD, InputCommand.R_CLICK),
                        () -> EX_RAPID_SLASH);
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.L_CLICK),
                        () -> EX_COMBO_A1);
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.BACK, InputCommand.SNEAK, InputCommand.R_CLICK),
                        () -> EX_UPPERSLASH);

                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.R_CLICK),
                        () -> EX_COMBO_A1);

                this.put(EnumSet.of(InputCommand.ON_AIR, InputCommand.SNEAK, InputCommand.BACK, InputCommand.R_CLICK),
                        () -> EX_AERIAL_CLEAVE);
                this.put(EnumSet.of(InputCommand.ON_AIR),
                        () -> EX_AERIAL_RAVE_A1);
            }}.entrySet().stream()
                    .collect(Collectors.toList());


    static public final ResourceLocation ADVANCEMENT_COMBO_A = new ResourceLocation(SlashBlade.modid, "arts/combo_a");
    static public final ResourceLocation ADVANCEMENT_COMBO_A_EX = new ResourceLocation(SlashBlade.modid, "arts/combo_a_ex");
    static public final ResourceLocation ADVANCEMENT_COMBO_B = new ResourceLocation(SlashBlade.modid, "arts/combo_b");
    static public final ResourceLocation ADVANCEMENT_COMBO_B_MAX = new ResourceLocation(SlashBlade.modid, "arts/combo_b_max");
    static public final ResourceLocation ADVANCEMENT_COMBO_C = new ResourceLocation(SlashBlade.modid, "arts/combo_c");
    static public final ResourceLocation ADVANCEMENT_AERIAL_A = new ResourceLocation(SlashBlade.modid, "arts/aerial_a");
    static public final ResourceLocation ADVANCEMENT_AERIAL_B = new ResourceLocation(SlashBlade.modid, "arts/aerial_b");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH = new ResourceLocation(SlashBlade.modid, "arts/upperslash");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH_JUMP = new ResourceLocation(SlashBlade.modid, "arts/upperslash_jump");
    static public final ResourceLocation ADVANCEMENT_AERIAL_CLEAVE = new ResourceLocation(SlashBlade.modid, "arts/aerial_cleave");
    static public final ResourceLocation ADVANCEMENT_RISING_STAR = new ResourceLocation(SlashBlade.modid, "arts/rising_star");
    static public final ResourceLocation ADVANCEMENT_RAPID_SLASH = new ResourceLocation(SlashBlade.modid, "arts/rapid_slash");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT = new ResourceLocation(SlashBlade.modid, "arts/judgement_cut");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT_JUST = new ResourceLocation(SlashBlade.modid, "arts/judgement_cut_just");
    static public final ResourceLocation ADVANCEMENT_QUICK_CHARGE = new ResourceLocation(SlashBlade.modid, "arts/quick_charge");
    //=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=

    public static void playQuickSheathSoundAction(LivingEntity e) {
        e.playSound(SoundEvents.CHAIN_HIT, 1.0F,1.0F);
    }

    public static final ComboState STANDBY_EX = new ComboState("standby", 10,
            ()->0,()->1,()->1.0f,()->true,()->1000,
            ExMotionLocation, (a)-> {

                    EnumSet<InputCommand> commands =
                            a.getCapability(ComboState.INPUT_STATE).map((state)->state.getCommands(a)).orElseGet(()-> EnumSet.noneOf(InputCommand.class));

                    return ex_standbyMap.stream()
                            .filter((entry)->commands.containsAll(entry.getKey()))
                            //.findFirst()
                            .min(Comparator.comparingInt((entry)-> entry.getValue().get().getPriority()))
                            .map((entry)->entry.getValue().get())
                            .orElseGet(()->ComboState.NONE);

                }, ()-> ComboState.NONE);


    public static final ComboState STANDBY_INAIR = new ComboState("standby_inair", 10,
                ()->0,()->1,()->1.0f,()->true,()->400,
                ExMotionLocation, (a)-> ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(FallHandler::fallDecrease);

    public static final ComboState EX_COMBO_A1 = new ComboState("ex_combo_a1",100,
            ()->1,()->10,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(5, (a)->Extra.EX_COMBO_A2), ()-> Extra.EX_COMBO_A1_END)
            .setClickAction((e)-> AttackManager.doSlash(e,  -10,true))
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_A1_END = new ComboState("ex_combo_a1_end",100,
            ()->10,()->21,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_COMBO_A2, ()-> Extra.EX_COMBO_A1_END2)
            .setReleaseAction(ComboState::releaseActionQuickCharge);
    public static final ComboState EX_COMBO_A1_END2 = new ComboState("ex_combo_a1_end2",100,
            ()->21,()->41,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE);


    public static final ComboState EX_COMBO_A2 = new ComboState("ex_combo_a2",100,
            ()->100,()->115,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(5,(a)->Extra.EX_COMBO_A3), ()-> Extra.EX_COMBO_A2_END)
            .setClickAction((e)-> AttackManager.doSlash(e,  180-10,true))
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_A2_END = new ComboState("ex_combo_a2_end",100,
            ()->115,()->132,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_COMBO_C, ()-> Extra.EX_COMBO_A2_END2)
            .setReleaseAction(ComboState::releaseActionQuickCharge);
    public static final ComboState EX_COMBO_A2_END2 = new ComboState("ex_combo_a2_end2",100,
            ()->132,()->151,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE);


    public static final ComboState EX_COMBO_C = new ComboState("ex_combo_c",100,
            ()->400,()->459,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(15,(a)->ComboState.NONE), ()-> Extra.EX_COMBO_C_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -30))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  -35, true))
                    .build())
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_COMBO_C));
    public static final ComboState EX_COMBO_C_END = new ComboState("ex_combo_c_end",100,
            ()->459,()->488,()->1.0f,()->false,()->0,
            ExMotionLocation,(a)-> ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_COMBO_A3 = new ComboState("ex_combo_a3",100,
            ()->200,()->218,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(9,(a)-> (a.hasEffect(MobEffects.DAMAGE_BOOST) || a.hasEffect(MobEffects.HUNGER)) ? Extra.EX_COMBO_A4EX : Extra.EX_COMBO_A4) , ()-> Extra.EX_COMBO_A3_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -61))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  180-42))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_A3_END = new ComboState("ex_combo_a3_end",100,
            ()->218,()->230,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_COMBO_B1, ()-> Extra.EX_COMBO_A3_END2);
    public static final ComboState EX_COMBO_A3_END2 = new ComboState("ex_combo_a3_end2",100,
            ()->230,()->281,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> Extra.EX_COMBO_A3_END3);
    public static final ComboState EX_COMBO_A3_END3 = new ComboState("ex_combo_a3_end3",100,
            ()->281,()->314,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_COMBO_A4 = new ComboState("ex_combo_a4",100,
            ()->500,()->576,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(21,(a)->ComboState.NONE), ()-> Extra.EX_COMBO_A4_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(8, (entityIn)->AttackManager.doSlash(entityIn,  45))
                    .put(9, (entityIn)->AttackManager.doSlash(entityIn,  50, true))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(8+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(8+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(8+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(8+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(8+4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(8+5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_COMBO_A));
    public static final ComboState EX_COMBO_A4_END = new ComboState("ex_combo_a4_end",100,
            ()->576,()->608,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_COMBO_A4EX = new ComboState("ex_combo_a4ex",100,
            ()->800,()->839,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(22,(a)-> Extra.EX_COMBO_A5EX) , ()-> Extra.EX_COMBO_A4EX_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(7, (entityIn)->AttackManager.doSlash(entityIn,  70))
                    .put(14, (entityIn)->AttackManager.doSlash(entityIn,  180+75))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_A4EX_END = new ComboState("ex_combo_a4ex_end",100,
            ()->839,()->877,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> Extra.EX_COMBO_A4EX_END2);
    public static final ComboState EX_COMBO_A4EX_END2 = new ComboState("ex_combo_a4ex_end2",100,
            ()->877,()->894,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);

    public static final ComboState EX_COMBO_A5EX = new ComboState("ex_combo_a5ex",100,
            ()->900,()->1013,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(33,(a)->ComboState.NONE), ()-> Extra.EX_COMBO_A5EX_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(15, (entityIn)->AttackManager.doSlash(entityIn,  35,false,true))
                    .put(17, (entityIn)->AttackManager.doSlash(entityIn,  40,true,true))
                    .put(19, (entityIn)->AttackManager.doSlash(entityIn,  30,true,true))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(13+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(13+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(13+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(13+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(13+4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(13+5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_COMBO_A_EX));
    public static final ComboState EX_COMBO_A5EX_END = new ComboState("ex_combo_a5ex_end",100,
            ()->1013,()->1061,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    private final static float rushDamageBase = 0.1f;
    public static final ComboState EX_COMBO_B1 = new ComboState("ex_combo_b1",100,
            ()->700,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation,  ComboState.TimeoutNext.buildFromFrame(13, (a)-> Extra.EX_COMBO_B2) , ()-> Extra.EX_COMBO_B1_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(6, (entityIn)->{
                        AttackManager.doSlash(entityIn,  -30, false, false, 0.25f);
                        AttackManager.doSlash(entityIn,  180-35, true, false, 0.25f);
                    })
                    .put(7+0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(7+1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(7+2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(7+3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(7+4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(7+5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(7+6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(7+7, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))

                    .build())
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_COMBO_B));

    public static final ComboState EX_COMBO_B1_END = new ComboState("ex_combo_b1_end",100,
            ()->720,()->743,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_COMBO_B1_END, ()-> Extra.EX_COMBO_B1_END2)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12 -3, (entityIn)->AttackManager.doSlash(entityIn,  0, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), false, true,1.0))
                    .put(13 -3, (entityIn)->AttackManager.doSlash(entityIn,  5, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), true, false,1.0))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12-3 +0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addHitEffect(StunManager::setStun);

    public static final ComboState EX_COMBO_B1_END2 = new ComboState("ex_combo_b1_end2",100,
            ()->743,()->764,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> Extra.EX_COMBO_B1_END3);
    public static final ComboState EX_COMBO_B1_END3 = new ComboState("ex_combo_b1_end3",100,
            ()->764,()->787,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build());


    public static Vec3 genRushOffset(LivingEntity entityIn){
        return new Vec3(entityIn.getRandom().nextFloat()-0.5f,entityIn.getRandom().nextFloat()-0.5f,0).scale(2.0);
    }

    public static final ComboState EX_COMBO_B2 = new ComboState("ex_combo_b2",100,
            ()->710,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6, (a)-> Extra.EX_COMBO_B3)  , ()-> Extra.EX_COMBO_B_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_B3 = new ComboState("ex_combo_b3",100,
            ()->710,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6, (a)-> Extra.EX_COMBO_B4)  , ()-> Extra.EX_COMBO_B_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_B4 = new ComboState("ex_combo_b4",100,
            ()->710,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6, (a)-> Extra.EX_COMBO_B5)  , ()-> Extra.EX_COMBO_B_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_B5 = new ComboState("ex_combo_b5",100,
            ()->710,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6, (a)-> Extra.EX_COMBO_B6)  , ()-> Extra.EX_COMBO_B_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_B6 = new ComboState("ex_combo_b6",100,
            ()->710,()->720,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6, (a)-> Extra.EX_COMBO_B7)  , ()-> Extra.EX_COMBO_B_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .build())
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_COMBO_B7 = new ComboState("ex_combo_b7",100,
            ()->710,()->764,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(33,(a)->ComboState.NONE), ()-> Extra.EX_COMBO_B7_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(1, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(2, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(3, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(4, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(5, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))
                    .put(6, (entityIn)->AttackManager.doSlash(entityIn,  -90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), false, false, rushDamageBase))
                    .put(7, (entityIn)->AttackManager.doSlash(entityIn,  +90 + 180 * entityIn.getRandom().nextFloat(), genRushOffset(entityIn), true , false, rushDamageBase))

                    .put(12, (entityIn)->AttackManager.doSlash(entityIn,  0, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), false, true,1.0))
                    .put(13, (entityIn)->AttackManager.doSlash(entityIn,  5, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), true, false,1.0))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12 +0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 +1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 +2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 +3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 +4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12 +5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_COMBO_B_MAX));
    public static final ComboState EX_COMBO_B7_END = new ComboState("ex_combo_b7_end",100,
            ()->764,()->787,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);

    public static final ComboState EX_COMBO_B_END = new ComboState("ex_combo_b_end",100,
            ()->720,()->743,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_COMBO_B_END, ()-> Extra.EX_COMBO_B_END2)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12 -3, (entityIn)->AttackManager.doSlash(entityIn,  0, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), false, true,1.0))
                    .put(13 -3, (entityIn)->AttackManager.doSlash(entityIn,  5, new Vec3(entityIn.getRandom().nextFloat()-0.5f,0.8f,0), true, false,1.0))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(12-3 +0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(12-3 +5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addHitEffect(StunManager::setStun);

    public static final ComboState EX_COMBO_B_END2 = new ComboState("ex_combo_b_end2",100,
            ()->743,()->764,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> Extra.EX_COMBO_B_END3);
    public static final ComboState EX_COMBO_B_END3 = new ComboState("ex_combo_b_end3",100,
            ()->764,()->787,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    //-------------------------------------------------------


    public static final ComboState EX_AERIAL_RAVE_A1 = new ComboState("ex_aerial_rave_a1",80,
            ()->1100,()->1122,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(5, (a)->Extra.EX_AERIAL_RAVE_A2), ()-> Extra.EX_AERIAL_RAVE_A1_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(3)+0, (entityIn)->AttackManager.doSlash(entityIn,  -20))
                    .build()
                    .andThen(FallHandler::fallDecrease))
            .addHitEffect(StunManager::setStun)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .setIsAerial();
    public static final ComboState EX_AERIAL_RAVE_A1_END = new ComboState("ex_aerial_rave_a1_end",80,
            ()->1122,()->1132,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_AERIAL_RAVE_A2 = new ComboState("ex_aerial_rave_a2",80,
            ()->1200,()->1210,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(5,(a)->Extra.EX_AERIAL_RAVE_A3), ()-> Extra.EX_AERIAL_RAVE_A2_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(3)+0, (entityIn)->AttackManager.doSlash(entityIn,  180-30))
                    .build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial();
    public static final ComboState EX_AERIAL_RAVE_A2_END = new ComboState("ex_aerial_rave_a2_end",80,
            ()->1210,()->1231,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_AERIAL_RAVE_B3, ()-> Extra.EX_AERIAL_RAVE_A2_END2)
            .addTickAction(FallHandler::fallDecrease)
            .setIsAerial();
    public static final ComboState EX_AERIAL_RAVE_A2_END2 = new ComboState("ex_aerial_rave_a2_end2",80,
            ()->1231,()->1241,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_AERIAL_RAVE_A3 = new ComboState("ex_aerial_rave_a3",80,
            ()->1300,()->1328,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(9,(a)->ComboState.NONE), ()-> Extra.EX_AERIAL_RAVE_A3_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(4)+0, (entityIn)->AttackManager.doSlash(entityIn,  0,Vec3.ZERO, false, false, 1.0, KnockBacks.smash))
                    .put((int)TimeValueHelper.getTicksFromFrames(4)+1, (entityIn)->AttackManager.doSlash(entityIn,  -3,Vec3.ZERO, true, true, 1.0, KnockBacks.smash))
                    .build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial()
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_AERIAL_A));
    public static final ComboState EX_AERIAL_RAVE_A3_END = new ComboState("ex_aerial_rave_a3_end",80,
            ()->1328,()->1338,()->1.0f,()->false,()->0,
            ExMotionLocation,(a)-> ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_AERIAL_RAVE_B3 = new ComboState("ex_aerial_rave_b3",80,
            ()->1400,()->1437,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(13,(a)->Extra.EX_AERIAL_RAVE_B4) , ()-> Extra.EX_AERIAL_RAVE_B3_END)
            .setClickAction((entityIn)->{
                Vec3 motion = entityIn.getDeltaMovement();
                entityIn.setDeltaMovement(motion.x, 0.6, motion.z);})
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(5), (entityIn)->AttackManager.doSlash(entityIn,  180+57,Vec3.ZERO, false, false, 1.0, KnockBacks.toss))
                    .put((int)TimeValueHelper.getTicksFromFrames(10), (entityIn)->AttackManager.doSlash(entityIn,  180+57,Vec3.ZERO, false, false, 1.0, KnockBacks.toss))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->UserPoseOverrider.setRot(entityIn, -90, true))
                    .put(1, (entityIn)->UserPoseOverrider.setRot(entityIn, -90, true))
                    .put(2, (entityIn)->UserPoseOverrider.setRot(entityIn, -90, true))
                    .put(3, (entityIn)->UserPoseOverrider.setRot(entityIn, -90, true))
                    .put(4, (entityIn)->UserPoseOverrider.setRot(entityIn, -120, true))
                    .put(5, (entityIn)->UserPoseOverrider.setRot(entityIn, -120, true))
                    .put(6, (entityIn)->UserPoseOverrider.setRot(entityIn, -120, true))
                    .put(7, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial();
    public static final ComboState EX_AERIAL_RAVE_B3_END = new ComboState("ex_aerial_rave_b3_end",80,
            ()->1437,()->1443,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_AERIAL_RAVE_B4 = new ComboState("ex_aerial_rave_b4",80,
            ()->1500,()->1537,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(15,(a)->ComboState.NONE), ()-> Extra.EX_AERIAL_RAVE_B4_END)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(10)+0, (entityIn)->AttackManager.doSlash(entityIn,  45,Vec3.ZERO, false, false, 1.0, KnockBacks.meteor))
                    .put((int)TimeValueHelper.getTicksFromFrames(10)+1, (entityIn)->AttackManager.doSlash(entityIn,  50,Vec3.ZERO, true, true, 1.0, KnockBacks.meteor))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(5+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(5+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(5+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(5+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(5+4, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial()
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_AERIAL_B));
    public static final ComboState EX_AERIAL_RAVE_B4_END = new ComboState("ex_aerial_rave_b4_end",80,
            ()->1537,()->1547,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    //-------------------------------------------------------

    private static final EnumSet<InputCommand> ex_upperslash_command = EnumSet.of(InputCommand.BACK, InputCommand.R_DOWN);
    public static final ComboState EX_UPPERSLASH = new ComboState("ex_upperslash",90,
            ()->1600, ()->1659, ()->1.0f, ()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(11,(a)->ComboState.NONE), ()-> Extra.EX_UPPERSLASH_END)
            .addHoldAction((player) -> {
                int elapsed = player.getTicksUsingItem();

                int fireTime = (int)TimeValueHelper.getTicksFromFrames(9);
                if(fireTime != elapsed) return;

                EnumSet<InputCommand> commands =
                        player.getCapability(INPUT_STATE).map((state)->state.getCommands(player)).orElseGet(()-> EnumSet.noneOf(InputCommand.class));

                if (!commands.containsAll(ex_upperslash_command)) return;

                player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                    state.updateComboSeq(player, Extra.EX_UPPERSLASH_JUMP);
                    AdvancementHelper.grantCriterion(player,ADVANCEMENT_UPPERSLASH_JUMP);
                });
            })
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(7), (entityIn)->AttackManager.doSlash(entityIn,  -80,Vec3.ZERO, false, false, 1.0, KnockBacks.toss))
                    .build())
            .addHitEffect((t,a)->StunManager.setStun(t, 15))
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_UPPERSLASH));
    public static final ComboState EX_UPPERSLASH_END = new ComboState("ex_upperslash_end",90,
            ()->1659, ()->1693, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_UPPERSLASH_JUMP = new ComboState("ex_upperslash_jump",90,
            ()->1700, ()->1713, ()->1.0f, ()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(7,(a)->ComboState.NONE), ()-> Extra.EX_UPPERSLASH_JUMP_END)
            .setClickAction((entityIn)->{
                Vec3 motion = entityIn.getDeltaMovement();
                entityIn.setDeltaMovement(motion.x, 0.6f, motion.z);

                entityIn.setOnGround(false);
                entityIn.hasImpulse = true;
            })
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(1, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(2, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(3, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(4, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial();
    public static final ComboState EX_UPPERSLASH_JUMP_END = new ComboState("ex_upperslash_jump_end",90,
            ()->1713, ()->1717, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build()
                    .andThen(FallHandler::fallDecrease));

    //-------------------------------------------------------

    public static final ComboState EX_AERIAL_CLEAVE = new ComboState("ex_aerial_cleave",70,
            ()->1800, ()->1812, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_AERIAL_CLEAVE, ()-> Extra.EX_AERIAL_CLEAVE_LOOP)
            .setClickAction((e)->{
                Vec3 motion = e.getDeltaMovement();
                e.setDeltaMovement(motion.x, 0.1, motion.z);

                AdvancementHelper.grantCriterion(e,ADVANCEMENT_AERIAL_CLEAVE);
            })
            .addTickAction((e)->{
                e.fallDistance = 1;

                long elapsed = ComboState.getElapsed(e);

                if(elapsed == 2){
                    e.level().playSound((Player)null, e.getX(), e.getY(), e.getZ(),
                            SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.75F, 1.0F);
                }

                if(2 < elapsed) {
                    Vec3 motion = e.getDeltaMovement();
                    e.setDeltaMovement(motion.x, motion.y - 3.0, motion.z);
                }

                if(elapsed % 2 == 0)
                    AttackManager.areaAttack(e, KnockBacks.meteor.action,0.1f,true,false,true);

                if(e.onGround()){
                    AttackManager.doSlash(e,  55,Vec3.ZERO, true, true, 1.0, KnockBacks.meteor);
                    e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        state.updateComboSeq(e,Extra.EX_AERIAL_CLEAVE_LANDING);
                        FallHandler.spawnLandingParticle(e, 20);
                    });
                }

                if(elapsed == 1){
                    e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        if(state.getComboSeq() == Extra.EX_AERIAL_CLEAVE){
                            state.updateComboSeq(e,Extra.EX_AERIAL_CLEAVE_LOOP);
                        }
                    });
                }
            })
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(1, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(2, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(3, (entityIn)->UserPoseOverrider.setRot(entityIn, 90, true))
                    .put(4, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build());
    //fall loop 1sec timeout
    public static final ComboState EX_AERIAL_CLEAVE_LOOP = new ComboState("ex_aerial_cleave_loop",70,
            ()->1812, ()->1817, ()->1.0f, ()->true,()->1000,
            ExMotionLocation, (a)->Extra.EX_AERIAL_CLEAVE_LOOP, ()-> ComboState.NONE)
            .addTickAction((e)->{
                e.fallDistance = 1;

                Vec3 motion = e.getDeltaMovement();
                e.setDeltaMovement(motion.x, motion.y - 3.0, motion.z);

                long elapsed = ComboState.getElapsed(e);

                if(elapsed % 2 == 0)
                    AttackManager.areaAttack(e, KnockBacks.meteor.action,0.1f,true,false,true);

                if(e.onGround()){
                    AttackManager.doSlash(e,  55, Vec3.ZERO, true, true, 1.0, KnockBacks.meteor);
                    e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        state.updateComboSeq(e,Extra.EX_AERIAL_CLEAVE_LANDING);
                        FallHandler.spawnLandingParticle(e, 20);
                    });
                }
            })
            .addHitEffect((t,a)->StunManager.setStun(t, 15));
    public static final ComboState EX_AERIAL_CLEAVE_LANDING = new ComboState("ex_aerial_cleave_landing",70,
            ()->1816, ()->1859, ()->1.0f, ()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(6,(a)->ComboState.NONE), ()-> Extra.EX_AERIAL_CLEAVE_END)
            .setClickAction((entityIn)->AttackManager.doSlash(entityIn,  60, Vec3.ZERO, false, false, 1.0, KnockBacks.meteor))
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn));
    public static final ComboState EX_AERIAL_CLEAVE_END = new ComboState("ex_aerial_cleave_end",70,
            ()->1859, ()->1886, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    //-------------------------------------------------------


    public static final ComboState EX_RAPID_SLASH = new ComboState("ex_rapid_slash",70,
            ()->2000, ()->2019, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)-> (a.hasEffect(MobEffects.DAMAGE_BOOST) || a.hasEffect(MobEffects.HUNGER)) ? Extra.EX_RAPID_SLASH_QUICK : Extra.EX_RAPID_SLASH, ()-> Extra.EX_RAPID_SLASH_END)
            .addHoldAction((e)->{
                AttributeModifier am = new AttributeModifier("SweepingDamageRatio", -3, AttributeModifier.Operation.ADDITION);
                AttributeInstance mai = e.getAttribute(ForgeMod.ENTITY_REACH.get());
                mai.addTransientModifier(am);
                AttackManager.areaAttack(e, (t)->{
                        boolean isRightDown = e.getCapability(INPUT_STATE)
                                .map((state)->state.getCommands().contains(InputCommand.R_DOWN))
                                .orElse(false);

                        if(isRightDown) {
                            e.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                                if (state.getComboSeq() == Extra.EX_RAPID_SLASH) {
                                    List<Entity> hits = AttackManager.areaAttack(e, KnockBacks.toss.action,0.01f,true,true,true);

                                    if(!hits.isEmpty()) {
                                        state.updateComboSeq(e, Extra.EX_RISING_STAR);
                                        AdvancementHelper.grantCriterion(e,ADVANCEMENT_RISING_STAR);
                                    }
                                }
                            });
                        }
                    }, 0.001f, true, false, true);
                mai.removeModifier(am);
            })
            .addTickAction((e)->{
                long elapsed = ComboState.getElapsed(e);

                if(elapsed == 0){
                    e.level().playSound((Player) null,e.getX(), e.getY(), e.getZ(),
                            SoundEvents.ARMOR_EQUIP_IRON,
                            SoundSource.PLAYERS,1.0F,1.0F);
                }

                if(elapsed <= 3 && e.onGround())
                    e.moveRelative( e.isInWater() ? 0.35f : 0.8f , new Vec3(0, 0, 1));

                if(2 <= elapsed && elapsed < 6){
                    float roll = -45 + 90 * e.getRandom().nextFloat();

                    if(elapsed % 2 == 0)
                        roll += 180;

                    boolean critical = e.hasEffect(MobEffects.DAMAGE_BOOST);

                    AttackManager.doSlash(e,  roll, genRushOffset(e), false, critical, rushDamageBase);
                }

                if(elapsed == 7) {
                    AttackManager.doSlash(e, -30, genRushOffset(e), false, true, rushDamageBase);
                }

                if(7 <= elapsed && elapsed <= 10){
                    UserPoseOverrider.setRot(e, 90, true);
                }
                if(10 < elapsed){
                    UserPoseOverrider.setRot(e, 0, false);
                }
            })
            .addHitEffect(StunManager::setStun)
            .setClickAction(a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_RAPID_SLASH));
    public static final ComboState EX_RAPID_SLASH_QUICK = new ComboState("ex_rapid_slash_quick",70,
            ()->2000, ()->2001, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->Extra.EX_RAPID_SLASH_QUICK, ()-> Extra.EX_RAPID_SLASH);
    public static final ComboState EX_RAPID_SLASH_END = new ComboState("ex_rapid_slash_end",70,
            ()->2019, ()->2054, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> Extra.EX_RAPID_SLASH_END2);
    public static final ComboState EX_RAPID_SLASH_END2 = new ComboState("ex_rapid_slash_end2",70,
            ()->2054, ()->2073, ()->1.0f, ()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_RISING_STAR = new ComboState("ex_rising_star",80,
            ()->2100,()->2137,()->1.0f,()->false,()->0,
            ExMotionLocation, ComboState.TimeoutNext.buildFromFrame(18,(a)->ComboState.NONE) , ()-> Extra.EX_RISING_STAR_END)
            .setClickAction((entityIn)->{
                entityIn.setDeltaMovement(0, 0.6, 0);
                entityIn.setOnGround(false);
                entityIn.hasImpulse = true;
                AttackManager.doSlash(entityIn,  -57,Vec3.ZERO, false, false, 1.0, KnockBacks.toss);
            })
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put((int)TimeValueHelper.getTicksFromFrames(9), (entityIn)->AttackManager.doSlash(entityIn,  -57,Vec3.ZERO, false, false, 1.0, KnockBacks.toss))
                    .build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(0+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(0+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(0+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(0+4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+4, (entityIn)->UserPoseOverrider.setRot(entityIn, 72, true))
                    .put(5+5, (entityIn)->UserPoseOverrider.resetRot(entityIn))
                    .build())
            .addTickAction((entityIn)->{

                        long elapsed = ComboState.getElapsed(entityIn);

                        if(elapsed < 3){
                            Vec3 motion = entityIn.getDeltaMovement();

                            double yMotion = motion.y;
                            if(yMotion <= 0) {
                                yMotion = 0.6;

                                entityIn.setOnGround(false);
                                entityIn.hasImpulse = true;
                            }

                            entityIn.setDeltaMovement(0, yMotion, 0);
                        }
                    })
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun)
            .setIsAerial();
    public static final ComboState EX_RISING_STAR_END = new ComboState("ex_rising_star_end",80,
            ()->2137,()->2147,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)->ComboState.NONE, ()-> ComboState.NONE)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(0,Extra::playQuickSheathSoundAction).build())
            .addTickAction(FallHandler::fallDecrease)
            .setReleaseAction(ComboState::releaseActionQuickCharge);

    //------------------------------------------------------------------------

    public static final ComboState EX_JUDGEMENT_CUT = new ComboState("ex_judgement_cut",50,
            ()->1900,()->1923,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_JUDGEMENT_CUT, ()->Extra.EX_JUDGEMENT_CUT_SLASH)
            .addTickAction((e)->{

                long elapsed = ComboState.getElapsed(e);

                if(elapsed == 0){
                    e.playSound(SoundEvents.TRIDENT_THROW, 0.80F, 0.625F + 0.1f * e.getRandom().nextFloat());
                    AdvancementHelper.grantCriterion(e,ADVANCEMENT_JUDGEMENT_CUT);
                }

                if(elapsed <= 3) {
                    e.moveRelative(-0.3f, new Vec3(0, 0, 1));
                    Vec3 vec = e.getDeltaMovement();
                    {
                        double d0 = vec.x;
                        double d1 = vec.z;
                        double d2 = 0.05D;

                        while (d0 != 0.0D && e.level().noCollision(e, e.getBoundingBox().move(d0, (double) (-e.maxUpStep()), 0.0D))) {
                            if (d0 < 0.05D && d0 >= -0.05D) {
                                d0 = 0.0D;
                            } else if (d0 > 0.0D) {
                                d0 -= 0.05D;
                            } else {
                                d0 += 0.05D;
                            }
                        }

                        while (d1 != 0.0D && e.level().noCollision(e, e.getBoundingBox().move(0.0D, (double) (-e.maxUpStep()), d1))) {
                            if (d1 < 0.05D && d1 >= -0.05D) {
                                d1 = 0.0D;
                            } else if (d1 > 0.0D) {
                                d1 -= 0.05D;
                            } else {
                                d1 += 0.05D;
                            }
                        }

                        while (d0 != 0.0D && d1 != 0.0D && e.level().noCollision(e, e.getBoundingBox().move(d0, (double) (-e.maxUpStep()), d1))) {
                            if (d0 < 0.05D && d0 >= -0.05D) {
                                d0 = 0.0D;
                            } else if (d0 > 0.0D) {
                                d0 -= 0.05D;
                            } else {
                                d0 += 0.05D;
                            }

                            if (d1 < 0.05D && d1 >= -0.05D) {
                                d1 = 0.0D;
                            } else if (d1 > 0.0D) {
                                d1 -= 0.05D;
                            } else {
                                d1 += 0.05D;
                            }
                        }

                        vec = new Vec3(d0, vec.y, d1);
                    }

                    e.move(MoverType.SELF, vec);
                }
                e.setDeltaMovement(e.getDeltaMovement().multiply(0,1,0));
            })
            .addTickAction(FallHandler::fallDecrease)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn));
    public static final ComboState EX_JUDGEMENT_CUT_SLASH = new ComboState("ex_judgement_cut_slash",50,
            ()->1923,()->1928,()->0.4f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_JUDGEMENT_CUT_SLASH, ()->Extra.EX_JUDGEMENT_CUT_SHEATH)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCut).build())
            .addTickAction(FallHandler::fallDecrease)
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_JUDGEMENT_CUT_SHEATH = new ComboState("ex_judgement_cut_sheath",50,
            ()->1928,()->1963,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> ComboState.NONE, ()->ComboState.NONE)
            .addTickAction(FallHandler::fallDecrease)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);


    public static final ComboState EX_JUDGEMENT_CUT_SLASH_AIR = new ComboState("ex_judgement_cut_slash_air",50,
            ()->1923,()->1928,()->0.5f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_JUDGEMENT_CUT_SLASH_AIR, ()->Extra.EX_JUDGEMENT_CUT_SHEATH_AIR)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCut).build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_JUDGEMENT_CUT)).build())
            .addTickAction(FallHandler::fallResist)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_JUDGEMENT_CUT_SHEATH_AIR = new ComboState("ex_judgement_cut_sheath_air",50,
            ()->1928,()->1963,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> ComboState.NONE, ()->ComboState.NONE)
            .addTickAction(FallHandler::fallDecrease)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);

    public static final ComboState EX_JUDGEMENT_CUT_SLASH_JUST = new ComboState("ex_judgement_cut_slash_just2",45,
            ()->1923,()->1928,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_JUDGEMENT_CUT_SLASH_JUST, ()->Extra.EX_JUDGEMENT_CUT_SLASH_JUST2)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCutJust).build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, a->AdvancementHelper.grantCriterion(a,ADVANCEMENT_JUDGEMENT_CUT_JUST)).build())
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addTickAction(FallHandler::fallResist)
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_JUDGEMENT_CUT_SLASH_JUST2 = new ComboState("ex_judgement_cut_slash_just2",50,
            ()->1923,()->1928,()->0.75f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_JUDGEMENT_CUT_SLASH_JUST2, ()->Extra.EX_JUDGEMENT_CUT_SLASH_JUST_SHEATH)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addTickAction(FallHandler::fallResist);
    public static final ComboState EX_JUDGEMENT_CUT_SLASH_JUST_SHEATH = new ComboState("ex_judgement_cut_slash_just_sheath",50,
            ()->1928,()->1963,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> ComboState.NONE, ()->ComboState.NONE)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addTickAction(FallHandler::fallDecrease)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);

    /**
     * VOID_SLASH
     */

    public static final ComboState EX_VOID_SLASH = new ComboState("ex_void_slash",45,
            ()->2200,()->2277,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> Extra.EX_VOID_SLASH, ()->Extra.EX_VOID_SLASH_SHEATH)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(28, (living)->{
                if(living.level().isClientSide){
                    Vec3 pos = living.position()
                            .add(0.0D, (double)living.getEyeHeight() * 0.75D, 0.0D)
                            .add(living.getLookAngle().scale(0.3f));

                    EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, living.level()){
                        @Override
                        protected void tryDespawn() {
                            if(this.getShooter() != null){
                                long timeout = this.getShooter().getPersistentData().getLong(ItemSlashBlade.BREAK_ACTION_TIMEOUT);
                                if(timeout <= this.level().getGameTime() || timeout == 0){
                                    this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.80F, 0.625F + 0.1f * this.random.nextFloat());

                                    this.remove(RemovalReason.DISCARDED);
                                }
                            }
                            super.tryDespawn();
                        }
                    };
                    jc.setPos(pos.x ,pos.y, pos.z);
                    jc.setOwner(living);

                    jc.setRotationRoll(0);
                    jc.setYRot(living.getYRot());
                    jc.setXRot(0);

                    int colorCode = living.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                            .map(state->state.getColorCode())
                            .orElseGet(()->0xFFFFFF);
                    jc.setColor(colorCode);

                    jc.setMute(true);
                    jc.setIsCritical(true);

                    jc.setDamage(living.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0);

                    jc.setKnockBack(KnockBacks.cancel);

                    jc.setBaseSize(20);

                    jc.setLifetime(20*5);

                    living.level().addFreshEntity(jc);
                }
            }).build())
            .addTickAction(ComboState.TimeLineTickAction.getBuilder()
                    .put(28+0, (entityIn)->UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(28+1, (entityIn)->UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(28+2, (entityIn)->UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(28+3, (entityIn)->UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(28+4, (entityIn)->UserPoseOverrider.setRot(entityIn, -36, true))
                    .put(28+5, (entityIn)->UserPoseOverrider.setRot(entityIn, 0, true))
                    .put(79+0, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+1, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+2, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+3, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+4, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+5, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+6, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+7, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+8, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+9, (entityIn)->UserPoseOverrider.setRot(entityIn, 18, true))
                    .put(79+10, (entityIn)->UserPoseOverrider.setRot(entityIn, 0, true))
                    .build())
            .addTickAction(FallHandler::fallResist)
            .addHitEffect(StunManager::setStun);
    public static final ComboState EX_VOID_SLASH_SHEATH = new ComboState("ex_void_slash_sheath",50,
            ()->2278,()->2299,()->1.0f,()->false,()->0,
            ExMotionLocation, (a)-> ComboState.NONE, ()->ComboState.NONE)
            .addTickAction((entityIn)->UserPoseOverrider.resetRot(entityIn))
            .addTickAction(FallHandler::fallDecrease)
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0,Extra::playQuickSheathSoundAction).build())
            .setReleaseAction(ComboState::releaseActionQuickCharge);

}
