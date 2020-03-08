package mods.flammpfeil.slashblade.client.renderer.model.obj;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GroupObject
{
    public String name;
    public ArrayList<Face> faces = new ArrayList<Face>();
    public int glDrawingMode;

    public GroupObject()
    {
        this("");
    }

    public GroupObject(String name)
    {
        this(name, -1);
    }

    public GroupObject(String name, int glDrawingMode)
    {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    public boolean compiled = false;
    public int displayList = -1;

    @OnlyIn(Dist.CLIENT)
    public void render()
    {
        if (faces.size() > 0)
        {
            if(compiled){
                GlStateManager.callList(this.displayList);
            }else{
                this.displayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.newList(this.displayList, GL11.GL_COMPILE_AND_EXECUTE);

                Tessellator tessellator = Tessellator.getInstance();
                tessellator.getBuffer().begin(glDrawingMode, DefaultVertexFormats.POSITION_TEX_NORMAL);
                render(tessellator);
                tessellator.draw();

                GlStateManager.endList();
                this.compiled = true;
                GLAllocation.generateDisplayLists(1);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void render(Tessellator tessellator)
    {
        if (faces.size() > 0)
        {
            for (Face face : faces)
            {
                face.addFaceForRender(tessellator);
            }
        }
    }
}