package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Consumer;
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
        ctx.get().setPacketHandled(true);

        if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        Consumer<Long> handler = DistExecutor.callWhenOn(Dist.CLIENT, ()->()->RankSyncMessage::setPoint);

        if(handler != null)
            ctx.get().enqueueWork(() -> {
                handler.accept(msg.rawPoint);
            });

    }

    @OnlyIn(Dist.CLIENT)
    static public void setPoint(long point){
        PlayerEntity pl = Minecraft.getInstance().player;
        pl.getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cr->{

            long time = pl.world.getGameTime();

            IConcentrationRank.ConcentrationRanks oldRank = cr.getRank(time);

            cr.setRawRankPoint(point);
            cr.setLastUpdte(time);

            if(oldRank.level < cr.getRank(time).level)
                cr.setLastRankRise(time);
        });
    }
}