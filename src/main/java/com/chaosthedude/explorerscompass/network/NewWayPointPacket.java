package com.chaosthedude.explorerscompass.network;

import com.chaosthedude.explorerscompass.client.HandleNewWay;
import com.chaosthedude.explorerscompass.config.ConfigHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NewWayPointPacket {
    private final int x;
    private final int z;
    public NewWayPointPacket(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public NewWayPointPacket(FriendlyByteBuf buf) {
        x = buf.readInt();
        z = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端执行
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                if (ConfigHandler.CLIENT.newWayPoint.get()) {
                    HandleNewWay.handleNewWay(x, z);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}