package mods.flammpfeil.slashblade.optional.playerAnim;

import com.google.common.collect.Maps;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdMotionPlayerGL2;
import jp.nyatla.nymmd.MmdPmdModelMc;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import jp.nyatla.nymmd.core.PmdBone;
import jp.nyatla.nymmd.types.MmdVector3;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Math;
import java.util.Map;
import java.util.logging.Logger;

public class VmdAnimation implements IAnimation {
     static final LazyOptional<MmdPmdModelMc> alex =
            LazyOptional.of(() -> {
                try {
                    return new MmdPmdModelMc(new ResourceLocation(SlashBlade.modid, "model/pa/alex.pmd"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MmdException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });

    static final LazyOptional<MmdMotionPlayerGL2> motionPlayer =
            LazyOptional.of(() -> {
                MmdMotionPlayerGL2 mmp = new MmdMotionPlayerGL2();;

                alex.ifPresent(pmd -> {
                    try {
                        mmp.setPmd(pmd);
                    } catch (MmdException e) {
                        e.printStackTrace();
                    }
                });

                return mmp;
            });

    int currentTick;

    final ResourceLocation loc;
    double start;
    double end;
    double span;
    boolean loop;

    private boolean isRunning = true;

    public VmdAnimation(ResourceLocation loc, double start, double end, boolean loop){
        this.loc = loc;
        this.start = start;
        this.end = end;

        this.span = TimeValueHelper.getTicksFromFrames((float)Math.abs(end - start));;

        this.loop = loop;

        currentTick = 0;
    }

    @Override
    public void tick() {
        if (this.isRunning) {
            this.currentTick++;

            double endTicks = span;
            this.loop = false;
            if (this.loop && endTicks < this.currentTick) {
                this.currentTick = 0;
            }

            if (endTicks <= currentTick) {
                this.stop();
            }
        }
    }

    public void play(){
        this.currentTick = 0;
        this.isRunning = true;
    }

    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean isActive() {
        return this.isRunning;
    }
    Map<String,String> nameMap = initNamemap();

    private Map<String, String> initNamemap() {
        Map<String,String> map = Maps.newHashMap();
        map.put("leftArm","left arm");
        map.put("rightArm","right arm");
        map.put("leftLeg","left leg");
        map.put("rightLeg","right leg");
        return map;
    }


    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        this.setupAnim(tickDelta);

        double motionScale = 1.5 / 12.0;
        double modelScaleBase = 0.0078125F; //0.5^7
        float scale = 1.5f;

        if(!motionPlayer.isPresent()) return value0;
        MmdMotionPlayerGL2 mmp = motionPlayer.orElse(null);

        String boneName = modelName;
        if(nameMap.containsKey(modelName)){
            boneName = nameMap.get(modelName);
        }

        if(modelName.equals("head") && type == TransformType.ROTATION) return value0;
        //if(modelName.equals("body")) boneName = "torso";


        PmdBone bone = mmp.getBoneByName(boneName);

        if(bone != null){
            switch (type){
                case POSITION -> {
                    MmdVector3 tmp = bone.m_vec3Position;
                    return new Vec3f(tmp.x,-tmp.y,tmp.z).scale(1.5f).add(value0);
                }
                case ROTATION -> {
                    Quaternionf qt = new Quaternionf(bone.m_vec4Rotate.x,bone.m_vec4Rotate.y,bone.m_vec4Rotate.z,bone.m_vec4Rotate.w);
                    Vector3f tmp = new Vector3f();
                    qt.getEulerAnglesZXY(tmp);
                    return new Vec3f(-tmp.x,tmp.y,-tmp.z).add(value0);
                }
            }
        }
        /**/

        /*
        int idx = mmp.getBoneIndexByName(boneName);
        if (0 <= idx) {
            float[] buf = new float[16];
            mmp._skinning_mat[idx].getValue(buf);

            Matrix4f mat = VectorHelper.matrix4fFromArray(buf);
            mat = (new Matrix4f()).scale(1, -1, 1).mul(mat).scale(1,-1,1).scale((float)scale);
            //mat.transpose();

            switch (type){
                case POSITION -> {
                    Vector3f tmp = new Vector3f();
                    mat.getTranslation(tmp);
                    MmdVector3 vec = bone._pmd_bone_position;

                    return new Vec3f(tmp.x,tmp.y,tmp.z).add(value0);
                }
                case ROTATION -> {

                    Quaternionf qt = new Quaternionf(bone.m_vec4Rotate.x,bone.m_vec4Rotate.y,bone.m_vec4Rotate.z,bone.m_vec4Rotate.w);
                    Vector3f tmp = new Vector3f();
                    qt.getEulerAnglesXYZ(tmp);

                    return new Vec3f(tmp.x,tmp.y,tmp.z);

                    //Vector3f tmp = new Vector3f();
                    //mat = mat;
                    //mat.getEulerAnglesZYX(tmp);
                    //return new Vec3f(tmp.x,tmp.y,tmp.z);

                }
            }
        }
        /**/

        return value0;
    }

    @Override
    public void setupAnim(float tickDelta) {
        if(!motionPlayer.isPresent()) return;

        MmdMotionPlayerGL2 mmp = motionPlayer.orElse(null);

        double eofTime = -1;
        MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(loc);
        try {
            mmp.setVmd(motion);
            eofTime = TimeValueHelper.getMSecFromFrames(motion.getMaxFrame());
        } catch (Exception e) {
            e.printStackTrace();
        }

        double time = TimeValueHelper.getMSecFromTicks(currentTick + tickDelta);
        time = Math.min(eofTime,time);
        time = TimeValueHelper.getMSecFromFrames((float)start) + time;

        try {
            mmp.updateMotion((float)time);
        } catch (MmdException e) {
            e.printStackTrace();
        }
    }
}
