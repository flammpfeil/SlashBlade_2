package mods.flammpfeil.slashblade.event;

import com.google.gson.*;
import mods.flammpfeil.slashblade.capability.inputstate.IInputState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.network.MoveCommandMessage;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;

public class MoveInputHandler {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final String LAST_CHANGE_TIME = "SB_LAST_CHANGE_TIME";

    public static boolean checkFlag(int data, int flags){
        return (data & flags) == flags;
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    static public void onPlayerPostTick(TickEvent.PlayerTickEvent event){
        if(event.phase != TickEvent.Phase.END) return;

        if(!(event.player instanceof LocalPlayer)) return;

        LocalPlayer player = (LocalPlayer)event.player;

        EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);

        if(player.input.up)
            commands.add(InputCommand.FORWARD);
        if(player.input.down)
            commands.add(InputCommand.BACK);
        if(player.input.left)
            commands.add(InputCommand.LEFT);
        if(player.input.right)
            commands.add(InputCommand.RIGHT);

        if(player.input.shiftKeyDown)
            commands.add(InputCommand.SNEAK);

        if(Minecraft.getInstance().options.keySprint.isDown())
            commands.add(InputCommand.SPRINT);

        if(Minecraft.getInstance().options.keyJump.isDown()){
            commands.add(InputCommand.JUMP);
        }

/*
        if((player.movementInput.sneak && SlashBlade.SneakForceLockOn)
                || CoreProxyClient.lockon.isKeyDown())
            message.activeTag += MoveCommandMessage.SNEAK;

        
        if(CoreProxyClient.camera.isKeyDown())
            message.activeTag += MoveCommandMessage.CAMERA;

        if(CoreProxyClient.styleaction.isKeyDown())
            message.activeTag += MoveCommandMessage.STYLE;
*/

        if(Minecraft.getInstance().options.keyUse.isDown())
            commands.add(InputCommand.R_DOWN);
        if(Minecraft.getInstance().options.keyAttack.isDown())
            commands.add(InputCommand.L_DOWN);

        if(Minecraft.getInstance().options.keyPickItem.isDown())
            commands.add(InputCommand.M_DOWN);


        if(Minecraft.getInstance().options.keySaveHotbarActivator.isDown())
            commands.add(InputCommand.SAVE_TOOLBAR);

        EnumSet<InputCommand> old = player.getCapability(INPUT_STATE)
                .map((state)->state.getCommands())
                .orElseGet(()->EnumSet.noneOf(InputCommand.class));

        Level worldIn = player.getCommandSenderWorld();

        /*
        if(player.movementInput.forwardKeyDown &&  (0 < (player.getPersistentData().getInt(KEY) & MoveCommandMessage.SNEAK)))
            player.getPersistentData().putLong("SB.MCS.F",currentTime);
        if(player.movementInput.backKeyDown &&  (0 < (player.getPersistentData().getInt(KEY) & MoveCommandMessage.SNEAK)))
            player.getPersistentData().putLong("SB.MCS.B",currentTime);
        */

        boolean doCopy = player.isCreative();

        if(doCopy && old.contains(InputCommand.SAVE_TOOLBAR) && !commands.contains(InputCommand.SAVE_TOOLBAR)){
            ItemStack stack = player.getMainHandItem();

            JsonObject ret = new JsonObject();

            String str = "";
            if(KeyModifier.SHIFT.isActive(KeyConflictContext.UNIVERSAL)){
                str = AdvancementBuilder.getAdvancementJsonStr(stack);
            }else{

                ret.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                if (stack.getCount() != 1)
                    ret.addProperty("count", stack.getCount());

                CompoundTag tag = new CompoundTag();
                stack.save(tag);

                CompoundTag nbt = stack.getOrCreateTag().copy();
                if(tag.contains("ForgeCaps"))
                    nbt.put("ForgeCaps", tag.get("ForgeCaps"));

                if(KeyModifier.ALT.isActive(KeyConflictContext.UNIVERSAL)){
                    //add anvilcrafting recipe template
                    AnvilCraftingRecipe acr = new AnvilCraftingRecipe();

                    ItemStack result = player.getOffhandItem();
                    acr.setResult(result);

                    nbt.put("RequiredBlade",acr.writeNBT());
                }

                if(KeyModifier.CONTROL.isActive(KeyConflictContext.UNIVERSAL)){
                    //add anvilcrafting recipe template
                    ItemStack result = player.getMainHandItem();

                    CompoundTag iconNbt = result.save(new CompoundTag());

                    ret.addProperty("iconStr_nbt",iconNbt.toString());


                    JsonObject criteriaitem = new JsonObject();
                    criteriaitem.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());

                    CompoundTag checktarget = new CompoundTag();
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
                str = GSON.toJson(ret);
            }

            Minecraft.getInstance().keyboardHandler.setClipboard(str);
        }

        long currentTime = worldIn.getGameTime();
        boolean doSend = !old.equals(commands);

        if(doSend){
            player.getCapability(INPUT_STATE)
                    .ifPresent((state)->{
                        commands.forEach(c->{
                            if(!old.contains(c))
                                state.getLastPressTimes().put(c, currentTime);
                        });

                        state.getCommands().clear();
                        state.getCommands().addAll(commands);
                    });
            MoveCommandMessage msg = new MoveCommandMessage();
            msg.command = EnumSetConverter.convertToInt(commands);
            NetworkManager.INSTANCE.sendToServer(msg);
        }
    }
}