package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.item.Item.Properties;

public class BladeStandItem extends HangingEntityItem {
    private boolean isWallType;
    public BladeStandItem(Properties builder) {
        this(builder, false);
    }
    public BladeStandItem(Properties builder, boolean isWallType) {
        super(SlashBlade.RegistryEvents.BladeStand, builder);

        this.isWallType = isWallType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        Player playerentity = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();
        if (playerentity != null && !this.mayPlace(playerentity, direction, itemstack, blockpos1)) {
            return InteractionResult.FAIL;
        } else {
            Level world = context.getLevel();
            HangingEntity hangingentity = BladeStandEntity.createInstanceFromPos(world, blockpos1, direction, this);

            CompoundTag compoundnbt = itemstack.getTag();
            if (compoundnbt != null) {
                EntityType.updateCustomEntityTag(world, playerentity, hangingentity, compoundnbt);
            }

            if (hangingentity.survives()) {
                if (!world.isClientSide) {
                    hangingentity.playPlacementSound();
                    world.addFreshEntity(hangingentity);
                }

                itemstack.shrink(1);
                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player player, Direction dir, ItemStack stack, BlockPos pos) {
        if(isWallType)
            return !dir.getAxis().isVertical() && !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, dir, stack);
        else
            return (dir == Direction.UP) && !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, dir, stack);
    }
}
