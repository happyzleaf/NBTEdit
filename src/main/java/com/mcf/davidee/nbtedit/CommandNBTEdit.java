package com.mcf.davidee.nbtedit;

import com.mcf.davidee.nbtedit.packets.MouseOverPacket;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Level;

public class CommandNBTEdit extends CommandBase {

	@Override
	public String getCommandName() {
		return "nbtedit";
	}
	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/nbtedit OR /nbtedit <EntityId> OR /nbtedit <TileX> <TileY> <TileZ>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws NumberInvalidException, WrongUsageException {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)sender;

			if (args.length == 3) {
				int x = parseInt(args[0]);
				int y = parseInt(args[1]);
				int z = parseInt(args[2]);
				NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/nbtedit " + x + " " + y + " " + z + "\"");
				NBTEdit.NETWORK.sendTile(player, new BlockPos(x, y, z));

			} else if (args.length == 1) {
				int entityID = (args[0].equalsIgnoreCase("me")) ? player.getEntityId() : parseInt(args[0], 0);
				NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/nbtedit " + entityID +  "\"");
				NBTEdit.NETWORK.sendEntity(player, entityID);

			} else if (args.length == 0) {
				NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/nbtedit\"");
				NBTEdit.NETWORK.INSTANCE.sendTo(new MouseOverPacket(), player);

			} else {
				String s = "";
				for (int i =0; i < args.length; ++i) {
					s += args[i];
					if (i != args.length - 1)
						s += " ";
				}
				NBTEdit.log(Level.TRACE, sender.getName() + " issued invalid command \"/nbtedit " + s + "\"");
				throw new WrongUsageException("Pass 0, 1, or 3 integers -- ex. /nbtedit");
			}
		}
	}

	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return sender instanceof EntityPlayer && NBTEdit.proxy.checkPermission((EntityPlayer) sender);
	}

}
