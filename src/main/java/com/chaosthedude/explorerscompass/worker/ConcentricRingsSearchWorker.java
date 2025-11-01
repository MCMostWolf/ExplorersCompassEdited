package com.chaosthedude.explorerscompass.worker;

import java.util.List;
import java.util.UUID;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

public class ConcentricRingsSearchWorker extends StructureSearchWorker<ConcentricRingsStructurePlacement> {

	private final List<ChunkPos> potentialChunks;
	private int chunkIndex;
	private double minDistance;
	private Pair<BlockPos, Structure> closest;

	public ConcentricRingsSearchWorker(ServerLevel level, Player player, ItemStack stack, BlockPos startPos, ConcentricRingsStructurePlacement placement, List<Structure> structureSet, String managerId, List<Pair<UUID, ChunkPos>> foundChunks) {
		super(level, player, stack, startPos, placement, structureSet, managerId, foundChunks);

		minDistance = Double.MAX_VALUE;
		chunkIndex = 0;
		potentialChunks = level.getChunkSource().getGeneratorState().getRingPositionsFor(placement);

		finished = !level.getServer().getWorldData().worldGenOptions().generateStructures() || potentialChunks == null || potentialChunks.isEmpty();
	}

	@Override
	public boolean hasWork() {
		// Samples for this placement are not necessarily in order of closest to furthest, so disregard radius
		return !finished && samples < ConfigHandler.GENERAL.maxSamples.get() && chunkIndex < potentialChunks.size();
	}

	@Override
	public boolean doWork() {
		super.doWork();
		if (hasWork()) {
			ChunkPos chunkPos = potentialChunks.get(chunkIndex);
			currentPos = new BlockPos(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 0, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
			double distance = startPos.distSqr(currentPos);
			
			if (closest == null || distance < minDistance) {
				Pair<BlockPos, Structure> pair = getStructureGeneratingAt(chunkPos);
				if (pair != null) {
					minDistance = distance;
					closest = pair;
				}
			}

			samples++;
			chunkIndex++;
		}

		if (hasWork()) {
			return true;
		}
		
		if (closest != null) {
			succeed(closest.getFirst(), closest.getSecond());
		} else if (!finished) {
			fail();
		}
		
		return false;
	}
	
	@Override
	protected String getName() {
		return "ConcentricRingsSearchWorker";
	}
	
	@Override
	public boolean shouldLogRadius() {
		return false;
	}

}
