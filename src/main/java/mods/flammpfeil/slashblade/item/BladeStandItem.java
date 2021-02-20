package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos blockpos = context.getPos();
        Direction direction = context.getFace();
        BlockPos blockpos1 = blockpos.offset(direction);
        PlayerEntity playerentity = context.getPlayer();
        ItemStack itemstack = context.getItem();
        if (playerentity != null && !this.canPlace(playerentity, direction, itemstack, blockpos1)) {
            return ActionResultType.FAIL;
        } else {
            World world = context.getWorld();
            HangingEntity hangingentity = BladeStandEntity.createInstanceFromPos(world, blockpos1, direction, this);

            CompoundNBT compoundnbt = itemstack.getTag();
            if (compoundnbt != null) {
                EntityType.applyItemNBT(world, playerentity, hangingentity, compoundnbt);
            }

            if (hangingentity.onValidSurface()) {
                if (!world.isRemote) {
                    hangingentity.playPlaceSound();
                    world.addEntity(hangingentity);
                }

                itemstack.shrink(1);
                return ActionResultType.func_233537_a_(world.isRemote);
            } else {
                return ActionResultType.CONSUME;
            }
        }
    }

    protected boolean canPlace(PlayerEntity player, Direction dir, ItemStack stack, BlockPos pos) {
        if(isWallType)
            return !dir.getAxis().isVertical() && !World.isOutsideBuildHeight(pos) && player.canPlayerEdit(pos, dir, stack);
        else
            return (dir == Direction.UP) && !World.isOutsideBuildHeight(pos) && player.canPlayerEdit(pos, dir, stack);
    }
}
