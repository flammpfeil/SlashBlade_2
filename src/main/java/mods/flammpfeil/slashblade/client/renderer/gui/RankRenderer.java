package mods.flammpfeil.slashblade.client.renderer.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RankRenderer {
    private static final class SingletonHolder {
        private static final RankRenderer instance = new RankRenderer();
    }

    public static RankRenderer getInstance() {
        return RankRenderer.SingletonHolder.instance;
    }

    private RankRenderer() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    

    static ResourceLocation RankImg = new ResourceLocation(SlashBlade.modid,"textures/gui/rank.png");

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderTick(TickEvent.RenderTickEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START) return;
        if(mc.player == null) return;
        //if(!mc.isGameFocused()) return;
        if(!Minecraft.isGuiEnabled()) return;
        if(mc.currentScreen != null) {
            if(!(mc.currentScreen instanceof ChatScreen))
                return;
        }

        World world = mc.world;
        ClientPlayerEntity player= mc.player;
        long time = System.currentTimeMillis();

        renderRankHud(event.renderTickTime, player, time);
    }

    private void renderRankHud(Float partialTicks, ClientPlayerEntity player, long time) {
        Minecraft mc = Minecraft.getInstance();

        player.getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cr->{
            long now = player.world.getGameTime();

            IConcentrationRank.ConcentrationRanks rank = cr.getRank(now);

            /* debug
            rank = IConcentrationRank.ConcentrationRanks.C;
            now = cr.getLastUpdate();
            */

            if(rank == IConcentrationRank.ConcentrationRanks.NONE)
                return;

            GL11.glPushMatrix(); //1 store
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            mc.mainWindow.loadGUIRenderMatrix(Minecraft.IS_RUNNING_ON_MAC);



            int k = mc.mainWindow.getScaledWidth();
            int l = mc.mainWindow.getScaledHeight();

            //position
            GL11.glTranslatef(k * 2 / 3, l / 5, 0);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);

            mc.getTextureManager().bindTexture(RankImg);

            boolean showTextRank = false;

            long textTimeout = cr.getLastRankRise() + 20;
            long visibleTimeout = cr.getLastUpdate() + 120;

            if(now < textTimeout)
                showTextRank = true;

            if(now < visibleTimeout){
                int rankOffset = 32 * (rank.level - 1);
                int textOffset = showTextRank ? 128 : 0;

                int progress = (int)(33 * cr.getRankProgress(now));

                int progressIcon = (int)(18 * cr.getRankProgress(now));
                int progressIconInv = 17 - progressIcon;

                //GL11.glScalef(3,3,3);
                //iconFrame
                drawTexturedQuad(0, 0, 0+ textOffset + 64, rankOffset, 64, 32, -95D);
                //icon
                drawTexturedQuad(0, progressIconInv + 7, 0+ textOffset, rankOffset + progressIconInv + 7, 64, progressIcon, -90D);

                //gauge frame
                drawTexturedQuad(0 , 32, 0 ,256-16, 64, 16, -90D);
                //gause fill
                drawTexturedQuad(16, 32, 16,256-32, progress, 16, -95D);
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        });

    }

    public static void drawTexturedQuad(int x, int y, int u, int v, int width, int height, double zLevel) {
        float var7 = 0.00390625F; // 1/256 texturesize
        float var8 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x + 0, y + height, zLevel).tex((u + 0.0) * var7, (v + height) * var8).endVertex();
        wr.pos(x + width, y + height, zLevel).tex((u + width) * var7, (v + height) * var8).endVertex();
        wr.pos(x + width, y + 0, zLevel).tex((u + width) * var7, (v + 0) * var8).endVertex();
        wr.pos(x + 0, y + 0, zLevel).tex(  (u + 0) * var7,  (v + 0) * var8).endVertex();
        tessellator.draw();
    }
}
