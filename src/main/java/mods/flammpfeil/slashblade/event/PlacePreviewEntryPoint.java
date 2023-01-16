package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;


public class PlacePreviewEntryPoint {
    private static final class SingletonHolder {
        private static final PlacePreviewEntryPoint instance = new PlacePreviewEntryPoint();
    }
    public static PlacePreviewEntryPoint getInstance() {
        return SingletonHolder.instance;
    }
    private PlacePreviewEntryPoint(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClick(PlayerInteractEvent.RightClickItem event) {
        Player trueSource = event.getEntity();

        if (!(trueSource instanceof LivingEntity)) return;

        ItemStack stack = event.getItemStack();
        if(stack.isEmpty()) return;
        if(stack.getItem() != SBItems.proudsoul) return;

        Level worldIn = trueSource.getCommandSenderWorld();

        /*
        PlacePreviewEntity ss = new PlacePreviewEntity(SlashBlade.RegistryEvents.PlacePreview, worldIn);

        Vector3d pos = trueSource.getEyePosition(1.0f).add(trueSource.getLookVec().scale(3.0)).align(EnumSet.of(Direction.Axis.X,Direction.Axis.Y,Direction.Axis.Z));

        ss.setPosition(pos.x, pos.y, pos.z);


        //ss.setShooter(trueSource);

        worldIn.addEntity(ss);
        */
    }
}
