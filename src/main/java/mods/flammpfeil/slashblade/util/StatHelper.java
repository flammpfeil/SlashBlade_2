package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;

public class StatHelper {

    static final long MAX_VALUE = Integer.MAX_VALUE;

    static public int increase(ServerPlayerEntity player, ResourceLocation loc, int amount){
        Stat<?> stat = Stats.CUSTOM.get(loc);
        ServerStatisticsManager stats = player.getStats();

        int oldValue = stats.getValue(stat);
        int newValue = (int)Math.min((long)oldValue + (long)amount, MAX_VALUE);
        if(oldValue == newValue){
            newValue--;
        }

        stats.setValue(player, stat, newValue);

        return newValue;
    }
}
