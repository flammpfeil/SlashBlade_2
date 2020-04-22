/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.CarryType;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialattack.SlashArts;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

public class CapabilitySlashBlade
{
    @CapabilityInject(ISlashBladeState.class)
    public static Capability<ISlashBladeState> BLADESTATE = null;

    protected static Vec3d readVec3dFrom(CompoundNBT tag, String key){
        ListNBT list = tag.getList(key, 6);
        return new Vec3d(list.getDouble(0), list.getDouble(1),list.getDouble(2));
    }

    protected static ListNBT newDoubleNBTList(Vec3d vec){
        return newDoubleNBTList(vec.x,vec.y,vec.z);
    }
    protected static ListNBT newDoubleNBTList(double... numbers) {
        ListNBT listnbt = new ListNBT();

        for(double d0 : numbers) {
            listnbt.add(DoubleNBT.valueOf(d0));
        }

        return listnbt;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ISlashBladeState.class, new IStorage<ISlashBladeState>()
        {

            @Override
            public INBT writeNBT(Capability<ISlashBladeState> capability, ISlashBladeState instance, Direction side)
            {
                CompoundNBT tag = new CompoundNBT();

                //action state
                tag.putLong("lastActionTime" , instance.getLastActionTime());
                tag.putInt("TargetEntity", instance.getTargetEntityId());
                tag.putBoolean("_onClick", instance.onClick());
                tag.putFloat("fallDecreaseRate", instance.getFallDecreaseRate());
                tag.putBoolean("isCharged", instance.isCharged());
                tag.putFloat("AttackAmplifier", instance.getAttackAmplifier());
                tag.putString("currentCombo", instance.getComboSeq().getName());
                tag.putString("lastPosHash", instance.getLastPosHash());
                tag.putBoolean("HasShield", instance.hasShield());

                tag.putFloat("Damage", instance.getDamage());

                tag.putBoolean("isBroken", instance.isBroken());

                //passive state
                tag.putBoolean("isNoScabbard", instance.isNoScabbard());
                tag.putBoolean("isSealed", instance.isSealed());

                tag.putFloat("baseAttackModifier", instance.getBaseAttackModifier());

                tag.putInt("killCount", instance.getKillCount());
                tag.putInt("RepairCounter", instance.getRefine());

                UUID id = instance.getOwner();
                if(id != null)
                    tag.putUniqueId("Owner", id);


                UUID bladeId = instance.getUniqueId();
                tag.putUniqueId("BladeUniqueId", bladeId);


                //performance setting
                tag.putString("RangeAttackType", instance.getRangeAttackType().getName());

                tag.putString("SpecialAttackType", Optional.ofNullable(instance.getSlashArtsKey()).orElse("none"));
                tag.putBoolean("isDestructable", instance.isDestructable());
                tag.putBoolean("isDefaultBewitched", instance.isDefaultBewitched());
                tag.putByte("rarityType", (byte)instance.getRarity().ordinal());
                tag.putString("translationKey", instance.getTranslationKey());

                //render info
                tag.putByte("StandbyRenderType", (byte)instance.getCarryType().ordinal());
                tag.putInt("SummonedSwordColor", instance.getColorCode());
                tag.putBoolean("SummonedSwordColorInverse", instance.isEffectColorInverse());
                tag.put("adjustXYZ" , newDoubleNBTList(instance.getAdjust()));

                instance.getTexture()
                        .ifPresent(loc ->  tag.putString("TextureName", loc.toString()));
                instance.getModel()
                        .ifPresent(loc ->  tag.putString("ModelName", loc.toString()));

                tag.putString("ComboRoot", Optional.ofNullable(instance.getComboRoot()).map((c)->c.getName()).orElseGet(()->"standby"));


                return tag;
            }

            private <T extends Enum> T fromOrdinal(T[] values, int ordinal ,T def){
                if(0 <= ordinal && ordinal < values.length){
                    return values[ordinal];
                }else{
                    return def;
                }
            }

            @Override
            public void readNBT(Capability<ISlashBladeState> capability, ISlashBladeState instance, Direction side, INBT nbt)
            {
                CompoundNBT tag = (CompoundNBT)nbt;
                if (!(instance instanceof SlashBladeState))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");

                //action state
                instance.setLastActionTime(tag.getLong("lastActionTime"));
                instance.setTargetEntityId(tag.getInt("TargetEntity"));
                instance.setOnClick(tag.getBoolean("_onClick"));
                instance.setFallDecreaseRate(tag.getFloat("fallDecreaseRate"));
                instance.setCharged(tag.getBoolean("isCharged"));
                instance.setAttackAmplifier(tag.getFloat("AttackAmplifier"));
                instance.setComboSeq(ComboState.NONE.valueOf(tag.getString("currentCombo")));
                instance.setLastPosHash(tag.getString("lastPosHash"));
                instance.setHasShield(tag.getBoolean("HasShield"));

                instance.setDamage(tag.getFloat("Damage"));

                instance.setBroken(tag.getBoolean("isBroken"));

                instance.setHasChangedActiveState(true);


                //passive state
                instance.setNoScabbard(tag.getBoolean("isNoScabbard"));
                instance.setSealed(tag.getBoolean("isSealed"));

                instance.setBaseAttackModifier(tag.getFloat("baseAttackModifier"));

                instance.setKillCount(tag.getInt("killCount"));
                instance.setRefine(tag.getInt("RepairCounter"));

                instance.setOwner(tag.hasUniqueId("Owner") ? tag.getUniqueId("Owner") : null);

                instance.setUniqueId(tag.hasUniqueId("BladeUniqueId") ? tag.getUniqueId("BladeUniqueId") : UUID.randomUUID());

                //performance setting
                instance.setRangeAttackType(RangeAttack.NONE.valueOf(tag.getString("RangeAttackType")));

                instance.setSlashArtsKey(tag.getString("SpecialAttackType"));
                instance.setDestructable(tag.getBoolean("isDestructable"));
                instance.setDefaultBewitched(tag.getBoolean("isDefaultBewitched"));

                instance.setRarity(fromOrdinal(Rarity.values(), tag.getByte("rarityType"), Rarity.COMMON));

                instance.setTranslationKey(tag.getString("translationKey"));

                //render info
                instance.setCarryType(fromOrdinal(CarryType.values(), tag.getByte("StandbyRenderType"), CarryType.DEFAULT));
                instance.setColorCode(tag.getInt("SummonedSwordColor"));
                instance.setEffectColorInverse(tag.getBoolean("SummonedSwordColorInverse"));
                instance.setAdjust(readVec3dFrom(tag, "adjustXYZ"));

                if(tag.contains("TextureName"))
                    instance.setTexture(new ResourceLocation(tag.getString("TextureName")));
                else
                    instance.setTexture(null);

                if(tag.contains("ModelName"))
                    instance.setModel(new ResourceLocation(tag.getString("ModelName")));
                else
                    instance.setModel(null);

                instance.setComboRootName(tag.getString("ComboRoot"));
            }
        },
        () -> new SlashBladeState());
    }
}