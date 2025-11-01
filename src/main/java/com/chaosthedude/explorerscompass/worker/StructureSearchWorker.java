package com.chaosthedude.explorerscompass.worker;

import java.util.List;
import java.util.UUID;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.connect.NewWayPoint;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.NewWayPointPacket;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.common.WorldWorkerManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;

public abstract class StructureSearchWorker<T extends StructurePlacement> implements WorldWorkerManager.IWorker {

	protected String managerId;
	protected ServerLevel level;
	protected Player player;
	protected ItemStack stack;
	protected BlockPos startPos;
	protected BlockPos currentPos;
	protected T placement;
	protected List<Structure> structureSet;
	protected int samples;
	protected boolean finished;
	protected int lastRadiusThreshold;
	protected List<Pair<UUID, ChunkPos>> foundChunks;
	
	public StructureSearchWorker(ServerLevel level, Player player, ItemStack stack, BlockPos startPos, T placement, List<Structure> structureSet, String managerId, List<Pair<UUID, ChunkPos>> foundChunks) {
		this.level = level;
		this.player = player;
		this.stack = stack;
		this.startPos = startPos;
		this.structureSet = structureSet;
		this.placement = placement;
		this.managerId = managerId;
		this.foundChunks = foundChunks;

        currentPos = startPos;
		samples = 0;

		finished = !level.getServer().getWorldData().worldGenOptions().generateStructures();
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			if (ConfigHandler.GENERAL.maxRadius.get() > 0) {
                ExplorersCompass.LOGGER.info("SearchWorkerManager {}: {} starting with {}{} max samples", managerId, getName(), shouldLogRadius() ? ConfigHandler.GENERAL.maxRadius.get() + " max radius, " : "", ConfigHandler.GENERAL.maxSamples.get());
				WorldWorkerManager.addWorker(this);
			} else {
				fail();
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() < ConfigHandler.GENERAL.maxRadius.get() && samples < ConfigHandler.GENERAL.maxSamples.get();
	}

	@Override
	public boolean doWork() {
		int radius = getRadius();
		if (radius > 250 && radius / 250 > lastRadiusThreshold) {
			if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
				((ExplorersCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius), player);
			}
			lastRadiusThreshold = radius / 250;
		}
		return false;
	}

	protected Pair<BlockPos, Structure> getStructureGeneratingAt(ChunkPos chunkPos) {
		// 检查当前区块是否已经在foundChunks中
		if (foundChunks.contains(Pair.of(player.getUUID(), chunkPos))) {
			return null;
		}

		for (Structure structure : structureSet) {
			StructureCheckResult result = level.structureManager().checkStructurePresence(chunkPos, structure, false);
			if (result != StructureCheckResult.START_NOT_PRESENT) {
				if (result == StructureCheckResult.START_PRESENT) {
					return Pair.of(placement.getLocatePos(chunkPos), structure);
				}

				ChunkAccess chunkAccess = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
				StructureStart structureStart = level.structureManager().getStartForStructure(SectionPos.bottomOf(chunkAccess), structure, chunkAccess);
				if (structureStart != null && structureStart.isValid()) {
					return Pair.of(placement.getLocatePos(structureStart.getChunkPos()), structure);
				}
			}
		}

		return null;
	}

	protected void succeed(BlockPos pos, Structure structure) {
        ExplorersCompass.LOGGER.info("SearchWorkerManager {}: {} succeeded with {}{} samples", managerId, getName(), shouldLogRadius() ? getRadius() + " radius, " : "", samples);
		if (!stack.isEmpty() && stack.getItem() instanceof ExplorersCompassItem compass) {
			compass.succeed(stack, StructureUtils.getKeyForStructure(level, structure), pos.getX(), pos.getZ(), samples, ConfigHandler.GENERAL.displayCoordinates.get());
			if (ModList.get().isLoaded("xaerominimap")) {
				if (player instanceof ServerPlayer serverPlayer) {
					ExplorersCompass.network.sendTo(new NewWayPointPacket(pos.getX(), pos.getZ()), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
				}
			}
		} else {
            ExplorersCompass.LOGGER.error("SearchWorkerManager {}: {} found invalid compass after successful search", managerId, getName());
		}
		ChunkPos chunkPos = new ChunkPos(pos);
        foundChunks.add(Pair.of(player.getUUID(), chunkPos));
		finished = true;
	}

	protected void fail() {
        ExplorersCompass.LOGGER.info("SearchWorkerManager {}: {} failed with {}{} samples", managerId, getName(), shouldLogRadius() ? getRadius() + " radius, " : "", samples);
		if (!stack.isEmpty() && stack.getItem() == ExplorersCompass.explorersCompass) {
			((ExplorersCompassItem) stack.getItem()).fail(stack, roundRadius(getRadius()), samples);
		} else {
            ExplorersCompass.LOGGER.error("SearchWorkerManager {}: {} found invalid compass after failed search", managerId, getName());
		}
		if (ConfigHandler.GENERAL.cleanCache.get()) {
			foundChunks.removeIf(pair -> pair.getFirst().equals(player.getUUID()));
		}

		finished = true;
	}

	public void stop() {
        ExplorersCompass.LOGGER.info("SearchWorkerManager {}: {} stopped with {}{} samples", managerId, getName(), shouldLogRadius() ? getRadius() + " radius, " : "", samples);
		finished = true;
	}

	protected int getRadius() {
		return StructureUtils.getHorizontalDistanceToLocation(startPos, currentPos.getX(), currentPos.getZ());
	}

	protected int roundRadius(int radius) {
		return (radius / 250) * 250;
	}

	protected abstract String getName();

	protected abstract boolean shouldLogRadius();
}
