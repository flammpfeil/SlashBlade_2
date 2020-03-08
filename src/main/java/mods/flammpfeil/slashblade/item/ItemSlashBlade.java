package mods.flammpfeil.slashblade.item;

import com.google.common.collect.*;
import com.google.gson.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.imputstate.IImputState;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateCapabilityProvider;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import mods.flammpfeil.slashblade.util.ImputCommand;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemSlashBlade extends SwordItem {
    protected static final UUID ATTACK_DAMAGE_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FD");
    protected static final UUID PLAYER_REACH_AMPLIFIER = UUID.fromString("2D988C13-595B-4E58-B254-39BB6FA077FD");

    @CapabilityInject(ISlashBladeState.class)
    public static Capability<ISlashBladeState> BLADESTATE = null;
    @CapabilityInject(IImputState.class)
    public static Capability<IImputState> IMPUT_STATE = null;

    public ItemSlashBlade(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> result = super.getAttributeModifiers(slot,stack);

        if (slot == EquipmentSlotType.MAINHAND) {

            LazyOptional<ISlashBladeState> state = stack.getCapability(BLADESTATE);
            state.ifPresent(s -> {
                float baseAttackModifier = s.getBaseAttackModifier();
                AttributeModifier base = new AttributeModifier(ATTACK_DAMAGE_MODIFIER,
                        "Weapon modifier",
                        (double) baseAttackModifier,
                        AttributeModifier.Operation.ADDITION);
                result.remove(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),base);
                result.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),base);

                float rankAttackAmplifier = s.getAttackAmplifier();
                result.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                        new AttributeModifier(ATTACK_DAMAGE_AMPLIFIER,
                                "Weapon amplifier",

                                (double) rankAttackAmplifier,
                                AttributeModifier.Operation.ADDITION));



                result.put(PlayerEntity.REACH_DISTANCE.getName(), new AttributeModifier(PLAYER_REACH_AMPLIFIER,
                        "Reach amplifer",
                        s.isBroken() ? 0 : 1.0, AttributeModifier.Operation.ADDITION));

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

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        boolean result = itemstack.getCapability(BLADESTATE).map((state) -> {

            playerIn.getCapability(IMPUT_STATE).ifPresent((s)->s.getCommands().add(ImputCommand.R_CLICK));

            ComboState combo = state.progressCombo(playerIn);
            state.setLastActionTime(worldIn.getGameTime());
            combo.clickAction(playerIn);

            playerIn.getCapability(IMPUT_STATE).ifPresent((s)->s.getCommands().remove(ImputCommand.R_CLICK));
            return true;
        }).orElse(false);

        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemstack, PlayerEntity playerIn, Entity entity) {

        World worldIn = playerIn.world;

        LazyOptional<ISlashBladeState> stateHolder = itemstack.getCapability(BLADESTATE)
                .filter((state) -> !state.onClick());

        stateHolder.ifPresent((state) -> {
            playerIn.getCapability(IMPUT_STATE).ifPresent((s)->s.getCommands().add(ImputCommand.L_CLICK));

            ComboState combo = state.progressCombo(playerIn);
            state.setLastActionTime(worldIn.getGameTime());
            combo.clickAction(playerIn);

            playerIn.getCapability(IMPUT_STATE).ifPresent((s)->s.getCommands().remove(ImputCommand.L_CLICK));
        });

        return stateHolder.isPresent();
    }

    private void onBroken(LivingEntity user){
        user.sendBreakAnimation(EquipmentSlotType.MAINHAND);

        ItemStack soul = new ItemStack(SBItems.proudsoul);
        ItemEntity itementity = new ItemEntity(user.world, user.posX, user.posY , user.posZ, soul);
        itementity.setNoPickupDelay();
        user.world.addEntity(itementity);
    }

    @Override
    public boolean hitEntity(ItemStack stackF, LivingEntity target, LivingEntity attacker) {

        ItemStack stack = attacker.getHeldItemMainhand();

        stack.getCapability(BLADESTATE).ifPresent((state)->{
            state.resolvCurrentComboState(attacker).hitEffect(target);

            state.damageBlade(stack, 1, attacker, this::onBroken);

        });

        return true;
    }
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.getCapability(BLADESTATE).ifPresent((s)->{
                s.damageBlade(stack, 1, entityLiving, this::onBroken);
            });
        }

        return true;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        int elapsed = this.getUseDuration(stack) - timeLeft;
        if(20*1 < elapsed){

            if (!worldIn.isRemote) {

                stack.getCapability(BLADESTATE).ifPresent((state)->{

                    ComboState sa = state.progressCombo(entityLiving, elapsed);

                    sa.tickAction(entityLiving);
                    if(sa != ComboState.NONE)
                        state.damageBlade(stack, 1, entityLiving, this::onBroken);
                });


                worldIn.playSound((PlayerEntity)null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 0.8F / (random.nextFloat() * 0.4F + 0.8F));

                Vec3d eyePos = entityLiving.getEyePosition(1.0f);

                final double airReach = 5;
                final double entityReach = 7;
                Optional<RayTraceResult> raytraceresult = RayTraceHelper.rayTrace(
                        worldIn, entityLiving, eyePos, entityLiving.getLookVec(), airReach, entityReach,
                        (entity) -> {
                            return !entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith() && (entity != entityLiving);
                        });

                Vec3d resultPos = raytraceresult.map((rtr)->{
                    Vec3d pos = null;
                    RayTraceResult.Type type = rtr.getType();
                    switch (type){
                        case ENTITY:
                            Entity target = ((EntityRayTraceResult)rtr).getEntity();
                            pos = target.getPositionVec().add(0,target.getEyeHeight() / 2.0f,0);
                            break;
                        case BLOCK:
                            Vec3d hitVec = rtr.getHitVec();
                            pos = hitVec;
                            break;
                    }
                    return pos;

                }).orElseGet(()->eyePos.add(entityLiving.getLookVec().scale(airReach)));


                EntityJudgementCut jc = new EntityJudgementCut(SlashBlade.RegistryEvents.JudgementCut, worldIn);
                jc.setPosition(resultPos.x ,resultPos.y ,resultPos.z);
                jc.setShooter(entityLiving);
                worldIn.addEntity(jc);




                /*
                EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn);
                ss.setPosition(entityLiving.posX,entityLiving.posY + (double)entityLiving.getEyeHeight() - (double)0.1F,entityLiving.posZ);

                float rotationYawIn = entityLiving.rotationYaw;
                float rotationPitchIn = entityLiving.rotationPitch;
                float pitchOffset = 0;

                float f = -MathHelper.sin(rotationYawIn * ((float)Math.PI / 180F)) * MathHelper.cos(rotationPitchIn * ((float)Math.PI / 180F));
                float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * ((float)Math.PI / 180F));
                float f2 = MathHelper.cos(rotationYawIn * ((float)Math.PI / 180F)) * MathHelper.cos(rotationPitchIn * ((float)Math.PI / 180F));
                ss.shoot((double)f, (double)f1, (double)f2, 1.5F, 1.0F);
                worldIn.addEntity(ss);
                */

                /*
                SnowballEntity ballentity = new SnowballEntity(worldIn, entityLiving);
                ballentity.func_213884_b(stack);
                ballentity.shoot(entityLiving, entityLiving.rotationPitch, entityLiving.rotationYaw, 0.0F, 1.5F, 1.0F);
                worldIn.addEntity(ballentity);
                /**/
            }
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        stack.getCapability(BLADESTATE).ifPresent((state)->{
            state.getComboSeq().holdAction(player);
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        if(!isSelected) return;

        stack.getCapability(BLADESTATE).ifPresent((state)->{
            if(entityIn instanceof LivingEntity){
                state.resolvCurrentComboState((LivingEntity)entityIn).tickAction((LivingEntity)entityIn);
                state.sendChanges(entityIn);
            }
        });
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return stack.getCapability(BLADESTATE).map(s->{
            stack.setTagInfo("ShareTag",s.getShareTag());
            return s.getShareTag();
        }).orElse(new CompoundNBT());
    }

    public static final String ICON_TAG_KEY = "SlashBladeIcon";

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if(nbt.contains(ICON_TAG_KEY)){
            stack.deserializeNBT(nbt.getCompound(ICON_TAG_KEY));
            stack.getCapability(BLADESTATE).ifPresent(s->{
                stack.setTagInfo("ShareTag",s.getShareTag());
            });
        }else{
            stack.getCapability(BLADESTATE).ifPresent(s->{
                s.setShareTag(nbt);
                stack.setTagInfo("ShareTag",s.getShareTag());
            });
        }
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
    public boolean showDurabilityBar(ItemStack stack) {
        return Minecraft.getInstance().player.getHeldItemMainhand() == stack;

        //super.showDurabilityBar(stack);
        //return false;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return stack.getCapability(BLADESTATE).map(s->s.getDamage()).orElse(0.0f);
        //return super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        boolean isBroken = stack.getCapability(BLADESTATE).filter(s->s.isBroken()).isPresent();

        return isBroken ? 0xFF66AE : 0x02E0EE;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return stack.getCapability(BLADESTATE)
                .filter((s)->!s.getTranslationKey().isEmpty())
                .map((state)->state.getTranslationKey())
                .orElseGet(()->super.getTranslationKey(stack));
    }

    @Override
    protected boolean isInGroup(ItemGroup group) {
        if(group == SlashBlade.SLASHBLADE)
            return true;
        else
            return super.isInGroup(group);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);

        if(group == SlashBlade.SLASHBLADE){
            RecipeManager rm = ServerLifecycleHooks.getCurrentServer().getRecipeManager();

            Set<ResourceLocation> keys =rm.getKeys()
                    .filter((loc)->loc.getNamespace().equals(SlashBlade.modid)
                            && (!(loc.getPath().startsWith("material") || loc.getPath().startsWith("simple_slashblade"))))
                    .collect(Collectors.toSet());

            List<ItemStack> allItems = keys.stream()
                    .map(key->rm.getRecipe(key)
                            .map(r->((IRecipe) r).getRecipeOutput())
                            .orElseGet(()->ItemStack.EMPTY))
                    .sorted(Comparator.comparing(s->((ItemStack)s).getTranslationKey()).reversed())
                    .collect(Collectors.toList());

            items.addAll(allItems);
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        /*
        Tag<Item> tags = ItemTags.getCollection().get(new ResourceLocation("slashblade","proudsouls"));

        if(tags != null){
            boolean result = Ingredient.fromTag(tags).test(repair);
        }*/

        //todo: repair custom material

        return super.getIsRepairable(toRepair, repair);
    }

    RangeMap refineColor = ImmutableRangeMap.builder()
            .put(Range.lessThan(10), TextFormatting.WHITE)
            .put(Range.closedOpen(10,50), TextFormatting.YELLOW)
            .put(Range.closedOpen(50,100), TextFormatting.GREEN)
            .put(Range.closedOpen(100,150), TextFormatting.AQUA)
            .put(Range.closedOpen(150,200), TextFormatting.BLUE)
            .put(Range.atLeast(200), TextFormatting.LIGHT_PURPLE)
            .build();


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            if(0 < s.getKillCount())
                tooltip.add(new TranslationTextComponent("slashblade.tooltip.killcount", s.getKillCount()));

            if(0 < s.getRefine()){
                tooltip.add(new TranslationTextComponent("slashblade.tooltip.refine", s.getRefine()).applyTextStyle((TextFormatting)refineColor.get(s.getRefine())));
            }
        });

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
