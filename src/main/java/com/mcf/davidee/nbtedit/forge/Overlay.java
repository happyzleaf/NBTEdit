package com.mcf.davidee.nbtedit.forge;

import com.pixelmonmod.pixelmon.client.gui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * @author happyzleaf
 * @since 06/07/2018
 */
public class Overlay extends Gui {
	private static final ResourceLocation RESOURCE = new ResourceLocation("nbtedit", "textures/gui/team_overlay.png");
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		int scaledWidth = event.getResolution().getScaledWidth();
		int scaledHeight = event.getResolution().getScaledHeight();
		if (ClientProxy.NBTEditTeam.getKeyCode() != Keyboard.KEY_NONE) {
			Minecraft.getMinecraft().fontRenderer.drawString(GameSettings.getKeyDisplayString(ClientProxy.NBTEditTeam.getKeyCode()), scaledWidth - 85, scaledHeight - 13, 0xFFFFFF);
			Minecraft.getMinecraft().renderEngine.bindTexture(RESOURCE);
			GuiHelper.drawImageQuad(scaledWidth - 105, scaledHeight - 30, 20, 20, 0, 0, 1, 1, this.zLevel);
		}
	}
}
