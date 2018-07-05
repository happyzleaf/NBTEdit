package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

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
	public void fromBytes(ByteBuf buf) {
		this.slot = buf.readInt();
		this.tag = NBTHelper.readNbtFromBuffer(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.slot);
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public static class Handler implements IMessageHandler<TeamNBTPacket, IMessage> {

		@Override
		public IMessage onMessage(final TeamNBTPacket packet, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(() -> {
					PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorage(player).get();
					NBTTagCompound pokemon = storage.partyPokemon[packet.slot];
					if (pokemon != null && NBTEdit.proxy.checkPermission(player) && pokemon.getString(NbtKeys.NAME).equals(packet.tag.getString(NbtKeys.NAME))) {
						storage.partyPokemon[packet.slot] = packet.tag;
						storage.updateClient(packet.tag, EnumUpdateType.values());
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
