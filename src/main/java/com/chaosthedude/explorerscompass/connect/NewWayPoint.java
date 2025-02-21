package com.chaosthedude.explorerscompass.connect;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.common.settings.ModSettings;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;


public class NewWayPoint {
    public static void addPoint(int x, int z, Player player, String structureName, Level level) {
        IXaeroMinimapClientPlayNetHandler clientLevel = null;
        if (Minecraft.getInstance().player != null) {
            clientLevel = (IXaeroMinimapClientPlayNetHandler) (Minecraft.getInstance().player.connection);
        }
        XaeroMinimapSession session = null;
        if (clientLevel != null) {
            session = clientLevel.getXaero_minimapSession();
        }
        WaypointsManager waypointsManager = null;
        if (session != null) {
            waypointsManager = session.getWaypointsManager();
        }
        Waypoint instant = new Waypoint(x, (int) player.getY(), z, structureName, structureName.substring(0,1), (int)(Math.random() * ModSettings.ENCHANT_COLORS.length), 0, false);
        if (waypointsManager != null) {
            waypointsManager.getWaypoints().getList().add(instant);
        }
        try {
            if (waypointsManager != null) {
                XaeroMinimap.instance.getSettings().saveWaypoints(waypointsManager.getCurrentWorld());
            }
        } catch (IOException error) {
            error.printStackTrace();
        }
    }
}
