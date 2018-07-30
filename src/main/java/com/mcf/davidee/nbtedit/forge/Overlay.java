package com.mcf.davidee.nbtedit.forge;

import com.pixelmonmod.pixelmon.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * @author happyzleaf
 * @since 06/07/2018
 */
public class Overlay extends Gui {
	private static final ResourceLocation RESOURCE = new ResourceLocation("nbtedit", "textures/gui/team_overlay.png");
	
	/**
	 * The unicode flag is set to true to copy better the style of pixelmon's overlay.
	 * @see com.pixelmonmod.pixelmon.client.gui.GuiPixelmonOverlay#onRenderGameOverlay(RenderGameOverlayEvent.Pre)
	 */
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
		int scaledWidth = event.getResolution().getScaledWidth();
		int scaledHeight = event.getResolution().getScaledHeight();
		if (ClientProxy.NBTEditTeam.getKeyCode() != Keyboard.KEY_NONE) {
			Minecraft.getMinecraft().fontRenderer.drawString(GameSettings.getKeyDisplayString(ClientProxy.NBTEditTeam.getKeyCode()), scaledWidth - 85, scaledHeight - 13, 0xFFFFFF);
			Minecraft.getMinecraft().renderEngine.bindTexture(RESOURCE);
			GuiHelper.drawImageQuad(scaledWidth - 105, scaledHeight - 30, 20, 20, 0, 0, 1, 1, this.zLevel);
		}
		Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(false);
	}
}
