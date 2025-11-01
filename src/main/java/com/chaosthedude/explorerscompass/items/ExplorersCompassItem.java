package com.chaosthedude.explorerscompass.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.BetterUI;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.gui.GuiWrapper;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;

public class ExplorersCompassItem extends Item {

	public static final String NAME = "explorerscompass";

	private static int haveFoundTimes = 0;

	private SearchWorkerManager workerManager;

	public ExplorersCompassItem() {
		super(new Properties().stacksTo(1));
		workerManager = new SearchWorkerManager();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!player.isCrouching()) {
			if (level.isClientSide()) {

				final ItemStack stack = ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass);
				GuiWrapper.openGUI(level, player, stack);
			} else {
				final ServerLevel serverLevel = (ServerLevel) level;
				final ServerPlayer serverPlayer = (ServerPlayer) player;
				final boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get() && PlayerUtils.canTeleport(player.getServer(), player);
				ExplorersCompass.network.sendTo(new SyncPacket(canTeleport, StructureUtils.getAllowedStructureKeys(serverLevel), StructureUtils.getGeneratingDimensionsForAllowedStructures(serverLevel), StructureUtils.getStructureKeysToTypeKeys(serverLevel), StructureUtils.getTypeKeysToStructureKeys(serverLevel)), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		} else {
			workerManager.stop();
			workerManager.clear();
			setState(player.getItemInHand(hand), null, CompassState.INACTIVE, player);
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
	}
	
	@Override
 	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
 		if (getState(oldStack) == getState(newStack)) {
 			return false;
 		}
 		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
 	}

	public void searchForStructure(Level level, Player player, ResourceLocation categoryKey, List<ResourceLocation> structureKeys, BlockPos pos, ItemStack stack) {
		setSearching(stack, categoryKey, player);
		setSearchRadius(stack, 0, player);
		if (level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel) level;
			List<Structure> structures = new ArrayList<Structure>();
			for (ResourceLocation key : structureKeys) {
				structures.add(StructureUtils.getStructureForKey(serverLevel, key));
			}
			workerManager.stop();
			workerManager.createWorkers(serverLevel, player, stack, structures, pos);
			boolean started = workerManager.start();
			if (!started) {
				setNotFound(stack, 0, 0);
			}
		}
	}
	
	public void succeed(ItemStack stack, ResourceLocation structureKey, int x, int z, int samples, boolean displayCoordinates) {
		setFound(stack, structureKey, x, z, samples);
		if (ConfigHandler.GENERAL.customResource.get()) {
			List<Map.Entry<String, Integer>> entries = BetterUI.getEntries();
			for (Map.Entry<String, Integer> entry : entries) {
				if (structureKey.toString().equals(entry.getKey())) {
					stack.getTag().putInt("CustomModelData", entry.getValue());
				}
			}
		}
		setDisplayCoordinates(stack, displayCoordinates);
		workerManager.clear();
	}
	
	public void fail(ItemStack stack, int radius, int samples) {
		workerManager.pop();
		boolean started = workerManager.start();
		if (!started) {
			setNotFound(stack, radius, samples);
		}
	}

	public boolean isActive(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return getState(stack) != CompassState.INACTIVE;
		}

		return false;
	}

	public void setSearching(ItemStack stack, ResourceLocation structureKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("StructureKey", structureKey.toString());
			stack.getTag().putInt("State", CompassState.SEARCHING.getID());
		}
	}

	public void setFound(ItemStack stack, ResourceLocation structureKey, int x, int z, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.FOUND.getID());
			stack.getTag().putString("StructureKey", structureKey.toString());
			stack.getTag().putInt("FoundX", x);
			stack.getTag().putInt("FoundZ", z);
			stack.getTag().putInt("Samples", samples);
			setHaveSearched(stack, structureKey, true, x, z);
		}
	}

	public void setNotFound(ItemStack stack, int searchRadius, int samples) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.NOT_FOUND.getID());
			stack.getTag().putInt("SearchRadius", searchRadius);
			stack.getTag().putInt("Samples", samples);
			setHaveSearched(stack, this.getStructureKey(stack), false, 0, 0);
		}
	}

	public void setInactive(ItemStack stack, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", CompassState.INACTIVE.getID());
		}
	}

	public void setState(ItemStack stack, BlockPos pos, CompassState state, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("State", state.getID());
		}
	}

	public void setFoundStructureX(ItemStack stack, int x, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundX", x);
		}
	}

	public void setFoundStructureZ(ItemStack stack, int z, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("FoundZ", z);
		}
	}

	public void setStructureKey(ItemStack stack, ResourceLocation structureKey, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putString("StructureKey", structureKey.toString());
		}
	}

	public void setSearchRadius(ItemStack stack, int searchRadius, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("SearchRadius", searchRadius);
		}
	}

	public void setSamples(ItemStack stack, int samples, Player player) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putInt("Samples", samples);
		}
	}
	
	public void setDisplayCoordinates(ItemStack stack, boolean displayPosition) {
		if (ItemUtils.verifyNBT(stack)) {
			stack.getTag().putBoolean("DisplayCoordinates", displayPosition);
		}
	}

	public CompassState getState(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return CompassState.fromID(stack.getTag().getInt("State"));
		}

		return null;
	}

	public int getFoundStructureX(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundX");
		}

		return 0;
	}

	public int getFoundStructureX1(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getInt("FoundX1");
		}
		return 0;
	}

	public int getFoundStructureX2(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getInt("FoundX2");
		}
		return 0;
	}

	public int getFoundStructureZ(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("FoundZ");
		}

		return 0;
	}

	public int getFoundStructureZ1(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getInt("FoundZ1");
		}
		return 0;
	}

	public int getFoundStructureZ2(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getInt("FoundZ2");
		}
		return 0;
	}

	public ResourceLocation getStructureKey(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return new ResourceLocation(stack.getTag().getString("StructureKey"));
		}

		return new ResourceLocation("");
	}
	public ResourceLocation getStructureKey1(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return new ResourceLocation(stack.getTag().getCompound("HaveSearched").getString("StructureKey1"));
		}

		return new ResourceLocation("");
	}

	public ResourceLocation getStructureKey2(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return new ResourceLocation(stack.getTag().getCompound("HaveSearched").getString("StructureKey2"));
		}

		return new ResourceLocation("");
	}

	public int getSearchRadius(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("SearchRadius");
		}

		return -1;
	}

	public int getSamples(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack)) {
			return stack.getTag().getInt("Samples");
		}

		return -1;
	}

	public int getDistanceToBiome(Player player, ItemStack stack) {
		return StructureUtils.getHorizontalDistanceToLocation(player, getFoundStructureX(stack), getFoundStructureZ(stack));
	}
	
	public boolean shouldDisplayCoordinates(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("DisplayCoordinates")) {
			return stack.getTag().getBoolean("DisplayCoordinates");
		}

		return true;
	}
	public int getOrInitHaveFoundTimes(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getInt("HaveFoundTimes");
		}
		else {
			return 0;
		}
	}
	public boolean getSearchResult1(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getBoolean("SearchState1");
		}
		else {
			return false;
		}
	}
	public boolean getSearchResult2(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getBoolean("SearchState2");
		}
		else {
			return false;
		}
	}

	public int getSearchedTimes(ItemStack stack) {
		if (ItemUtils.verifyNBT(stack) && stack.getTag().contains("HaveSearched")) {
			return stack.getTag().getCompound("HaveSearched").getInt("SearchedTimes");
		}
		else {
			return 0;
		}
	}

	public void setHaveSearched(ItemStack stack, ResourceLocation structureKey, boolean isFound, int x, int z) {
		if (ItemUtils.verifyNBT(stack)) {
			if (!stack.getTag().contains("HaveSearched")) {
				CompoundTag tag = new CompoundTag();
				tag.putString("StructureKey", structureKey.toString());
				tag.putString("StructureKey1","");
				tag.putString("StructureKey2", "");

				tag.putBoolean("SearchState", isFound);
				tag.putBoolean("SearchState1", false);
				tag.putBoolean("SearchState2", false);
				tag.putInt("FoundX", x);
				tag.putInt("FoundX1", 0);
				tag.putInt("FoundZ2", 0);
				tag.putInt("FoundZ", z);
				tag.putInt("FoundX1", 0);
				tag.putInt("FoundZ2", 0);
				tag.putInt("SearchedTimes", 0);
				stack.getTag().put("HaveSearched", tag);
			}
			else {
				CompoundTag tag = stack.getTag().getCompound("HaveSearched");
				tag.putString("StructureKey2", tag.getString("StructureKey1"));
				tag.putString("StructureKey1", tag.getString("StructureKey"));
				tag.putString("StructureKey", structureKey.toString());
				tag.putBoolean("SearchState2", tag.getBoolean("SearchState1"));
				tag.putBoolean("SearchState1", tag.getBoolean("SearchState"));
				tag.putBoolean("SearchState", isFound);
				tag.putInt("FoundX2", tag.getInt("FoundX1"));
				tag.putInt("FoundZ2", tag.getInt("FoundZ1"));
				tag.putInt("FoundX1", tag.getInt("FoundX"));
				tag.putInt("FoundZ1", tag.getInt("FoundZ"));
				tag.putInt("FoundX", x);
				tag.putInt("FoundZ", z);
				tag.putInt("SearchedTimes", tag.getInt("SearchedTimes") + 1);
			}
		}
	}
}
