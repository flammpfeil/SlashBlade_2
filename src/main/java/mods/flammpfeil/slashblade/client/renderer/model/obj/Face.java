package mods.flammpfeil.slashblade.client.renderer.model.obj;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Face
{
    public static boolean isSmoothShade = true;

    public Vertex[] vertices;
    public Vertex[] vertexNormals;
    public Vertex faceNormal;
    public TextureCoordinate[] textureCoordinates;

    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(Tessellator tessellator)
    {
        addFaceForRender(tessellator, 0.0005F);
    }

    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(Tessellator tessellator, float textureOffset)
    {
        if (faceNormal == null)
        {
            faceNormal = this.calculateFaceNormal();
        }

        float averageU = 0F;
        float averageV = 0F;

        if ((textureCoordinates != null) && (textureCoordinates.length > 0))
        {
            for (int i = 0; i < textureCoordinates.length; ++i)
            {
                averageU += textureCoordinates[i].u;
                averageV += textureCoordinates[i].v;
            }

            averageU = averageU / textureCoordinates.length;
            averageV = averageV / textureCoordinates.length;
        }

        float offsetU, offsetV;

        BufferBuilder wr = tessellator.getBuffer();

        for (int i = 0; i < vertices.length; ++i)
        {
            wr.pos(vertices[i].x, vertices[i].y, vertices[i].z);

            if ((textureCoordinates != null) && (textureCoordinates.length > 0))
            {
                offsetU = textureOffset;
                offsetV = textureOffset;

                if (textureCoordinates[i].u > averageU)
                {
                    offsetU = -offsetU;
                }
                if (textureCoordinates[i].v > averageV)
                {
                    offsetV = -offsetV;
                }


                wr.tex(textureCoordinates[i].u + offsetU, textureCoordinates[i].v + offsetV);
            }else{
                wr.tex(0, 0);
            }

            if(isSmoothShade && vertexNormals != null) {
                Vertex normal = vertexNormals[i];
                Vec3d nol = new Vec3d(normal.x, normal.y, normal.z);
                nol.rotatePitch(180);
                wr.normal((float)nol.x, (float)nol.y, (float)nol.z);
            }else{

                wr.normal(faceNormal.x, faceNormal.y, faceNormal.z);
            }

            wr.endVertex();
        }
    }

    public Vertex calculateFaceNormal()
    {
        Vec3d v1 = new Vec3d(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vec3d v2 = new Vec3d(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vec3d normalVector = null;

        normalVector = v1.crossProduct(v2).normalize();

        return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
    }
}