package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;

import static com.mcf.davidee.nbtedit.NBTEdit.SECTION_SIGN;

public class EntityRequestPacket extends AbstractPacket {

	private int entityID;

	public EntityRequestPacket() {

	}

	public EntityRequestPacket(int entityID) {
		this.entityID = entityID;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(entityID);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		entityID = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {

	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		Entity e = player.worldObj.getEntityByID(entityID);
		if (e instanceof EntityPlayer && e != player) {
			sendMessageToPlayer(player, SECTION_SIGN + "cError - You may not use NBTEdit on other Players");
			NBTEdit.log(Level.WARN, player.getName() + " tried to use NBTEdit on another player, " + e.getName());

		} else if (e != null) {
			NBTTagCompound tag = new NBTTagCompound();
			e.writeToNBT(tag);
			NBTEdit.DISPATCHER.sendTo(new EntityNBTPacket(entityID, tag), player);

		} else
			sendMessageToPlayer(player, SECTION_SIGN + "cError - Unknown EntityID #" + entityID);
	}

}
