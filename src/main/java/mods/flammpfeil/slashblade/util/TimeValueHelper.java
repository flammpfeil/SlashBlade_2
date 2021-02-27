package mods.flammpfeil.slashblade.util;

public class TimeValueHelper {
    //20ticks = 1sec
    //30frames = 1sec
    
    static final double TicksToMSec = (1000.0/20.0);
    public static double getMSecFromTicks(float ticks){
        return (ticks * TicksToMSec);
    }
    static final double FramesToMSec = (1000.0/30.0);
    public static double getMSecFromFrames(float frames){
        return (frames * FramesToMSec);
    }

    static final double TicksToFrames = (30.0/20.0);
    public static double getFramesFromTicks(float ticks){
        return (ticks * TicksToFrames);
    }
    static final double MSecToFrames = (30.0/1000.0);
    public static double getFramesFromMSec(float msec){
        return (msec * MSecToFrames);
    }

    static final double MSecToTicks = (20.0/1000.0);
    public static double getTicksFromMSec(float msec){
        return (msec * MSecToTicks);
    }
    static final double FramesToTicks = (20.0/30.0);
    public static double getTicksFromFrames(float frames){
        return (frames * FramesToTicks);
    }
}
