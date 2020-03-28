package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
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
        if(super.onItemUse(context) == ActionResultType.SUCCESS) {

            BlockPos clickPos = context.getPos();
            Direction direction = context.getFace();
            BlockPos placePos = clickPos.offset(direction);
            PlayerEntity player = context.getPlayer();
            ItemStack stack = context.getItem();

            World world = context.getWorld();

            HangingEntity entity;
            entity = BladeStandEntity.createInstanceFromPos(world, placePos, direction, this);

            CompoundNBT compoundnbt = stack.getTag();
            if (compoundnbt != null) {
                EntityType.applyItemNBT(world, player, entity, compoundnbt);
            }

            if (entity.onValidSurface()) {
                if (!world.isRemote) {
                    entity.playPlaceSound();
                    world.addEntity(entity);
                }

                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }else{
            return ActionResultType.FAIL;
        }
    }

    protected boolean canPlace(PlayerEntity player, Direction dir, ItemStack stack, BlockPos pos) {
        if(isWallType)
            return !dir.getAxis().isVertical() && !World.isOutsideBuildHeight(pos) && player.canPlayerEdit(pos, dir, stack);
        else
            return (dir == Direction.UP) && !World.isOutsideBuildHeight(pos) && player.canPlayerEdit(pos, dir, stack);
    }
}
