package mods.flammpfeil.slashblade.util;

import com.google.gson.*;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

public class AdvancementBuilder {
    static public String getAdvancementJsonStr(ItemStack inItemStack){

        ItemStack iconItem = inItemStack.copy();

        JsonObject ret = new JsonObject();

        final String recipeid = "[recipeid]";
        ret.addProperty("置換用ヒントResourceLocation",recipeid);

        iconItem.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state->{
            JsonObject display = new JsonObject();
            ret.add("display", display);
            {
                JsonObject title = new JsonObject();
                display.add("title", title);
                {
                    title.addProperty("translate", state.getTranslationKey());
                }

                JsonObject description = new JsonObject();
                display.add("description", description);
                {
                    description.addProperty("translate", state.getTranslationKey()+".desc");
                }

                JsonObject icon = new JsonObject();
                display.add("icon", icon);
                {
                    icon.addProperty("item", ForgeRegistries.ITEMS.getKey(iconItem.getItem()).toString());

                    iconItem.getOrCreateTag().putString("Crafting", recipeid);

                    CompoundTag iconNbt = iconItem.save(new CompoundTag());
                    icon.addProperty("nbt", "{SlashBladeIcon:" + iconNbt.toString() + "}");
                }

                display.addProperty("frame", "task");
            }

            ret.addProperty("parent", "slashblade:blade/ex/");

            JsonObject criteria = new JsonObject();
            ret.add("criteria", criteria);
            {
                JsonObject crafting = new JsonObject();
                criteria.add("crafting", crafting);
                {
                    crafting.addProperty("trigger", "inventory_changed");

                    JsonObject conditions = new JsonObject();
                    crafting.add("conditions", conditions);
                    {
                        JsonArray items = new JsonArray();
                        conditions.add("items", items);
                        {
                            JsonObject item = new JsonObject();
                            item.addProperty("item", ForgeRegistries.ITEMS.getKey(iconItem.getItem()).toString());
                            item.addProperty("nbt", "{ShareTag:{translationKey:\"" + state.getTranslationKey() + "\",isBroken:\"false\"}}");
                            items.add(item);
                        }
                    }
                }
            }
        });

        Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return GSON.toJson(ret);
    }
}
