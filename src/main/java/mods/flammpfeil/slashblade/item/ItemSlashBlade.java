package mods.flammpfeil.slashblade.item;

import com.google.common.collect.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.capability.inputstate.InputState;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.event.AnvilCraftingRecipe;
import mods.flammpfeil.slashblade.event.BladeMaterialTooltips;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.server.ServerLifecycleHooks;

import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class ItemSlashBlade extends SwordItem {
    protected static final UUID ATTACK_DAMAGE_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FD");
    protected static final UUID PLAYER_REACH_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FE");

    public static final Capability<ISlashBladeState> BLADESTATE = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>(){});

    public ItemSlashBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
    {
        Multimap<Attribute, AttributeModifier> def = super.getAttributeModifiers(slot,stack);
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        result.putAll(Attributes.ATTACK_DAMAGE, def.get(Attributes.ATTACK_DAMAGE));
        result.putAll(Attributes.ATTACK_SPEED, def.get(Attributes.ATTACK_SPEED));

        if (slot == EquipmentSlot.MAINHAND) {
            LazyOptional<ISlashBladeState> state = stack.getCapability(BLADESTATE);
            state.ifPresent(s -> {
                float baseAttackModifier = s.getBaseAttackModifier();
                AttributeModifier base = new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                        "Weapon modifier",
                        (double) baseAttackModifier,
                        AttributeModifier.Operation.ADDITION);
                result.remove(Attributes.ATTACK_DAMAGE,base);
                result.put(Attributes.ATTACK_DAMAGE,base);

                float rankAttackAmplifier = s.getAttackAmplifier();
                result.put(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_AMPLIFIER,
                                "Weapon amplifier",

                                (double) rankAttackAmplifier,
                                AttributeModifier.Operation.ADDITION));

                result.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(PLAYER_REACH_AMPLIFIER,
                        "Reach amplifer",
                        s.isBroken() ? ReachModifier.BrokendReach() : ReachModifier.BladeReach(), AttributeModifier.Operation.ADDITION));

            });
        }

        return result;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        LazyOptional<ISlashBladeState> state = stack.getCapability(BLADESTATE);

        return state
                .filter(s -> s.getRarity() != Rarity.COMMON)
                .map(s -> s.getRarity())
                .orElseGet(() -> super.getRarity(stack));

    }


    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        boolean result = itemstack.getCapability(BLADESTATE).map((state) -> {

            playerIn.getCapability(INPUT_STATE).ifPresent((s)->s.getCommands().add(InputCommand.R_CLICK));

            ComboState combo = state.progressCombo(playerIn);

            playerIn.getCapability(INPUT_STATE).ifPresent((s)->s.getCommands().remove(InputCommand.R_CLICK));

            if(combo != ComboState.NONE)
                playerIn.swing(handIn);

            return true;
        }).orElse(false);

        playerIn.startUsingItem(handIn);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemstack, Player playerIn, Entity entity) {

        Level worldIn = playerIn.level();

        Optional<ISlashBladeState> stateHolder = itemstack.getCapability(BLADESTATE)
                .filter((state) -> !state.onClick());

        stateHolder.ifPresent((state) -> {
            playerIn.getCapability(INPUT_STATE).ifPresent((s)->s.getCommands().add(InputCommand.L_CLICK));

            ComboState combo = state.progressCombo(playerIn);

            playerIn.getCapability(INPUT_STATE).ifPresent((s)->s.getCommands().remove(InputCommand.L_CLICK));
        });

        return stateHolder.isPresent();
    }

    static public final String BREAK_ACTION_TIMEOUT = "BreakActionTimeout";

    static public Consumer<LivingEntity> getOnBroken(ItemStack stack){
        return (user)->{
            user.broadcastBreakEvent(user.getUsedItemHand());

            ItemStack soul = new ItemStack(SBItems.proudsoul);

            CompoundTag blade = stack.save(new CompoundTag());
            soul.addTagElement(BladeMaterialTooltips.BLADE_DATA, blade);

            stack.getCapability(BLADESTATE).ifPresent(s->{
                s.getTexture().ifPresent(r->soul.addTagElement("Texture", StringTag.valueOf(r.toString())));
                s.getModel().ifPresent(r->soul.addTagElement("Model", StringTag.valueOf(r.toString())));
            });

            {//add clone blade recipe
                ItemStack cpBlade = stack.copy();
                cpBlade.getCapability(BLADESTATE).ifPresent(s->{
                    s.setDamage(0);
                    s.setOwner(null);
                    s.setRefine(0);
                    s.setKillCount(0);
                });
                cpBlade.getEnchantmentTags().clear();

                AnvilCraftingRecipe recipe = new AnvilCraftingRecipe();
                recipe.setLevel(10);
                recipe.setKillcount(0);
                recipe.setRefine(0);
                recipe.setBroken(false);
                recipe.setNoScabbard(false);
                recipe.setTranslationKey("item.slashblade.slashblade");
                recipe.setResultWithNBT(cpBlade.save(new CompoundTag()));
                recipe.setOverwriteTag(null);

                soul.addTagElement("RequiredBlade", recipe.writeNBT());
            }

            ItemEntity itementity = new ItemEntity(user.level(), user.getX(), user.getY() , user.getZ(), soul);
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, user.level()){

                static final String isReleased = "isReleased";
                @Override
                public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource ds) {

                    CompoundTag tag = this.getPersistentData();

                    if(!tag.getBoolean(isReleased)){
                        this.getPersistentData().putBoolean(isReleased, true);

                        if(this.level() instanceof ServerLevel){
                            Entity thrower = getOwner();

                            if (thrower != null) {
                                thrower.getPersistentData().remove(BREAK_ACTION_TIMEOUT);
                            }
                        }
                    }

                    return super.causeFallDamage(distance, damageMultiplier, ds);
                }
            };

            e.restoreFrom(itementity);
            e.init();
            e.push(0,0.4,0);

            e.setPickUpDelay(20*2);
            e.setGlowingTag(true);

            e.setAirSupply(-1);

            e.setThrower(user.getUUID());

            user.level().addFreshEntity(e);

            user.getPersistentData().putLong(BREAK_ACTION_TIMEOUT, user.level().getGameTime() + 20*5);

            stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state->{
                if(0 < state.getRefine()){
                    state.setRefine(state.getRefine() - 1);
                    state.doBrokenAction(user);
                }
            });
        };
    }

    @Override
    public boolean hurtEnemy(ItemStack stackF, LivingEntity target, LivingEntity attacker) {

        ItemStack stack = attacker.getMainHandItem();

        stack.getCapability(BLADESTATE).ifPresent((state)->{
            state.resolvCurrentComboState(attacker).hitEffect(target, attacker);

            state.damageBlade(stack, 1, attacker, this.getOnBroken(stack));

        });

        return true;
    }
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (state.getDestroySpeed(worldIn, pos) != 0.0F) {
            stack.getCapability(BLADESTATE).ifPresent((s)->{
                s.damageBlade(stack, 1, entityLiving, this.getOnBroken(stack));
            });
        }

        return true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        int elapsed = this.getUseDuration(stack) - timeLeft;

        if (!worldIn.isClientSide) {

            stack.getCapability(BLADESTATE).ifPresent((state) -> {

                ComboState sa = state.doChargeAction(entityLiving, elapsed);

                //sa.tickAction(entityLiving);
                if (sa != ComboState.NONE){
                    state.damageBlade(stack, 1, entityLiving, this.getOnBroken(stack));
                    entityLiving.swing(InteractionHand.MAIN_HAND);
                }
            });
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        stack.getCapability(BLADESTATE).ifPresent((state)->{
            state.getComboSeq().holdAction(player);

            if(!player.level().isClientSide){
                int ticks = player.getTicksUsingItem();
                if(0 < ticks){

                    if( ticks == 20){//state.getFullChargeTicks(player)){
                        Vec3 pos = player.getEyePosition(1.0f).add(player.getLookAngle());
                        ((ServerLevel)player.level()).sendParticles(ParticleTypes.PORTAL,pos.x,pos.y,pos.z, 7, 0.7,0.7,0.7, 0.02);
                    }
                }
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if(!isSelected) {
            stack.getCapability(BLADESTATE).ifPresent((state)->{
                if(entityIn instanceof Player
                && ((Player) entityIn).hasEffect(MobEffects.HUNGER)
                && 0 < ((Player) entityIn).getFoodData().getFoodLevel()) {

                    int level = 1 + Math.abs(((LivingEntity) entityIn).getEffect(MobEffects.HUNGER).getAmplifier());
                    float amout = 0.0004f * level;

                    ((Player) entityIn).causeFoodExhaustion(0.005F * level);

                    state.setDamage(state.getDamage() - amout);
                }
            });

            return;
        };

        if(stack == null)
            return;
        if(entityIn == null)
            return;

        stack.getCapability(BLADESTATE).ifPresent((state)->{
            if(entityIn instanceof LivingEntity){

                entityIn.getCapability(INPUT_STATE).ifPresent(mInput->{
                    mInput.getScheduler().onTick((LivingEntity) entityIn);
                });

                /*
                if(0.5f > state.getDamage())
                    state.setDamage(0.99f);
                */

                state.resolvCurrentComboState((LivingEntity)entityIn).tickAction((LivingEntity)entityIn);
                state.sendChanges(entityIn);
            }
        });
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        return stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->s.getShareTag() != null)
                .map(s->{
                    CompoundTag tag = s.getShareTag();
                    tag.putString("translationKey", s.getTranslationKey());
                    if(tag.getBoolean("isBroken") != s.isBroken())
                        tag.putString("isBroken",Boolean.toString(s.isBroken()));

                    stack.addTagElement("ShareTag", tag);

                    return stack.getTag();
                })
                .orElseGet(()-> {

                    CompoundTag tag = stack.getCapability(ItemSlashBlade.BLADESTATE).map(s->
                        NBTHelper.getNBTCoupler(stack.getOrCreateTag())
                                .getChild("ShareTag").
                                put("translationKey", s.getTranslationKey()).
                                put("isBroken", Boolean.toString(s.isBroken())).
                                put("isNoScabbard", Boolean.toString(s.isNoScabbard())).getRawCompound()
                    ).orElseGet(()->new CompoundTag());

                    /*
                    CompoundNBT tag = stack.write(new CompoundNBT()).copy();

                    NBTHelper.getNBTCoupler(tag)
                            .getChild("ForgeCaps")
                            .getChild("slashblade:bladestate")
                            .doRawCompound("State", ISlashBladeState::removeActiveState);
                    */

                    stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->s.setShareTag(tag));

                    return stack.getTag();
                });

    }

    public static final String ICON_TAG_KEY = "SlashBladeIcon";
    //public static final String CLIENT_CAPS_KEY = "AllCapsData";

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {

        super.readShareTag(stack, nbt);

        if(nbt == null)
            return;

        if(nbt.contains(ICON_TAG_KEY)) {
            stack.deserializeNBT(nbt.getCompound(ICON_TAG_KEY));
            return;
        }

/*
        if(nbt.contains(CLIENT_CAPS_KEY,10)){
            stack.deserializeNBT(nbt.getCompound(CLIENT_CAPS_KEY));
        }else{
            stack.deserializeNBT(nbt);
        }

        DistExecutor.runWhenOn(Dist.CLIENT, ()->()->{
            CompoundNBT tag = nbt.copy();
            tag.remove(CLIENT_CAPS_KEY);
            stack.setTagInfo(CLIENT_CAPS_KEY, tag);
        });
        */
    }

    //damage ----------------------------------------------------------
    int getHalfMaxdamage(){
        return this.getMaxDamage() / 2;
    }
    
    @Override
    public int getDamage(ItemStack stack) {
        return getHalfMaxdamage();
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        if(damage == getHalfMaxdamage())
            return;
        
        //anti shrink damageItem
        if(damage > stack.getMaxDamage())
            stack.setCount(2);

        stack.getCapability(BLADESTATE).ifPresent((s)->{
            float amount = (damage - getHalfMaxdamage()) / (float)this.getMaxDamage();

            s.setDamage(s.getDamage() + amount);
        });
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return stack.getCapability(BLADESTATE).map(s->0 < s.getDamage()).orElse(false);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return Math.min(amount, getHalfMaxdamage() / 2);
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        return Minecraft.getInstance().player.getMainHandItem() == stack;

        //super.showDurabilityBar(stack);
        //return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.F - 13.0F * stack.getCapability(BLADESTATE).map(s->s.getDamage()).orElse(0.0f));
        //return super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        boolean isBroken = stack.getCapability(BLADESTATE).filter(s->s.isBroken()).isPresent();

        return isBroken ? 0xFF66AE : 0x02E0EE;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return stack.getCapability(BLADESTATE)
                .filter((s)->!s.getTranslationKey().isEmpty())
                .map((state)->state.getTranslationKey())
                .orElseGet(()->super.getDescriptionId(stack));
    }

    @OnlyIn(Dist.CLIENT)
    public static RecipeManager getClientRM(){
        ClientLevel cw = Minecraft.getInstance().level;
        if(cw != null)
            return cw.getRecipeManager();
        else
            return null;
    }
    public static RecipeManager getServerRM(){
        MinecraftServer sw = ServerLifecycleHooks.getCurrentServer();
        if(sw != null)
            return sw.getRecipeManager();
        else
            return null;
    }


    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {

        if(Ingredient.of(ItemTags.STONE_TOOL_MATERIALS).test(repair)){
            return true;
        }

        /*
        Tag<Item> tags = ItemTags.getCollection().get(new ResourceLocation("slashblade","proudsouls"));

        if(tags != null){
            boolean result = Ingredient.fromTag(tags).test(repair);
        }*/

        //todo: repair custom material

        return super.isValidRepairItem(toRepair, repair);
    }

    RangeMap refineColor = ImmutableRangeMap.builder()
            .put(Range.lessThan(10), ChatFormatting.WHITE)
            .put(Range.closedOpen(10,50), ChatFormatting.YELLOW)
            .put(Range.closedOpen(50,100), ChatFormatting.GREEN)
            .put(Range.closedOpen(100,150), ChatFormatting.AQUA)
            .put(Range.closedOpen(150,200), ChatFormatting.BLUE)
            .put(Range.atLeast(200), ChatFormatting.LIGHT_PURPLE)
            .build();


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            if(0 < s.getKillCount())
                tooltip.add(Component.translatable("slashblade.tooltip.killcount", s.getKillCount()));

            if(0 < s.getRefine()){
                tooltip.add(Component.translatable("slashblade.tooltip.refine", s.getRefine()).withStyle((ChatFormatting)refineColor.get(s.getRefine())));
            }
        });

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }


    /**
     * @return true = cancel : false = swing
     */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return !stack.getCapability(BLADESTATE).filter(s->s.getLastActionTime() == entity.level().getGameTime()).isPresent();
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, world);
        e.restoreFrom(location);
        e.init();
        return e;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level world) {
        return super.getEntityLifespan(itemStack, world);// Short.MAX_VALUE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {
            BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });

        super.initializeClient(consumer);
    }
}
