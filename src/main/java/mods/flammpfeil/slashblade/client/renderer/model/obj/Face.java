package mods.flammpfeil.slashblade.client.renderer.model.obj;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.function.Supplier;

public class Face
{
    public static boolean isSmoothShade = true;
    public static int lightmap = 15;
    public static void setLightMap(int value){
        lightmap = value;
    }
    public static void resetLightMap(){
        lightmap = 15;
    }

    public static Color col;
    public static void setCol(Color col) {
        Face.col = col;
    }
    public static void resetCol() {
        Face.col = Color.white;
    }

    private static final LazyValue<Matrix4f> defaultTransform = new LazyValue(()->{Matrix4f m = new Matrix4f(); m.setIdentity(); return m;});

    public static MatrixStack matrix = null;
    public static void setMatrix(MatrixStack ms){
        matrix = ms;
    }
    public static void resetMatrix(){
        matrix = null;
    }

    public Vertex[] vertices;
    public Vertex[] vertexNormals;
    public Vertex faceNormal;
    public TextureCoordinate[] textureCoordinates;

    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(IVertexBuilder tessellator)
    {
        addFaceForRender(tessellator, 0.0005F);
    }

    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(IVertexBuilder tessellator, float textureOffset)
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

        IVertexBuilder wr = tessellator;


        Matrix4f transform;
        if(matrix != null){
            MatrixStack.Entry me = matrix.getLast();
            transform = me.getMatrix();
        }else{
            transform = defaultTransform.getValue();
        }

        for (int i = 0; i < vertices.length; ++i)
        {
            Vector4f vector4f = new Vector4f(vertices[i].x, vertices[i].y, vertices[i].z, 1.0F);
            vector4f.transform(transform);
            wr.pos(vector4f.getX(),vector4f.getY(),vector4f.getZ());

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

            wr.lightmap(lightmap);

            wr.color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());

            Vector3f vector3f;
            if(isSmoothShade && vertexNormals != null) {

                Vertex normal = vertexNormals[i];

                Vector3d nol = new Vector3d(normal.x, normal.y, normal.z);
                //nol.rotatePitch(180);
                vector3f = new Vector3f((float)nol.x, (float)nol.y, (float)nol.z);
            }else{
                vector3f = new Vector3f(faceNormal.x, faceNormal.y, faceNormal.z);
            }
            vector3f.transform(new Matrix3f(transform));;
            vector3f.normalize();
            wr.normal(vector3f.getX(), vector3f.getY(), vector3f.getZ());

            wr.endVertex();
        }
    }

    public Vertex calculateFaceNormal()
    {
        Vector3d v1 = new Vector3d(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vector3d v2 = new Vector3d(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vector3d normalVector = null;

        normalVector = v1.crossProduct(v2).normalize();

        return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
    }
}