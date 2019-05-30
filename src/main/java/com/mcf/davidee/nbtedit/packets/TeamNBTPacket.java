package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Updated by happyz on 30/05/2019.
 */
public class TeamNBTPacket implements IMessage {
	protected int slot;
	protected NBTTagCompound tag;
	
	public TeamNBTPacket() {
	}
	
	public TeamNBTPacket(int slot, NBTTagCompound tag) {
		this.slot = slot;
		this.tag = tag;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(slot);
		NBTHelper.writeToBuffer(tag, buf);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.slot = buf.readInt();
		this.tag = NBTHelper.readNbtFromBuffer(buf);
	}

	public static class Handler implements IMessageHandler<TeamNBTPacket, IMessage> {
		@Override
		public IMessage onMessage(final TeamNBTPacket packet, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(() -> {
					PartyStorage party = Pixelmon.storageManager.getParty(player);
					Pokemon pokemon = party.get(packet.slot);
					if (pokemon != null && NBTEdit.proxy.checkPermission(player) && pokemon.getSpecies().name.equals(packet.tag.getString(NbtKeys.NAME))) {
						pokemon.readFromNBT(packet.tag);
					} else {
						NBTEdit.proxy.sendMessage(player, "Save Failed - The slot " + packet.slot + " now holds another pokémon.", TextFormatting.RED);
					}
				});
			} else {
				NBTEdit.proxy.openEditGUI(packet.tag, packet.slot);
			}
			return null;
		}
	}
}
