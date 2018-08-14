package com.mcf.davidee.nbtedit.forge;

import com.pixelmonmod.pixelmon.client.gui.GuiHelper;
import com.pixelmonmod.pixelmon.client.gui.inventory.GuiCreativeInventoryExtended;
import com.pixelmonmod.pixelmon.client.gui.inventory.GuiInventoryPixelmonExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * @author happyzleaf
 * @since 06/07/2018
 */
public class GuiListener extends Gui {
	private static final ResourceLocation OVERLAY = new ResourceLocation("nbtedit", "textures/gui/team_overlay.png");
	private static final ResourceLocation INVENTORY = new ResourceLocation("nbtedit", "textures/gui/team_inventory.png");
	
	/**
	 * The unicode flag is set to true to copy better the style of pixelmon's overlay.
	 * @see com.pixelmonmod.pixelmon.client.gui.GuiPixelmonOverlay#onRenderGameOverlay(RenderGameOverlayEvent.Pre)
	 */
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(true);
			int scaledWidth = event.getResolution().getScaledWidth();
			int scaledHeight = event.getResolution().getScaledHeight();
			if (ClientProxy.NBTEditTeam.getKeyCode() != Keyboard.KEY_NONE) {
				Minecraft.getMinecraft().fontRenderer.drawString(GameSettings.getKeyDisplayString(ClientProxy.NBTEditTeam.getKeyCode()), scaledWidth - 85, scaledHeight - 13, 0xFFFFFF);
				Minecraft.getMinecraft().renderEngine.bindTexture(OVERLAY);
				GuiHelper.drawImageQuad(scaledWidth - 105, scaledHeight - 30, 20, 20, 0, 0, 1, 1, this.zLevel);
			}
			Minecraft.getMinecraft().fontRenderer.setUnicodeFlag(false);
		}
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		/*if (event.getGui() instanceof GuiInventoryPixelmonExtendedFROM_ME && !(event.getGui() instanceof GuiInventoryPixelmonExtended)) {
			event.setGui(new GuiInventoryPixelmonExtended(Minecraft.getMinecraft().player));
		} else if (event.getGui() instanceof GuiCreativeInventoryExtendedFROM_ME && !(event.getGui() instanceof GuiCreativeInventoryExtended)) {
			event.setGui(new GuiCreativeInventoryExtended(Minecraft.getMinecraft().player));
		}*/
	}
}
