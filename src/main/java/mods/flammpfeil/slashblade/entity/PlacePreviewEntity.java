package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class PlacePreviewEntity extends ItemFrameEntity implements IEntityAdditionalSpawnData {

    public Item currentType = null;
    public ItemStack currentTypeStack = ItemStack.EMPTY;

    public PlacePreviewEntity(EntityType<? extends PlacePreviewEntity> p_i50224_1_, World p_i50224_2_) {
        super(p_i50224_1_, p_i50224_2_);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        String standTypeStr;
        if(this.currentType != null){
            standTypeStr = this.currentType.getRegistryName().toString();
        }else{
            standTypeStr = "";
        }
        compound.putString("StandType", standTypeStr);

        compound.putByte("Pose", (byte)this.getPose().ordinal());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.currentType = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("StandType")));

        this.setPose(Pose.values()[compound.getByte("Pose") % Pose.values().length]);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        CompoundNBT tag = new CompoundNBT();
        this.writeAdditional(tag);
        buffer.writeCompoundTag(tag);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        CompoundNBT tag = additionalData.readCompoundTag();
        this.readAdditional(tag);
    }

    public static PlacePreviewEntity createInstanceFromPos(World worldIn, BlockPos placePos, Direction dir, Item type) {
        PlacePreviewEntity e = new PlacePreviewEntity(SlashBlade.RegistryEvents.PlacePreview, worldIn);

        e.hangingPosition = placePos;
        e.updateFacingWithBoundingBox(dir);
        e.currentType = type;

        return e;
    }

    public static PlacePreviewEntity createInstance(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        return new PlacePreviewEntity(SlashBlade.RegistryEvents.PlacePreview, world);
    }

    @Nullable
    @Override
    public ItemEntity entityDropItem(IItemProvider iip) {
        if(iip == Items.ITEM_FRAME){
            if(this.currentType == null || this.currentType == Items.AIR)
                return null;

            iip = this.currentType;
        }
        return super.entityDropItem(iip);
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        ActionResultType result = ActionResultType.PASS;
        if(!this.world.isRemote){
            ItemStack itemstack = player.getHeldItem(hand);
            if(player.isSneaking() && !this.getDisplayedItem().isEmpty()){
                Pose current = this.getPose();
                int newIndex = (current.ordinal() + 1) % Pose.values().length;
                this.setPose(Pose.values()[newIndex]);
                result = ActionResultType.SUCCESS;
            }else if((!itemstack.isEmpty() && itemstack.getItem() instanceof ItemSlashBlade)
                    || (itemstack.isEmpty() && !this.getDisplayedItem().isEmpty())){

                if(this.getDisplayedItem().isEmpty()){
                    result = super.processInitialInteract(player, hand);
                }else{
                    ItemStack displayed = this.getDisplayedItem().copy();

                    this.setDisplayedItem(ItemStack.EMPTY);
                    result = super.processInitialInteract(player, hand);

                    player.setHeldItem(hand, displayed);
                }

            }else {
                this.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
                this.setItemRotation(this.getRotation() + 1);
                result = ActionResultType.SUCCESS;
            }
        }
        return result;
    }

    private BlockState tile = Blocks.SAND.getDefaultState();

    public BlockState getBlockState() {
        return this.tile;
    }
}
