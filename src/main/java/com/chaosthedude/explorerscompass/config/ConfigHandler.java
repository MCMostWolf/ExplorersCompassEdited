package com.chaosthedude.explorerscompass.config;

import java.util.ArrayList;
import java.util.List;

import com.chaosthedude.explorerscompass.client.OverlaySide;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {

	private static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final General GENERAL = new General(GENERAL_BUILDER);
	public static final Client CLIENT = new Client(CLIENT_BUILDER);

	public static final ForgeConfigSpec GENERAL_SPEC = GENERAL_BUILDER.build();
	public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

	public static class General {
		public final ForgeConfigSpec.BooleanValue allowTeleport;
		public final ForgeConfigSpec.BooleanValue displayCoordinates;
		public final ForgeConfigSpec.IntValue maxRadius;
		public final ForgeConfigSpec.ConfigValue<List<String>> structureBlacklist;
		public final ForgeConfigSpec.IntValue maxSamples;
		public final ForgeConfigSpec.BooleanValue cleanCache;
		public final ForgeConfigSpec.BooleanValue couldFoundAlexCaves;
		public final ForgeConfigSpec.BooleanValue customResource;

		General(ForgeConfigSpec.Builder builder) {
			String desc;
			builder.push("General");

			desc = "Allows a player to teleport to a located structure when in creative mode, opped, or in cheat mode.";
			allowTeleport = builder.comment(desc).define("allowTeleport", true);
			
			desc = "Allows players to view the precise coordinates and distance of a located structure on the HUD, rather than relying on the direction the compass is pointing.";
			displayCoordinates = builder.comment(desc).define("displayCoordinates", true);

			desc = "The maximum radius that will be searched for a structure. Raising this value will increase search accuracy but will potentially make the process more resource intensive.";
			maxRadius = builder.comment(desc).defineInRange("maxRadius", 10000, 0, 1000000);

			desc = "A list of structures that the compass will not display in the GUI and will not be able to search for. Wildcard character * can be used to match any number of characters, and ? can be used to match one character. Ex: [\"minecraft:stronghold\", \"minecraft:endcity\", \"minecraft:*village*\"]";
			structureBlacklist = builder.comment(desc).define("structureBlacklist", new ArrayList<>());

			desc = "The maximum number of samples to be taken when searching for a structure.";
			maxSamples = builder.comment(desc).defineInRange("maxSamples", 100000, 0, 100000000);

			desc = "If this option is enabled, then the compass will clear the cache of previously found structures when the structure couldn't found.";
			cleanCache = builder.comment(desc).define("cleanCache", false);

			desc = "If this option is enabled, then the compass will search alex cave";
			couldFoundAlexCaves = builder.comment(desc).define("couldFoundAlexCaves", true);

			desc = "Load config.json";
			customResource = builder.comment(desc).define("customResource", false);

			builder.pop();
		}
	}

	public static class Client {
		public final ForgeConfigSpec.BooleanValue displayWithChatOpen;
		public final ForgeConfigSpec.BooleanValue translateStructureNames;
		public final ForgeConfigSpec.EnumValue<OverlaySide> overlaySide;
		public final ForgeConfigSpec.IntValue overlayLineOffset;
		public final ForgeConfigSpec.BooleanValue showHaveFound;
		public final ForgeConfigSpec.BooleanValue newWayPoint;

		Client(ForgeConfigSpec.Builder builder) {
			String desc;
			builder.push("Client");

			desc = "Displays Explorer's Compass information on the HUD even while chat is open.";
			displayWithChatOpen = builder.comment(desc).define("displayWithChatOpen", true);

			desc = "Attempts to translate structure names before fixing the unlocalized names. Translations may not be available for all structures.";
			translateStructureNames = builder.comment(desc).define("translateStructureNames", true);

			desc = "The line offset for information rendered on the HUD.";
			overlayLineOffset = builder.comment(desc).defineInRange("overlayLineOffset", 1, 0, 50);

			desc = "The side for information rendered on the HUD. Ex: LEFT, RIGHT";
			overlaySide = builder.comment(desc).defineEnum("overlaySide", OverlaySide.LEFT);

			desc = "If this option is enabled, then show the structures that have been searched in the past.";
			showHaveFound = builder.comment(desc).define("showHaveFound", true);

			desc = "If true, a new Xaero's Map way point will be add when you have search successfully";
			newWayPoint = builder.comment(desc).define("newWayPoint", true);

			builder.pop();
		}
	}
}
