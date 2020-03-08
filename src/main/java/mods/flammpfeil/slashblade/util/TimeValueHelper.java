package mods.flammpfeil.slashblade.util;

public class TimeValueHelper {
    //20ticks = 1sec
    //30frames = 1sec
    
    static final double TicksToMSec = (1000.0/20.0);
    public static float getMSecFromTicks(float ticks){
        return (float)(ticks * TicksToMSec);
    }
    static final double FramesToMSec = (1000.0/30.0);
    public static float getMSecFromFrames(float frames){
        return (float)(frames * FramesToMSec);
    }

    static final double TicksToFrames = (20.0/30.0);
    public static float getFramesFromTicks(float ticks){
        return (float)(ticks * TicksToFrames);
    }
    static final double MSecToFrames = (30.0/1000.0);
    public static float getFramesFromMSec(float msec){
        return (float)(msec * MSecToFrames);
    }

    static final double MSecToTicks = (20.0/1000.0);
    public static float getTicksFromMSec(float msec){
        return (float)(msec * MSecToTicks);
    }
    static final double FramesToTicks = (30.0/20.0);
    public static float getTicksFromFrames(float frames){
        return (float)(frames * FramesToTicks);
    }
}
