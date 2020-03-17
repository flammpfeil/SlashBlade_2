package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.ability.LockOnManager;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.imputstate.CapabilityImputState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.ImputCommand;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.EnumSet;
import java.util.function.Supplier;

public class RankSyncMessage {
    public long rawPoint;


    public RankSyncMessage(){}

    static public RankSyncMessage decode(PacketBuffer buf) {
        RankSyncMessage msg = new RankSyncMessage();
        msg.rawPoint = buf.readLong();
        return msg;
    }

    static public void encode(RankSyncMessage msg, PacketBuffer buf) {
        buf.writeLong(msg.rawPoint);
    }

    static public void handle(RankSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().setPacketHandled(true);
            return;
        }

        ctx.get().enqueueWork(() -> {
            PlayerEntity pl = Minecraft.getInstance().player;
            pl.getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cr->{

                long time = pl.world.getGameTime();

                IConcentrationRank.ConcentrationRanks oldRank = cr.getRank(time);

                cr.setRawRankPoint(msg.rawPoint);
                cr.setLastUpdte(time);

                if(oldRank.level < cr.getRank(time).level)
                    cr.setLastRankRise(time);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}