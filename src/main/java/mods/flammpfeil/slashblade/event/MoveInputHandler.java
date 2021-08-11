package mods.flammpfeil.slashblade.event;

import com.google.gson.*;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.network.MoveCommandMessage;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;

public class MoveInputHandler {

    @CapabilityInject(IInputState.class)
    public static Capability<IInputState> INPUT_STATE = null;

    public static boolean checkFlag(int data, int flags){
        return (data & flags) == flags;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    static public void onPlayerPostTick(TickEvent.PlayerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        if(!(event.player instanceof ClientPlayerEntity)) return;

        ClientPlayerEntity player = (ClientPlayerEntity)event.player;

        EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);

        if(player.movementInput.forwardKeyDown)
            commands.add(InputCommand.FORWARD);
        if(player.movementInput.backKeyDown)
            commands.add(InputCommand.BACK);
        if(player.movementInput.leftKeyDown)
            commands.add(InputCommand.LEFT);
        if(player.movementInput.rightKeyDown)
            commands.add(InputCommand.RIGHT);

        if(player.movementInput.sneaking)
            commands.add(InputCommand.SNEAK);

        if(Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown())
            commands.add(InputCommand.SPRINT);

/*
        if((player.movementInput.sneak && SlashBlade.SneakForceLockOn)
                || CoreProxyClient.lockon.isKeyDown())
            message.activeTag += MoveCommandMessage.SNEAK;

        
        if(CoreProxyClient.camera.isKeyDown())
            message.activeTag += MoveCommandMessage.CAMERA;

        if(CoreProxyClient.styleaction.isKeyDown())
            message.activeTag += MoveCommandMessage.STYLE;
*/

        if(Minecraft.getInstance().gameSettings.keyBindUseItem.isKeyDown())
            commands.add(InputCommand.R_DOWN);
        if(Minecraft.getInstance().gameSettings.keyBindAttack.isKeyDown())
            commands.add(InputCommand.L_DOWN);

        if(Minecraft.getInstance().gameSettings.keyBindPickBlock.isKeyDown())
            commands.add(InputCommand.M_DOWN);


        if(Minecraft.getInstance().gameSettings.keyBindSaveToolbar.isKeyDown())
            commands.add(InputCommand.SAVE_TOOLBAR);

        EnumSet<InputCommand> old = player.getCapability(INPUT_STATE)
                .map((state)->state.getCommands())
                .orElseGet(()->EnumSet.noneOf(InputCommand.class));

        long currentTime = player.getEntityWorld().getGameTime();

        /*
        if(player.movementInput.forwardKeyDown &&  (0 < (player.getPersistentData().getInt(KEY) & MoveCommandMessage.SNEAK)))
            player.getPersistentData().putLong("SB.MCS.F",currentTime);
        if(player.movementInput.backKeyDown &&  (0 < (player.getPersistentData().getInt(KEY) & MoveCommandMessage.SNEAK)))
            player.getPersistentData().putLong("SB.MCS.B",currentTime);
        */

        boolean doCopy = player.isCreative();

        if(doCopy && old.contains(InputCommand.SAVE_TOOLBAR) && !commands.contains(InputCommand.SAVE_TOOLBAR)){
            ItemStack stack = player.getHeldItemMainhand();

            JsonObject ret = new JsonObject();

            String str = "";
            if(KeyModifier.SHIFT.isActive(KeyConflictContext.UNIVERSAL)){
                str = AdvancementBuilder.getAdvancementJsonStr(stack);
            }else{

                ret.addProperty("item", stack.getItem().getRegistryName().toString());
                if (stack.getCount() != 1)
                    ret.addProperty("count", stack.getCount());

                CompoundNBT tag = new CompoundNBT();
                stack.write(tag);

                CompoundNBT nbt = stack.getOrCreateTag().copy();
                if(tag.contains("ForgeCaps"))
                    nbt.put("ForgeCaps", tag.get("ForgeCaps"));

                if(KeyModifier.ALT.isActive(KeyConflictContext.UNIVERSAL)){
                    //add anvilcrafting recipe template
                    AnvilCraftingRecipe acr = new AnvilCraftingRecipe();

                    ItemStack result = player.getHeldItemOffhand();
                    acr.setResult(result);

                    nbt.put("RequiredBlade",acr.writeNBT());
                }

                if(KeyModifier.CONTROL.isActive(KeyConflictContext.UNIVERSAL)){
                    //add anvilcrafting recipe template
                    ItemStack result = player.getHeldItemMainhand();

                    CompoundNBT iconNbt = result.write(new CompoundNBT());

                    ret.addProperty("iconStr_nbt",iconNbt.toString());


                    JsonObject criteriaitem = new JsonObject();
                    criteriaitem.addProperty("item", result.getItem().getRegistryName().toString());

                    CompoundNBT checktarget = new CompoundNBT();
                    {
                        NBTHelper.NBTCoupler nbtc = NBTHelper.getNBTCoupler(checktarget)
                                .getChild("ForgeCaps")
                                .getChild("slashblade:bladestate")
                                .getChild("State");

                        result.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
                            if(s.isBroken())
                                nbtc.put("isBroken",s.isBroken());
                            if(s.getTranslationKey() != null || !s.getTranslationKey().isEmpty())
                                nbtc.put("translationKey",s.getTranslationKey());
                        });
                    }
                    criteriaitem.addProperty("nbt",checktarget.toString());
                    ret.add("CriteriaItem", criteriaitem);
                }

                JsonElement element = null;
                element = (new JsonParser()).parse(JSONUtil.NBTtoJsonString(nbt));

                if (stack.getTag() != null && element != null)
                    ret.add("nbt", element);

                Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                GSON.toJson(ret);
            }

            Minecraft.getInstance().keyboardListener.setClipboardString(str);
        }


        if(!old.equals(commands)){
            player.getCapability(INPUT_STATE)
                    .ifPresent((state)->{
                        state.getCommands().clear();
                        state.getCommands().addAll(commands);
                    });
            MoveCommandMessage msg = new MoveCommandMessage();
            msg.command = EnumSetConverter.convertToInt(commands);
            NetworkManager.INSTANCE.sendToServer(msg);
        }

    }
}