package com.chaosthedude.explorerscompass.gui;

import com.chaosthedude.explorerscompass.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TransparentButton extends Button {

	public TransparentButton(int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label, onPress);
	}

	@Override
	public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			float state = 2;
			if (!active) {
				state = 5;
			} else if (isHovered) {
				state = 4;
			}
			final float f = state / 2 * 0.9F + 0.1F;
			final int color = (int) (255.0F * f);

			GuiComponent.fill(poseStack, x, y, x + width, y + height, color / 2 << 24);
			drawBorder(poseStack, x, y, width, height, state);
			drawCenteredString(poseStack, mc.font, getMessage(), x + width / 2, y + (height - 8) / 2, 0xffffff);
		}
	}
	private void drawBorder(PoseStack poseStack,int x, int y, int width, int height, float state) {
		if (state==2) {
			state = 5;
		}
		else {
			state = 2;
		}
		// 上边框
		GuiComponent.fill(poseStack, x, y, x + width, y+1, (int) (0x33333333*state));
		// 下边框
		GuiComponent.fill(poseStack, x, y + height - 1, x + width, y + height, (int) (0x33333333*state));
		// 左边框
		GuiComponent.fill(poseStack, x, y, x + 1, y + height, (int) (0x33333333*state));
		// 右边框
		GuiComponent.fill(poseStack, x + width - 1, y, x + width, y + height, (int) (0x33333333*state));

	}

}