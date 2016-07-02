package com.mcf.davidee.nbtedit.forge;

import com.mcf.davidee.nbtedit.NBTEdit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.io.File;

public class CommonProxy {
	public void registerInformation() {
		
	}

	public File getMinecraftDirectory() {
		return new File(".");
	}
	
	public void openEditGUI(int entityID, NBTTagCompound tag) {
		
	}
	
	public void openEditGUI(BlockPos pos, NBTTagCompound tag) {
		
	}

	public void sendMessage(EntityPlayer player, String message, EnumChatFormatting color) {
		if (player != null) {
			IChatComponent component = new ChatComponentText(message);
			component.getChatStyle().setColor(color);
			player.addChatMessage(component);
		}
	}

	public boolean checkPermission(EntityPlayer player) {
		if (NBTEdit.opOnly ? player.canCommandSenderUseCommand(4, NBTEdit.MODID) : player.capabilities.isCreativeMode) {
			return true;
		}
		return false;
	}
}
