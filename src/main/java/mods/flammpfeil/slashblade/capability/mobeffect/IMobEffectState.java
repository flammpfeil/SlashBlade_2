package mods.flammpfeil.slashblade.capability.mobeffect;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public interface IMobEffectState {
    default void setManagedStun(long now, long duration){
        if(duration <= 0)
            return;

        long solvedDuration = Math.min(duration, this.getStunLimit());
        long timeout = now + solvedDuration;
        if(this.getStunTimeOut() < timeout)
            this.setStunTimeOut(timeout);
    }
    void setStunTimeOut(long timeout);
    default void clearStunTimeOut(){
        setStunTimeOut(-1);
    }

    long getStunTimeOut();

    default boolean isStun(long now) {
        return isStun(now,false);
    }
    default boolean isStun(long now, boolean isVirtual){

        long timeout = getStunTimeOut();

        //not stun
        if(timeout <= 0) return false;


        //timeout
        timeout = timeout - now;
        if(timeout <= 0 || getStunLimit() < timeout){
            if(!isVirtual)
                clearStunTimeOut();
            return false;
        }

        //it is in Effect
        return true;
    }


    default void setManagedFreeze(long now, long duration){
        if(duration <= 0)
            return;

        long solvedDuration = Math.min(duration, this.getFreezeLimit());
        long timeout = now + solvedDuration;
        if(this.getFreezeTimeOut() < timeout)
            this.setFreezeTimeOut(timeout);
    }
    void setFreezeTimeOut(long timeout);
    default void clearFreezeTimeOut(){
        setFreezeTimeOut(-1);
    }

    long getFreezeTimeOut();

    default boolean isFreeze(long now) {
        return isFreeze(now,false);
    }
    default boolean isFreeze(long now, boolean isVirtual){

        long timeout = getFreezeTimeOut();

        //not Freeze
        if(timeout <= 0) return false;


        //timeout
        timeout = timeout - now;
        if(timeout <= 0 || getFreezeLimit() < timeout){
            if(!isVirtual)
                clearFreezeTimeOut();
            return false;
        }

        //it is in Effect
        return true;
    }

    int getStunLimit();
    void setStunLimit(int limit);

    int getFreezeLimit();
    void setFreezeLimit(int limit);
}
