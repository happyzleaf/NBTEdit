package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Level;

/**
 * @author happyzleaf
 * @since 05/07/2018
 */
public class EntityRequestTeamPacket implements IMessage {
	private int slot;
	
	public EntityRequestTeamPacket() {
	}
	
	public EntityRequestTeamPacket(int slot) {
		this.slot = slot;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.slot = buf.readInt();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.slot);
	}
	
	public static class Handler implements IMessageHandler<EntityRequestTeamPacket, IMessage> {
		@Override
		public IMessage onMessage(EntityRequestTeamPacket packet, MessageContext ctx) {
			System.out.println("received packet " + packet.slot);
			EntityPlayerMP player = ctx.getServerHandler().player;
			NBTEdit.log(Level.TRACE, player.getName() + " requested party member at slot " + packet.slot);
			NBTEdit.NETWORK.sendTeamMember(player, packet.slot);
			return null;
		}
	}
}
