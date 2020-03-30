package mods.flammpfeil.slashblade.capability.slashblade;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.capability.imputstate.IImputState;
import mods.flammpfeil.slashblade.event.FallHandler;
import mods.flammpfeil.slashblade.event.KnockBackHandler;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialattack.SlashArts;
import mods.flammpfeil.slashblade.util.AttackManager;
import mods.flammpfeil.slashblade.util.ImputCommand;
import mods.flammpfeil.slashblade.util.RegistryBase;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ComboState extends RegistryBase<ComboState> {
    public static final ResourceLocation baseMotionLoc = new ResourceLocation(SlashBlade.modid, "combostate/motion.vmd");

    @CapabilityInject(IImputState.class)
    public static Capability<IImputState> IMPUT_STATE = null;

    public static final ComboState NONE = new ComboState(BaseInstanceName, 1000,
            ()->30, ()->31, ()->1.0f, ()->true,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .setQuickChargeEnabled(()->false);

    static List<Map.Entry<EnumSet<ImputCommand>, Supplier<ComboState>>> standbyMap =
            new HashMap<EnumSet<ImputCommand>, Supplier<ComboState>>(){{
                this.put(EnumSet.of(ImputCommand.ON_GROUND, ImputCommand.SNEAK, ImputCommand.FORWARD, ImputCommand.R_CLICK),
                        () -> ARTS_RAPID_SLASH);
                this.put(EnumSet.of(ImputCommand.ON_GROUND, ImputCommand.L_CLICK),
                        () -> COMBO_B1);
                this.put(EnumSet.of(ImputCommand.ON_GROUND, ImputCommand.BACK, ImputCommand.SNEAK, ImputCommand.R_CLICK),
                        () -> COMBO_B1);
                this.put(EnumSet.of(ImputCommand.ON_GROUND, ImputCommand.R_CLICK),
                        () -> COMBO_A1);

                this.put(EnumSet.of(ImputCommand.ON_AIR, ImputCommand.SNEAK, ImputCommand.FORWARD, ImputCommand.R_CLICK),
                        () -> ARTS_HELM_BREAKER);
                this.put(EnumSet.of(ImputCommand.ON_AIR),
                        () -> COMBO_AA1);
            }}.entrySet().stream()
            .collect(Collectors.toList());


    public static final ComboState STANDBY = new ComboState("standby", 10,
            ()->30,()->31,()->1.0f,()->true,()->1000,
            baseMotionLoc, (a)-> {

        EnumSet<ImputCommand> commands =
            a.getCapability(IMPUT_STATE).map((state)->state.getCommands(a)).orElseGet(()-> EnumSet.noneOf(ImputCommand.class));

        return standbyMap.stream()
                .filter((entry)->commands.containsAll(entry.getKey()))
                //.findFirst()
                .min(Comparator.comparingInt((entry)-> entry.getValue().get().getPriority()))
                .map((entry)->entry.getValue().get())
                .orElseGet(()->ComboState.NONE);

    }, ()-> ComboState.NONE)
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_A1 = new ComboState("combo_a1",100,
            ()->60,()->70,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->ComboState.COMBO_A2, ()-> ComboState.NONE)
            .setClickAction((e)->AttackManager.areaAttack(e,  KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun)
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_A2 = new ComboState("combo_a2",100,
            ()->70,()->80,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)-> ComboState.COMBO_A3, ()-> ComboState.NONE)
            .setClickAction((e)->AttackManager.areaAttack(e,  KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun)
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_A3 = new ComboState("combo_a3",100,
            ()->80,()->90,()->1.0f,()->false,()->600,
            baseMotionLoc, (a)->(ComboState.NONE), () -> ComboState.COMBO_A3_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setBoost(ee,1.5)))
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_A3_F = new ComboState("combo_a3_f", 100,
            ()->90,()->120,()->1.5f,()->false,()->2000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE);


    private static final EnumSet<ImputCommand> combo_b1_alt = EnumSet.of(ImputCommand.BACK, ImputCommand.R_DOWN);
    public static final ComboState COMBO_B1 = new ComboState("combo_b1",90,
            ()->150, ()->160, ()->1.0f, ()->false,()->1000,
            baseMotionLoc, (a)-> ComboState.COMBO_B2, ()-> ComboState.COMBO_B1_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setSmash(ee,0.5)))
            .addHoldAction((player) -> {
                int elapsed = player.getItemInUseMaxCount();

                EnumSet<ImputCommand> commands =
                        player.getCapability(IMPUT_STATE).map((state)->state.getCommands(player)).orElseGet(()-> EnumSet.noneOf(ImputCommand.class));

                if (5 == elapsed && commands.containsAll(combo_b1_alt)) {
                    Vec3d motion = player.getMotion();
                    player.setMotion(motion.x, motion.y + 0.7, motion.z);
                    player.onGround = false;
                    player.isAirBorne = true;
                }
            })
            .addHitEffect((e)->StunManager.setStun(e, 15))
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            })
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_B1_F = new ComboState("combo_b1_f",100,
            ()->165,()-> 185,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState COMBO_B2 = new ComboState("combo_b2",90,
            ()->200,()-> 215,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), () -> ComboState.COMBO_B2_F)
            .addHitEffect(StunManager::setStun)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setSmash(ee,-5)))
            .setQuickChargeEnabled(()->false);
    public static final ComboState COMBO_B2_F = new ComboState("combo_b2_f",100,
            ()->215,()-> 240,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE);


    public static final ComboState COMBO_AA1 = new ComboState("combo_aa1",80,
            ()->245,()-> 270,()->1.0f,()->false,()->500,
            baseMotionLoc, (a)->ComboState.COMBO_AA2, ()-> ComboState.COMBO_AA1_F)
            .setClickAction((e)->AttackManager.areaAttack(e, KnockBackHandler::setCancel))
            .setIsAerial()
            .addTickAction((playerIn)->{

                FallHandler.fallDecrease(playerIn);

                playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                    long elapsed = state.getElapsedTime(playerIn);
                    if(elapsed == 4){
                        AttackManager.areaAttack(playerIn, KnockBackHandler::setCancel);
                    }
                });
            })
            .addHitEffect(StunManager::setStun)
            .setIsAerial();

    public static final ComboState COMBO_AA1_F = new ComboState("combo_aa1_f",80,
            ()->269,()-> 270,()->20.0f,()->true,()->1000,
            baseMotionLoc, (a)->ComboState.COMBO_AA2, ()-> ComboState.NONE);

    public static final ComboState COMBO_AA2 = new ComboState("combo_aa2",80,
            ()->270,()-> 295,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.COMBO_AA2_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setBoost(ee,1.5)))
            .setIsAerial()
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            })
            .setQuickChargeEnabled(()->false);

    public static final ComboState COMBO_AA2_F = new ComboState("combo_aa2_f",100,
            ()->295,()-> 300,()->1.0f,()->false,()->400,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    //=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=

    public static final ComboState ARTS_RAPID_SLASH = new ComboState("arts_rapid_slash",70,
            ()->80,()->90,()->1.0f,()->false,()->1000,
            baseMotionLoc,
            (a)->ComboState.ARTS_RAPID_SLASH_F, () -> ComboState.ARTS_RAPID_SLASH_F)
            .setClickAction((e)->AttackManager.areaAttack(e, KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun)
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getItemInUseMaxCount();

                if(elapsed < 6){
                    playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn, KnockBackHandler::setCancel,1.0f,false,false,true);
                    });

                    if (elapsed % 3 == 1) {
                        playerIn.world.playSound((PlayerEntity)null, playerIn.posX, playerIn.posY, playerIn.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                                0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));
                    }
                }

                if(elapsed <= 3 && playerIn.onGround)
                    playerIn.moveRelative( playerIn.isInWater() ? 0.35f : 0.8f , new Vec3d(0, 0, 1));

                if(elapsed == 10 && (playerIn.world.isRemote ? playerIn.onGround : true)){
                    playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                        ComboState combo = ComboState.ARTS_RISING_STAR;
                        state.setComboSeq(combo);
                        state.setLastActionTime(playerIn.world.getGameTime());
                        combo.clickAction(playerIn);
                    });
                }
            });

    public static final ComboState ARTS_RAPID_SLASH_F = new ComboState("arts_rapid_slash_f", 70,
            ()->90,()->120,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), ()-> ComboState.NONE);

    public static final ComboState ARTS_RISING_STAR = new ComboState("arts_rising_star",100,
            ()->250,()-> 255,()->0.75f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), () -> ComboState.COMBO_A3_F)
            .addHitEffect(StunManager::setStun)
            .setIsAerial()
            .setClickAction((playerIn)->{
                AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setSmash(ee,0.5),1.0f,true,false,false);

                Vec3d motion = playerIn.getMotion();
                playerIn.setMotion(0, motion.y + 0.7, 0);
                playerIn.onGround = false;
                playerIn.isAirBorne = true;
            })
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getItemInUseMaxCount();
                if(elapsed < 6){
                    playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setSmash(ee,0.5),1.0f,false,false,true);
                    });

                    if (elapsed % 2 == 1) {
                        playerIn.world.playSound((PlayerEntity)null, playerIn.posX, playerIn.posY, playerIn.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                                0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));
                    }
                }
            })
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState ARTS_HELM_BREAKER = new ComboState("arts_helm_breaker",70,
            ()->200,()-> 215,()->1.0f,()->false,()->1000,
            baseMotionLoc, (a)->(ComboState.NONE), () -> ComboState.ARTS_HELM_BREAKER_F)
            .addHitEffect(StunManager::setStun)
            .setClickAction((playerIn)->{
                AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setSmash(ee,-5),1.0f,true,false,false);

                Vec3d motion = playerIn.getMotion();
                playerIn.setMotion(motion.x, motion.y - 0.7, motion.z);
            })
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getItemInUseMaxCount();
                if(!playerIn.onGround){
                    playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setSmash(ee,-5),1.0f,false,false,true);
                    });

                    if (elapsed % 2 == 1) {
                        playerIn.world.playSound((PlayerEntity)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));
                    }
                }
            })
            .addTickAction((playerIn)-> {
                if(!playerIn.onGround)
                    playerIn.fallDistance = 1;
                else{
                    //finish
                    playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setSmash(ee,-5),1.3f,true,true,true);
                        state.setComboSeq(ComboState.ARTS_HELM_BREAKER_F);
                        state.setLastActionTime(playerIn.world.getGameTime());
                        FallHandler.spawnLandingParticle(playerIn, 20);
                    });
                }
            })
            .setQuickChargeEnabled(()->false);

    public static final ComboState ARTS_HELM_BREAKER_F = new ComboState("arts_helm_breaker_f",70,
            ()->214,()-> 215,()->20.0f,()->true,()->600,
            baseMotionLoc, (a)->(ComboState.NONE), () -> ComboState.COMBO_B2_F)
            .setQuickChargeEnabled(()->false);


    static final EnumSet<ImputCommand> jc_cycle_imput = EnumSet.of(ImputCommand.L_DOWN, ImputCommand.R_CLICK);
    static final RangeMap<Long, SlashArts.ArtsType> jc_cycle_accept = ImmutableRangeMap.<Long, SlashArts.ArtsType>builder()
            .put(Range.lessThan(7l), SlashArts.ArtsType.Fail)
            .put(Range.closedOpen(7l, 8l), SlashArts.ArtsType.Jackpot)
            .put(Range.closed(8l, 9l), SlashArts.ArtsType.Success)
            .put(Range.greaterThan(9l), SlashArts.ArtsType.Fail)
            .build();
    public static final ComboState SLASH_ARTS_JC = new ComboState("slash_arts_jc",50,
            ()->115,()->120,()->0.5f,()->false,()->600,
            baseMotionLoc, (a)->{

        EnumSet<ImputCommand> commands =
                a.getCapability(IMPUT_STATE).map((state)->state.getCommands(a)).orElseGet(()-> EnumSet.noneOf(ImputCommand.class));

        if(commands.containsAll(jc_cycle_imput)){
            return a.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).map(s->{
                long time = a.world.getGameTime();
                long lastAction = s.getLastActionTime();

                long count = time - lastAction;

                SlashArts.ArtsType type = jc_cycle_accept.get(count);
                return s.getSlashArts().doArts(type, a);
            }).orElse(ComboState.NONE);
        }

        return ComboState.NONE;
    }, ()-> ComboState.NONE)
            .addTickAction((playerIn)-> {
                //if(playerIn.world.getGameTime() % 2 == 0)
                    FallHandler.fallResist(playerIn);
            });

    private ResourceLocation motionLoc;

    //frame
    private Supplier<Integer> start;
    //frame
    private Supplier<Integer> end;

    private Supplier<Float> speed;
    private Supplier<Boolean> roop;

    //Next input acceptance period *ms
    public Supplier<Integer> timeout;

    private Function<LivingEntity, ComboState> next;
    private Supplier<ComboState> nextOfTimeout;

    private Consumer<LivingEntity> holdAction;

    private Consumer<LivingEntity> tickAction;

    private Consumer<LivingEntity> hitEffect;

    private Consumer<LivingEntity> clickAction;

    private boolean isAerial;

    private int priority;

    public ResourceLocation getMotionLoc() {
        return motionLoc;
    }

    public int getStartFrame() {
        return start.get();
    }

    public int getEndFrame() {
        return end.get();
    }

    public float getSpeed() {
        return speed.get();
    }

    public boolean getRoop() {
        return roop.get();
    }

    public int getTimeoutMS() {
        return timeout.get();
    }

    public void holdAction(LivingEntity user){
        holdAction.accept(user);
    }
    public ComboState addHoldAction(Consumer<LivingEntity> holdAction){
        this.holdAction = this.holdAction.andThen(holdAction);
        return this;
    }
    public void tickAction(LivingEntity user){
        tickAction.accept(user);
    }
    public ComboState addTickAction(Consumer<LivingEntity> tickAction){
        this.tickAction = this.tickAction.andThen(tickAction);
        return this;
    }
    public void hitEffect(LivingEntity target){
        hitEffect.accept(target);
    }
    public ComboState addHitEffect(Consumer<LivingEntity> hitEffect){
        this.hitEffect = this.hitEffect.andThen(hitEffect);
        return this;
    }

    public void clickAction(LivingEntity user){
        clickAction.accept(user);
    }
    public ComboState setClickAction(Consumer<LivingEntity> clickAction){
        this.clickAction = clickAction;
        return this;
    }

    private Supplier<Boolean> quickChargeEnabled;
    public ComboState setQuickChargeEnabled(Supplier<Boolean> sup){
        this.quickChargeEnabled = sup;
        return this;
    }
    public boolean getQuickChargeEnabled(){
        return this.quickChargeEnabled.get();
    }


    public ComboState(String name,int priority, Supplier<Integer> start, Supplier<Integer> end, Supplier<Float> speed, Supplier<Boolean> roop, Supplier<Integer> timeout
            , ResourceLocation motionLoc
            , Function<LivingEntity, ComboState> next
            , Supplier<ComboState> nextOfTimeout) {
        super(name);

        this.start = start;
        this.end = end;

        this.speed = speed;
        this.timeout = timeout;
        this.roop = roop;

        this.motionLoc = motionLoc;

        this.next = next;
        this.nextOfTimeout = nextOfTimeout;

        this.holdAction = (a)->{};

        this.tickAction = ArrowReflector::doTicks;

        this.hitEffect = (a)->{};

        this.clickAction = (user) -> {
            AttackManager.areaAttack(user, (e)->{});
        };

        this.isAerial = false;

        this.priority = priority;

        this.quickChargeEnabled = ()->true;
    }

    @Override
    public String getPath() {
        return "combostate";
    }

    @Override
    public ComboState getNone() {
        return NONE;
    }

    public ComboState getNext(LivingEntity living){
        return this.next.apply(living);
    }

    public ComboState getNextOfTimeout(){
        return this.nextOfTimeout.get();
    }

    @Nonnull
    public ComboState checkTimeOut(float time){
        return this.timeout.get() < time ? nextOfTimeout.get() : this;
    }

    public boolean isAerial(){
        return this.isAerial;
    }
    public ComboState setIsAerial(){
        this.isAerial = true;
        return this;
    }

    public int getPriority() {
        return priority;
    }
}
