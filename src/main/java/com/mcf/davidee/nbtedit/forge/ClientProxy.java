package com.mcf.davidee.nbtedit.forge;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.gui.GuiEditNBTTree;
import com.mcf.davidee.nbtedit.nbt.SaveStates;
import com.mcf.davidee.nbtedit.packets.EntityRequestPacket;
import com.mcf.davidee.nbtedit.packets.EntityRequestTeamPacket;
import com.mcf.davidee.nbtedit.packets.TileRequestPacket;
import com.pixelmonmod.pixelmon.client.ServerStorageDisplay;
import com.pixelmonmod.pixelmon.client.gui.GuiPixelmonOverlay;
import com.pixelmonmod.pixelmon.comm.PixelmonData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.Arrays;

public class ClientProxy extends CommonProxy {

	public static KeyBinding NBTEditKey;

	public static KeyBinding NBTEditTeam;
	
	@Override
	public void registerInformation() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new GuiListener());
		SaveStates save = NBTEdit.getSaveStates();
		save.load();
		save.save();
		NBTEditKey = new KeyBinding("Select Entity", Keyboard.KEY_NONE, "key.categories.nbtedit");
		ClientRegistry.registerKeyBinding(NBTEditKey);
		NBTEditTeam = new KeyBinding("Select Party Member", Keyboard.KEY_N, "key.categories.nbtedit");
		ClientRegistry.registerKeyBinding(NBTEditTeam);
	}

	@Override
	public File getMinecraftDirectory() {
		return FMLClientHandler.instance().getClient().mcDataDir;
	}

	@Override
	public void openEditGUI(final int entityID, final NBTTagCompound tag) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(entityID, tag));
			}
		});
	}

	@Override
	public void openEditGUI(final BlockPos pos, final NBTTagCompound tag) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(pos, tag));
			}
		});
	}
	
	@Override
	public void openEditGUI(NBTTagCompound tag, int slot) {
		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(tag, slot)));
	}

	@Override
	public void sendMessage(EntityPlayer player, String message, TextFormatting color) {
		ITextComponent component = new TextComponentString(message);
		component.getStyle().setColor(color);
		Minecraft.getMinecraft().player.sendMessage(component);
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event) {
		GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
		if (curScreen instanceof GuiEditNBTTree) {
			GuiEditNBTTree screen = (GuiEditNBTTree) curScreen;
			if (screen.poke) return;
			Entity e = screen.getEntity();

			if (e != null && e.isEntityAlive())
				drawBoundingBox(event.getContext(), event.getPartialTicks(), e.getEntityBoundingBox());
			else if (screen.isTileEntity()) {
				int x = screen.getBlockX();
				int y = screen.y;
				int z = screen.z;
				World world = Minecraft.getMinecraft().world;
				BlockPos pos = new BlockPos(x, y, z);
				IBlockState state = world.getBlockState(pos);
				Block block = world.getBlockState(pos).getBlock();
				if (block != null) {
					//block.setBlockBoundsBasedOnState(world, pos);
					drawBoundingBox(event.getContext(), event.getPartialTicks(), block.getSelectedBoundingBox(state, world, pos));
				}
			}
		}
	}

	@SubscribeEvent
	public void onKey(InputEvent.KeyInputEvent event) {
		if (NBTEditKey.isPressed()) {
			RayTraceResult pos = Minecraft.getMinecraft().objectMouseOver;
			if (pos != null) {
				if (pos.entityHit != null) {
					NBTEdit.NETWORK.INSTANCE.sendToServer(new EntityRequestPacket(pos.entityHit.getEntityId()));
				} else if (pos.typeOfHit == RayTraceResult.Type.BLOCK) {
					NBTEdit.NETWORK.INSTANCE.sendToServer(new TileRequestPacket(pos.getBlockPos()));
				} else {
					this.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
				}
			}
		}
		
		if (NBTEditTeam.isPressed()) {
			PixelmonData[] party = ServerStorageDisplay.getPokemon();
			System.out.println("party = " + Arrays.toString(party));
			if (party != null && party[GuiPixelmonOverlay.selectedPixelmon] != null) {
				System.out.println("party[" + GuiPixelmonOverlay.selectedPixelmon + "] = " + party[GuiPixelmonOverlay.selectedPixelmon]);
				NBTEdit.NETWORK.INSTANCE.sendToServer(new EntityRequestTeamPacket(GuiPixelmonOverlay.selectedPixelmon));
			} else {
				this.sendMessage(null, "Error - Team member selected", TextFormatting.RED);
			}
		}
	}

	private void drawBoundingBox(RenderGlobal r, float f, AxisAlignedBB aabb) {
		if (aabb == null)
			return;

		Entity player = Minecraft.getMinecraft().getRenderViewEntity();

		double var8 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) f;
		double var10 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) f;
		double var12 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) f;

		aabb = aabb.grow(-var8, -var10, -var12);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 0.0F, 0.0F, .5F);
		GL11.glLineWidth(3.5F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
		worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.draw();
		worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
		worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.draw();
		worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
		worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
		worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
		worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ);
		worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

	}
}
