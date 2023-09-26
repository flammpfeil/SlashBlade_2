package mods.flammpfeil.slashblade.capability.slashblade;

import com.google.common.collect.*;
import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.slashblade.combo.Extra;
import mods.flammpfeil.slashblade.event.FallHandler;
import mods.flammpfeil.slashblade.event.KnockBackHandler;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialattack.JudgementCut;
import mods.flammpfeil.slashblade.specialattack.SlashArts;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static mods.flammpfeil.slashblade.init.DefaultResources.BaseMotionLocation;

public class ComboState extends RegistryBase<ComboState>{
    static Map<ResourceLocation, ComboState> registry = Maps.newHashMap();

    @Override
    public Map<ResourceLocation, ComboState> getRegistry() {
        return ComboState.registry;
    }

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final ComboState NONE = new ComboState(BaseInstanceName, 1000,
            ()->0, ()->1, ()->1.0f, ()->true,()->0,
            DefaultResources.ExMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .addTickAction((e)->UserPoseOverrider.resetRot(e));

    static List<Map.Entry<EnumSet<InputCommand>, Supplier<ComboState>>> standbyMap =
            new HashMap<EnumSet<InputCommand>, Supplier<ComboState>>(){{
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.SNEAK, InputCommand.FORWARD, InputCommand.R_CLICK),
                        () -> ARTS_RAPID_SLASH);
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.L_CLICK),
                        () -> COMBO_B1);
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.BACK, InputCommand.SNEAK, InputCommand.R_CLICK),
                        () -> COMBO_B1);
                this.put(EnumSet.of(InputCommand.ON_GROUND, InputCommand.R_CLICK),
                        () -> COMBO_A1);

                this.put(EnumSet.of(InputCommand.ON_AIR, InputCommand.SNEAK, InputCommand.FORWARD, InputCommand.R_CLICK),
                        () -> ARTS_HELM_BREAKER);
                this.put(EnumSet.of(InputCommand.ON_AIR),
                        () -> COMBO_AA1);
            }}.entrySet().stream()
            .collect(Collectors.toList());


    public static final ComboState STANDBY = new ComboState("standby_old", 10,
            ()->30,()->31,()->1.0f,()->true,()->1000,
            BaseMotionLocation, (a)-> {

        EnumSet<InputCommand> commands =
            a.getCapability(INPUT_STATE).map((state)->state.getCommands(a)).orElseGet(()-> EnumSet.noneOf(InputCommand.class));

        return standbyMap.stream()
                .filter((entry)->commands.containsAll(entry.getKey()))
                //.findFirst()
                .min(Comparator.comparingInt((entry)-> entry.getValue().get().getPriority()))
                .map((entry)->entry.getValue().get())
                .orElseGet(()->ComboState.NONE);

    }, ()-> ComboState.NONE);

    public static final ComboState COMBO_A1 = new ComboState("combo_a1",100,
            ()->60,()->70,()->1.5f,()->false,()->1000,
            BaseMotionLocation, (a)->ComboState.COMBO_A2, ()-> ComboState.NONE)
            .setClickAction((e)->AttackManager.areaAttack(e,  KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun);

    public static final ComboState COMBO_A2 = new ComboState("combo_a2",100,
            ()->70,()->80,()->1.5f,()->false,()->1000,
            BaseMotionLocation, (a)-> ComboState.COMBO_A3, ()-> ComboState.NONE)
            .setClickAction((e)->AttackManager.areaAttack(e,  KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun);

    public static final ComboState COMBO_A3 = new ComboState("combo_a3",100,
            ()->80,()->90,()->1.75f,()->false,()->600,
            BaseMotionLocation, (a)->(ComboState.NONE), () -> ComboState.COMBO_A3_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setSmash(ee,1.5)));

    public static final ComboState COMBO_A3_F = new ComboState("combo_a3_f", 100,
            ()->90,()->120,()->1.5f,()->false,()->2000,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE);


    private static final EnumSet<InputCommand> combo_b1_alt = EnumSet.of(InputCommand.BACK, InputCommand.R_DOWN);
    public static final ComboState COMBO_B1 = new ComboState("combo_b1",90,
            ()->150, ()->160, ()->1.0f, ()->false,()->1000,
            BaseMotionLocation, (a)-> ComboState.COMBO_B2, ()-> ComboState.COMBO_B1_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setVertical(ee,0.5)))
            .addHoldAction((player) -> {
                int elapsed = player.getTicksUsingItem();

                EnumSet<InputCommand> commands =
                        player.getCapability(INPUT_STATE).map((state)->state.getCommands(player)).orElseGet(()-> EnumSet.noneOf(InputCommand.class));

                if (5 == elapsed && commands.containsAll(combo_b1_alt)) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, motion.y + 0.7, motion.z);
                    player.setOnGround(false);
                    player.hasImpulse = true;
                }
            })
            .addHitEffect((e,a)->StunManager.setStun(e, 15))
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState COMBO_B1_F = new ComboState("combo_b1_f",100,
            ()->165,()-> 185,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState COMBO_B2 = new ComboState("combo_b2",90,
            ()->200,()-> 215,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), () -> ComboState.COMBO_B2_F)
            .addHitEffect(StunManager::setStun)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setVertical(ee,-5)));
    public static final ComboState COMBO_B2_F = new ComboState("combo_b2_f",100,
            ()->215,()-> 240,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE);


    public static final ComboState COMBO_AA1 = new ComboState("combo_aa1",80,
            ()->245,()-> 270,()->1.0f,()->false,()->500,
            BaseMotionLocation, (a)->ComboState.COMBO_AA2, ()-> ComboState.COMBO_AA1_F)
            .setClickAction((e)->AttackManager.areaAttack(e, KnockBackHandler::setCancel))
            .setIsAerial()
            .addTickAction((playerIn)->{

                FallHandler.fallDecrease(playerIn);

                playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
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
            BaseMotionLocation, (a)->ComboState.COMBO_AA2, ()-> ComboState.NONE);

    public static final ComboState COMBO_AA2 = new ComboState("combo_aa2",80,
            ()->270,()-> 295,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.COMBO_AA2_F)
            .setClickAction((e)->AttackManager.areaAttack(e, (ee)->KnockBackHandler.setSmash(ee,1.5)))
            .setIsAerial()
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState COMBO_AA2_F = new ComboState("combo_aa2_f",100,
            ()->295,()-> 300,()->1.0f,()->false,()->400,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE)
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    
    



    //=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=-+-=

    public static final ComboState ARTS_RAPID_SLASH = new ComboState("arts_rapid_slash",70,
            ()->80,()->90,()->1.0f,()->false,()->1000,
            BaseMotionLocation,
            (a)->ComboState.ARTS_RAPID_SLASH_F, () -> ComboState.ARTS_RAPID_SLASH_F)
            .setClickAction((e)->AttackManager.areaAttack(e, KnockBackHandler::setCancel))
            .addHitEffect(StunManager::setStun)
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getTicksUsingItem();

                if(elapsed < 6){
                    playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn, KnockBackHandler::setCancel,1.0f,false,false,true);
                    });

                    if (elapsed % 3 == 1) {
                        playerIn.level().playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                                0.5F, 0.4F / (playerIn.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                }

                if(elapsed <= 3 && playerIn.onGround())
                    playerIn.moveRelative( playerIn.isInWater() ? 0.35f : 0.8f , new Vec3(0, 0, 1));

                if(elapsed == 10 && (playerIn.level().isClientSide ? playerIn.onGround() : true)){
                    playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                        ComboState combo = ComboState.ARTS_RISING_STAR;
                        state.setComboSeq(combo);
                        state.setLastActionTime(playerIn.level().getGameTime());
                        combo.clickAction(playerIn);
                    });
                }
            });

    public static final ComboState ARTS_RAPID_SLASH_F = new ComboState("arts_rapid_slash_f", 70,
            ()->90,()->120,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), ()-> ComboState.NONE);

    public static final ComboState ARTS_RISING_STAR = new ComboState("arts_rising_star",100,
            ()->250,()-> 255,()->0.75f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), () -> ComboState.COMBO_A3_F)
            .addHitEffect(StunManager::setStun)
            .setIsAerial()
            .setClickAction((playerIn)->{
                AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setVertical(ee,0.5),1.0f,true,false,false);

                Vec3 motion = playerIn.getDeltaMovement();
                playerIn.setDeltaMovement(0, motion.y + 0.7, 0);
                playerIn.setOnGround(false);
                playerIn.hasImpulse = true;
            })
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getTicksUsingItem();
                if(elapsed < 6){
                    playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setVertical(ee,0.5),1.0f,false,false,true);
                    });

                    if (elapsed % 2 == 1) {
                        playerIn.level().playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                                0.5F, 0.4F / (playerIn.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                }
            })
            .addTickAction((playerIn)-> {
                FallHandler.fallDecrease(playerIn);
            });

    public static final ComboState ARTS_HELM_BREAKER = new ComboState("arts_helm_breaker",70,
            ()->200,()-> 215,()->1.0f,()->false,()->1000,
            BaseMotionLocation, (a)->(ComboState.NONE), () -> ComboState.ARTS_HELM_BREAKER_F)
            .addHitEffect(StunManager::setStun)
            .setClickAction((playerIn)->{
                AttackManager.areaAttack(playerIn, KnockBacks.meteor.action,1.0f,true,false,false);

                Vec3 motion = playerIn.getDeltaMovement();
                playerIn.setDeltaMovement(motion.x, motion.y - 0.7, motion.z);
            })
            .addHoldAction((playerIn)->{
                int elapsed = playerIn.getTicksUsingItem();
                if(!playerIn.onGround()){
                    playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setVertical(ee,-5),1.0f,false,false,true);
                    });

                    if (elapsed % 2 == 1) {
                        playerIn.level().playSound((Player)null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5F, 0.4F / (playerIn.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                }
            })
            .addTickAction((playerIn)-> {
                if(!playerIn.onGround())
                    playerIn.fallDistance = 1;
                else{
                    //finish
                    playerIn.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                        AttackManager.areaAttack(playerIn,(ee)->KnockBackHandler.setVertical(ee,-5),1.3f,true,true,true);
                        state.setComboSeq(ComboState.ARTS_HELM_BREAKER_F);
                        state.setLastActionTime(playerIn.level().getGameTime());
                        FallHandler.spawnLandingParticle(playerIn, 20);
                    });
                }
            });

    public static final ComboState ARTS_HELM_BREAKER_F = new ComboState("arts_helm_breaker_f",70,
            ()->214,()-> 215,()->20.0f,()->true,()->600,
            BaseMotionLocation, (a)->(ComboState.NONE), () -> ComboState.COMBO_B2_F);


    static final EnumSet<InputCommand> jc_cycle_input = EnumSet.of(InputCommand.L_DOWN, InputCommand.R_CLICK);
    static final RangeMap<Long, SlashArts.ArtsType> jc_cycle_accept = ImmutableRangeMap.<Long, SlashArts.ArtsType>builder()
            .put(Range.lessThan(7l), SlashArts.ArtsType.Fail)
            .put(Range.closedOpen(7l, 8l), SlashArts.ArtsType.Jackpot)
            .put(Range.closed(8l, 9l), SlashArts.ArtsType.Success)
            .put(Range.greaterThan(9l), SlashArts.ArtsType.Fail)
            .build();
    public static final ComboState SLASH_ARTS_JC = new ComboState("slash_arts_jc",50,
            ()->115,()->120,()->0.5f,()->false,()->600,
            BaseMotionLocation, (a)->{

        EnumSet<InputCommand> commands =
                a.getCapability(INPUT_STATE).map((state)->state.getCommands(a)).orElseGet(()-> EnumSet.noneOf(InputCommand.class));

        if(commands.containsAll(jc_cycle_input)){
            return a.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).map(s->{
                long time = a.level().getGameTime();
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
            })
            .addTickAction(ComboState.TimeLineTickAction.getBuilder().put(0, JudgementCut::doJudgementCut).build())
            ;


    /*-----------------------------------------*/

    static public ComboState ExtraStandBy = Extra.STANDBY_EX;

    /*-----------------------------------------*/


    private ResourceLocation motionLoc;

    //frame
    private Supplier<Integer> start;
    //frame
    private Supplier<Integer> end;

    private Supplier<Float> speed;
    private Supplier<Boolean> loop;

    //Next input acceptance period *ms
    public Supplier<Integer> timeout;

    private Function<LivingEntity, ComboState> next;
    private Supplier<ComboState> nextOfTimeout;

    private Consumer<LivingEntity> holdAction;

    private Consumer<LivingEntity> tickAction;

    private BiConsumer<LivingEntity,LivingEntity> hitEffect;

    private Consumer<LivingEntity> clickAction;

    private BiFunction<LivingEntity, Integer, SlashArts.ArtsType> releaseAction;

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

    public boolean getLoop() {
        return loop.get();
    }

    public int getTimeoutMS() {
        return (int)(TimeValueHelper.getMSecFromFrames(Math.abs(getEndFrame() - getStartFrame())) / getSpeed()) + timeout.get();
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
    public void hitEffect(LivingEntity target, LivingEntity attacker){
        hitEffect.accept(target, attacker);
    }
    public ComboState addHitEffect(BiConsumer<LivingEntity,LivingEntity> hitEffect){
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

    public SlashArts.ArtsType releaseAction(LivingEntity user, int elapsed){
        return this.releaseAction.apply(user, elapsed);
    }
    public ComboState setReleaseAction(BiFunction<LivingEntity,Integer,SlashArts.ArtsType> clickAction){
        this.releaseAction = clickAction;
        return this;
    }

    public ComboState(String name,int priority, Supplier<Integer> start, Supplier<Integer> end, Supplier<Float> speed, Supplier<Boolean> loop, Supplier<Integer> timeout
            , ResourceLocation motionLoc
            , Function<LivingEntity, ComboState> next
            , Supplier<ComboState> nextOfTimeout) {
        super(name);

        this.start = start;
        this.end = end;

        this.speed = speed;
        this.timeout = timeout;
        this.loop = loop;

        this.motionLoc = motionLoc;

        this.next = next;
        this.nextOfTimeout = nextOfTimeout;

        this.holdAction = (a)->{};

        this.tickAction = ArrowReflector::doTicks;

        this.hitEffect = (a,b)->{};

        this.clickAction = (user) -> {};

        this.releaseAction = (u,e) -> SlashArts.ArtsType.Fail;

        this.isAerial = false;

        this.priority = priority;
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
    public ComboState checkTimeOut(float msec){
        return this.getTimeoutMS() < msec ? nextOfTimeout.get() : this;
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

    static public SlashArts.ArtsType releaseActionQuickCharge(LivingEntity user, Integer elapsed){
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.SOUL_SPEED,user);
        if(elapsed <= 3 + level) {
            AdvancementHelper.grantedIf(Enchantments.SOUL_SPEED,user);
            AdvancementHelper.grantCriterion(user,Extra.ADVANCEMENT_QUICK_CHARGE);
            return SlashArts.ArtsType.Jackpot;
        }
        else
            return SlashArts.ArtsType.Fail;
    }

    public static class TimeoutNext implements Function<LivingEntity, ComboState>{

        long timeout;
        Function<LivingEntity, ComboState> next;

        static public TimeoutNext buildFromFrame(int timeoutFrame, Function<LivingEntity, ComboState> next){
            return new TimeoutNext((int)TimeValueHelper.getTicksFromFrames(timeoutFrame), next);
        }

        public TimeoutNext(long timeout, Function<LivingEntity, ComboState> next){
            this.timeout = timeout;
            this.next = next;
        }

        @Override
        public ComboState apply(LivingEntity livingEntity) {

            long elapsed = ComboState.getElapsed(livingEntity);

            if(timeout <= elapsed){
                return next.apply(livingEntity);
            }else{
                return livingEntity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                        .map((state)->state.getComboSeq())
                        .orElseGet(()->ComboState.NONE);
            }
        }
    }

    public static class TimeLineTickAction implements Consumer<LivingEntity>{
        long offset = -1;

        public static TimeLineTickActionBuilder getBuilder(){
            return new TimeLineTickActionBuilder();
        }

        public static class TimeLineTickActionBuilder{
            Map<Integer, Consumer<LivingEntity>> timeLine = Maps.newHashMap();

            public TimeLineTickActionBuilder put(int ticks, Consumer<LivingEntity> action){
                timeLine.put(ticks, action);
                return this;
            }

            public TimeLineTickAction build(){
                return new TimeLineTickAction(timeLine);
            }
        }

        Map<Integer, Consumer<LivingEntity>> timeLine = Maps.newHashMap();

        TimeLineTickAction(Map<Integer, Consumer<LivingEntity>> timeLine){
            this.timeLine.putAll(timeLine);

        }

        @Override
        public void accept(LivingEntity livingEntity) {
            long elapsed = getElapsed(livingEntity);

            if(offset < 0){
                offset = elapsed;
            }
            elapsed -= offset;

            Consumer<LivingEntity> action = timeLine.getOrDefault((int)elapsed, this::defaultConsumer);

            action.accept(livingEntity);
        }

        void defaultConsumer(LivingEntity entityIn){}
    }

    static public long getElapsed(LivingEntity livingEntity){
        return livingEntity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE)
                .map((state)->state.getElapsedTime(livingEntity))
                .orElseGet(()->0l);
    }
}
