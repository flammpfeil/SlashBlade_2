package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SlashBlade.modid, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(){
        int id = 0;
        INSTANCE.registerMessage(id++,
                MoveCommandMessage.class,
                MoveCommandMessage::encode,
                MoveCommandMessage::decode,
                MoveCommandMessage::handle
        );

        INSTANCE.registerMessage(id++,
                ActiveStateSyncMessage.class,
                ActiveStateSyncMessage::encode,
                ActiveStateSyncMessage::decode,
                ActiveStateSyncMessage::handle
        );

        INSTANCE.registerMessage(id++,
                RankSyncMessage.class,
                RankSyncMessage::encode,
                RankSyncMessage::decode,
                RankSyncMessage::handle
        );

        INSTANCE.registerMessage(id++,
                MotionBroadcastMessage.class,
                MotionBroadcastMessage::encode,
                MotionBroadcastMessage::decode,
                MotionBroadcastMessage::handle
        );
    }

}
