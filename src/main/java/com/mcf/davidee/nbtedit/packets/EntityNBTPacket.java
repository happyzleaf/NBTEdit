package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
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

public class EntityNBTPacket implements IMessage {
	/**
	 * The id of the entity being edited.
	 */
	protected int entityID;
	/**
	 * The nbt data of the entity.
	 */
	protected NBTTagCompound tag;

	/**
	 * Required default constructor.
	 */
	public EntityNBTPacket() {
	}

	public EntityNBTPacket(int entityID, NBTTagCompound tag) {
		this.entityID = entityID;
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityID = buf.readInt();
		this.tag = NBTHelper.readNbtFromBuffer(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityID);
		NBTHelper.writeToBuffer(this.tag, buf);
	}

	public static class Handler implements IMessageHandler<EntityNBTPacket, IMessage> {

		@Override
		public IMessage onMessage(final EntityNBTPacket packet, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				final EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						Entity entity = player.getServerWorld().getEntityByID(packet.entityID);
						if (entity != null && NBTEdit.proxy.checkPermission(player)) {
							try {
								GameType preGameType = null;
								if (entity instanceof EntityPlayerMP)
									preGameType = ((EntityPlayerMP) entity).interactionManager.getGameType();
								entity.readFromNBT(packet.tag);
								NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Entity ID #" + packet.entityID);
								NBTEdit.logTag(packet.tag);
								if (entity instanceof EntityPlayerMP) {//Update player info
									// This is fairly hacky. Consider swapping to an event driven system, where classes can register to
									// receive entity edit events and provide feedback/send packets as necessary.
									EntityPlayerMP targetPlayer = (EntityPlayerMP) entity;
									targetPlayer.sendContainerToPlayer(targetPlayer.inventoryContainer);
									GameType type = targetPlayer.interactionManager.getGameType();
									if (preGameType != type) {
										targetPlayer.setGameType(type);
									}
									targetPlayer.connection.sendPacket(new SPacketUpdateHealth(targetPlayer.getHealth(),
											targetPlayer.getFoodStats().getFoodLevel(), targetPlayer.getFoodStats().getSaturationLevel()));
									targetPlayer.connection.sendPacket(new SPacketSetExperience(targetPlayer.experience,
											targetPlayer.experienceTotal, targetPlayer.experienceLevel));
									targetPlayer.sendPlayerAbilities();
									targetPlayer.setPositionAndUpdate(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ);
								}
								NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
							} catch (Throwable t) {
								NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", TextFormatting.RED);
								NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
								NBTEdit.logTag(packet.tag);
								NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
							}
						} else {
							NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", TextFormatting.RED);
						}
					}
				});
			} else {
				NBTEdit.proxy.openEditGUI(packet.entityID, packet.tag);
			}
			return null;
		}
	}
}
