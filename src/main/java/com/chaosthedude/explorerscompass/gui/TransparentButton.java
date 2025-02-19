package com.chaosthedude.explorerscompass.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransparentButton extends Button {
	
	public TransparentButton(int x, int y, int width, int height, Component label, OnPress onPress) {
		super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
	}
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
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
			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color / 2 << 24);
			drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), state);
			guiGraphics.drawCenteredString(mc.font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xffffff);
		}
	}
	private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, float state) {
		if (state==2) {
			state = 5;
		}
		else {
			state = 2;
		}
		// 上边框
		guiGraphics.fill(x, y, x + width, y + 1, (int) (0x33333333*state));
		// 下边框
		guiGraphics.fill(x, y + height - 1, x + width, y + height, (int) (0x33333333*state));
		// 左边框
		guiGraphics.fill(x, y, x + 1, y + height, (int) (0x33333333*state));
		// 右边框
		guiGraphics.fill(x + width - 1, y, x + width, y + height, (int) (0x33333333*state));
	}
}