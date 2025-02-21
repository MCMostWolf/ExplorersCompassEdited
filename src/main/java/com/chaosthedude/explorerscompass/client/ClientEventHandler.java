package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.connect.NewWayPoint;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

	private static final Minecraft mc = Minecraft.getInstance();
	private static CompassState state = CompassState.INACTIVE;
	public static int isFound = 0;
	public static HashMap<Integer, Pair<String, String>> HaveFound = new HashMap<>();


	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		HaveFound.clear();
		isFound = 0;
	}

	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent event) {
		if (mc.level != null && !mc.options.hideGui && !mc.options.renderDebug && (mc.screen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.screen instanceof ChatScreen))) {
			Player player = event.player;
			ItemStack itemStack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
			if (itemStack != null && itemStack.getItem() instanceof ExplorersCompassItem) {
				ExplorersCompassItem compass = (ExplorersCompassItem) itemStack.getItem();
				CompassState nowState = compass.getState(itemStack);
				if (nowState != state && nowState.equals(CompassState.FOUND)) {
					HaveFound.put(isFound, new Pair<>(StructureUtils.getPrettyStructureName(compass.getStructureKey(itemStack)), compass.getFoundStructureX(itemStack) + ", " + compass.getFoundStructureZ(itemStack)));
					isFound++;
					if (ModList.get().isLoaded("xaerominimap") && ConfigHandler.CLIENT.newWayPoint.get()) {
						NewWayPoint.addPoint(compass.getFoundStructureX(itemStack), compass.getFoundStructureZ(itemStack), player, StructureUtils.getPrettyStructureName(compass.getStructureKey(itemStack)), player.level);
					}
				}
				state = nowState;
			}
		}
	}
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && mc.level != null && !mc.options.hideGui && !mc.options.renderDebug && (mc.screen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.screen instanceof ChatScreen))) {
			final Player player = mc.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
			if (stack != null && stack.getItem() instanceof ExplorersCompassItem) {
				PoseStack poseStack = new PoseStack();
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				if (compass.getState(stack) == CompassState.SEARCHING) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.searching"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);
					
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
 					RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				} else if (compass.getState(stack) == CompassState.FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.found"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					if (compass.shouldDisplayCoordinates(stack)) {
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.coordinates"), 5, 5, 0xFFFFFF, 6);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, compass.getFoundStructureX(stack) + ", " + compass.getFoundStructureZ(stack), 5, 5, 0xAAAAAA, 7);

						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.distance"), 5, 5, 0xFFFFFF, 9);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(StructureUtils.getHorizontalDistanceToLocation(player, compass.getFoundStructureX(stack), compass.getFoundStructureZ(stack))), 5, 5, 0xAAAAAA, 10);
					}
				} else if (compass.getState(stack) == CompassState.NOT_FOUND) {
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.status"), 5, 5, 0xFFFFFF, 0);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.notFound"), 5, 5, 0xAAAAAA, 1);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.structure"), 5, 5, 0xFFFFFF, 3);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 5, 5, 0xAAAAAA, 4);

					RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.radius"), 5, 5, 0xFFFFFF, 6);
					RenderUtils.drawConfiguredStringOnHUD(poseStack, String.valueOf(compass.getSearchRadius(stack)), 5, 5, 0xAAAAAA, 7);
				}
				if (ConfigHandler.CLIENT.showHaveFound.get()) {
					if (isFound == 2 && !HaveFound.isEmpty() && HaveFound.get(0) != null) {
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.have_searched"), 64, 5, 0xFFFFFF, 0);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get(HaveFound.get(isFound - 2).getFirst()), 64, 5, 0xAAAAAA, 1);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, HaveFound.get(isFound - 2).getSecond(), 64, 5, 0xAAAAAA, 2);
					} else if (isFound >= 3 && !HaveFound.isEmpty() && HaveFound.get(isFound - 2) != null && HaveFound.get(isFound - 3) != null) {
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get("string.explorerscompass.have_searched"), 64, 5, 0xFFFFFF, 0);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get(HaveFound.get(isFound - 2).getFirst()), 64, 5, 0xAAAAAA, 1);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, HaveFound.get(isFound - 2).getSecond(), 64, 5, 0xAAAAAA, 2);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, I18n.get(HaveFound.get(isFound - 3).getFirst()), 64, 5, 0xAAAAAA, 4);
						RenderUtils.drawConfiguredStringOnHUD(poseStack, HaveFound.get(isFound - 3).getSecond(), 64, 5, 0xAAAAAA, 5);
					}
				}
			}
		}
	}

}