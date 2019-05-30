package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

/**
 * Created by Jay113355 on 6/28/2016.
 * Updated by happyz on 30/05/2019.
 */
public class PacketHandler {
	public SimpleNetworkWrapper INSTANCE;
	private static int ID = 0;
	
	public void initialize() {
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(NBTEdit.MODID);
		registerPackets();
	}
	
	public void registerPackets() {
		INSTANCE.registerMessage(TileRequestPacket.Handler.class, TileRequestPacket.class, ID++, Side.SERVER);
		INSTANCE.registerMessage(TileNBTPacket.Handler.class, TileNBTPacket.class, ID++, Side.CLIENT);
		INSTANCE.registerMessage(TileNBTPacket.Handler.class, TileNBTPacket.class, ID++, Side.SERVER);
		INSTANCE.registerMessage(EntityRequestPacket.Handler.class, EntityRequestPacket.class, ID++, Side.SERVER);
		INSTANCE.registerMessage(EntityNBTPacket.Handler.class, EntityNBTPacket.class, ID++, Side.CLIENT);
		INSTANCE.registerMessage(EntityNBTPacket.Handler.class, EntityNBTPacket.class, ID++, Side.SERVER);
		INSTANCE.registerMessage(MouseOverPacket.Handler.class, MouseOverPacket.class, ID++, Side.CLIENT);
		INSTANCE.registerMessage(EntityRequestTeamPacket.Handler.class, EntityRequestTeamPacket.class, ID++, Side.SERVER);
		INSTANCE.registerMessage(TeamNBTPacket.Handler.class, TeamNBTPacket.class, ID++, Side.CLIENT);
		INSTANCE.registerMessage(TeamNBTPacket.Handler.class, TeamNBTPacket.class, ID++, Side.SERVER);
	}
	
	/**
	 * Sends a TileEntity's nbt data to the player for editing.
	 *
	 * @param player The player to send the TileEntity to.
	 * @param pos    The block containing the TileEntity.
	 */
	public void sendTile(final EntityPlayerMP player, final BlockPos pos) {
		if (NBTEdit.proxy.checkPermission(player)) {
			player.getServerWorld().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileEntity te = player.getServerWorld().getTileEntity(pos);
					if (te != null) {
						NBTTagCompound tag = new NBTTagCompound();
						te.writeToNBT(tag);
						INSTANCE.sendTo(new TileNBTPacket(pos, tag), player);
					} else {
						NBTEdit.proxy.sendMessage(player, "Error - There is no TileEntity at "
								+ pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), TextFormatting.RED);
					}
				}
			});
		}
	}
	
	/**
	 * Sends a Entity's nbt data to the player for editing.
	 *
	 * @param player   The player to send the Entity data to.
	 * @param entityId The id of the Entity.
	 */
	public void sendEntity(final EntityPlayerMP player, final int entityId) {
		if (NBTEdit.proxy.checkPermission(player)) {
			player.getServerWorld().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Entity entity = player.getServerWorld().getEntityByID(entityId);
					if (entity instanceof EntityPlayer && entity != player && !NBTEdit.editOtherPlayers) {
						NBTEdit.proxy.sendMessage(player, "Error - You may not use NBTEdit on other Players", TextFormatting.RED);
						NBTEdit.log(Level.WARN, player.getName() + " tried to use NBTEdit on another player, " + entity.getName());
						return;
					}
					if (entity != null) {
						NBTTagCompound tag = new NBTTagCompound();
						entity.writeToNBT(tag);
						INSTANCE.sendTo(new EntityNBTPacket(entityId, tag), player);
					} else {
						NBTEdit.proxy.sendMessage(player, "\"Error - Unknown EntityID #" + entityId, TextFormatting.RED);
					}
				}
			});
		}
	}
	
	public void sendTeamMember(EntityPlayerMP player, int slot) {
		if (NBTEdit.proxy.checkPermission(player)) {
			player.getServerWorld().addScheduledTask(() -> {
				Pokemon pokemon = Pixelmon.storageManager.getParty(player).get(slot);
				if (pokemon == null) {
					NBTEdit.proxy.sendMessage(player, "\"Error - Unknown Team Member #" + (slot + 1), TextFormatting.RED);
				} else {
					pokemon.retrieve();
					INSTANCE.sendTo(new TeamNBTPacket(slot, pokemon.writeToNBT(new NBTTagCompound())), player);
				}
			});
		}
	}
}
