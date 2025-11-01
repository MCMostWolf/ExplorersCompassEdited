package com.chaosthedude.explorerscompass.client;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.connect.NewWayPoint;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.util.ItemUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandleNewWay {
    public static void handleNewWay(int x, int z) {
        ItemStack stack = ItemStack.EMPTY;
        if (Minecraft.getInstance().player != null) {
            stack = ItemUtils.getHeldItem(Minecraft.getInstance().player, ExplorersCompass.explorersCompass);
        }
        if ((!stack.isEmpty()) && stack.getItem() instanceof ExplorersCompassItem compass) {
            String structureName = getPrettyStructureNameSafely(compass.getStructureKey(stack));
            NewWayPoint.addPoint(x, z, Minecraft.getInstance().player, structureName, Minecraft.getInstance().level);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static String getPrettyStructureNameSafely(ResourceLocation structureKey) {
        try {
            return StructureUtils.getPrettyStructureName(structureKey);
        } catch (Exception e) {
            // 如果无法获取美化名称，则返回原始键名
            return structureKey.toString();
        }
    }
}
