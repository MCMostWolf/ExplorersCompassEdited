package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final int BASE_X = 5;
	private static final int HISTORY_X = 64;
	private static final int TITLE_COLOR = 0xFFFFFF;
	private static final int CONTENT_COLOR = 0xAAAAAA;

	@SubscribeEvent
	public void onRenderTick(RenderGuiOverlayEvent.Post event) {
		if (shouldRenderHUD()) {
			final Player player = mc.player;
			final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
			if (isValidCompass(stack)) {
				final ExplorersCompassItem compass = (ExplorersCompassItem) stack.getItem();
				final CompassState state = compass.getState(stack);
				if (state == CompassState.INACTIVE) return;
				renderMainState(event, compass, stack, player, state);
				if (ConfigHandler.CLIENT.showHaveFound.get()) {
					renderSearchHistory(event, compass, stack);
				}
			}
		}
	}

	private boolean shouldRenderHUD() {
		return mc.player != null
				&& mc.level != null
				&& !mc.options.hideGui
				&& !mc.options.renderDebug
				&& (mc.screen == null || (ConfigHandler.CLIENT.displayWithChatOpen.get() && mc.screen instanceof ChatScreen));
	}

	private boolean isValidCompass(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ExplorersCompassItem;
	}

	private void renderMainState(RenderGuiOverlayEvent.Post event, ExplorersCompassItem compass, ItemStack stack, Player player, CompassState state) {
		// 公共状态信息
		renderLinePair(event, "string.explorerscompass.status", I18n.get(getStatusKey(state)), 0);
		renderLinePair(event, "string.explorerscompass.structure", StructureUtils.getPrettyStructureName(compass.getStructureKey(stack)), 3);

		// 状态特定信息
		switch (state) {
			case SEARCHING, NOT_FOUND ->
					renderLinePair(event, "string.explorerscompass.radius", String.valueOf(compass.getSearchRadius(stack)), 6);
			case FOUND -> {
				if (compass.shouldDisplayCoordinates(stack)) {
					String coords = compass.getFoundStructureX(stack) + ", " + compass.getFoundStructureZ(stack);
					renderLinePair(event, "string.explorerscompass.coordinates", coords, 6);
					int distance = StructureUtils.getHorizontalDistanceToLocation(player, compass.getFoundStructureX(stack), compass.getFoundStructureZ(stack));
					renderLinePair(event, "string.explorerscompass.distance", String.valueOf(distance), 9);
				}
			}
		}
	}

	private void renderSearchHistory(RenderGuiOverlayEvent.Post event, ExplorersCompassItem compass, ItemStack stack) {
		int searchedTimes = compass.getSearchedTimes(stack);
		if (searchedTimes < 1) return;

		renderLine(event, I18n.get("string.explorerscompass.have_searched"), HISTORY_X, 0, TITLE_COLOR);
		renderSearchRecord(event, compass, stack, 1, 1);

		if (searchedTimes >= 2) {
			renderSearchRecord(event, compass, stack, 2, 4);
		}
	}

	private void renderSearchRecord(RenderGuiOverlayEvent.Post event, ExplorersCompassItem compass, ItemStack stack, int recordId, int lineOffset) {
		ResourceLocation structureKey = (recordId == 1) ? compass.getStructureKey1(stack) : compass.getStructureKey2(stack);
		boolean found = (recordId == 1) ? compass.getSearchResult1(stack) : compass.getSearchResult2(stack);
		int xCoord = (recordId == 1) ? compass.getFoundStructureX1(stack) : compass.getFoundStructureX2(stack);
		int zCoord = (recordId == 1) ? compass.getFoundStructureZ1(stack) : compass.getFoundStructureZ2(stack);

		renderLine(event, StructureUtils.getPrettyStructureName(structureKey), ClientEventHandler.HISTORY_X, lineOffset, CONTENT_COLOR);
		String result = found ? (xCoord + ", " + zCoord) : I18n.get("string.explorerscompass.notFound");
		renderLine(event, result, ClientEventHandler.HISTORY_X, lineOffset + 1, CONTENT_COLOR);
	}

	private void renderLinePair(RenderGuiOverlayEvent.Post event, String titleKey, String content, int startLine) {
		renderLine(event, I18n.get(titleKey), ClientEventHandler.BASE_X, startLine, TITLE_COLOR);
		renderLine(event, content, ClientEventHandler.BASE_X, startLine + 1, CONTENT_COLOR);
	}

	private void renderLine(RenderGuiOverlayEvent.Post event, String text, int x, int line, int color) {
		RenderUtils.drawConfiguredStringOnHUD(event.getGuiGraphics(), text, x, BASE_X, color, line);
	}

	private String getStatusKey(CompassState state) {
		return switch (state) {
            case INACTIVE -> null;
            case SEARCHING -> "string.explorerscompass.searching";
			case FOUND -> "string.explorerscompass.found";
			case NOT_FOUND -> "string.explorerscompass.notFound";
		};
	}
}