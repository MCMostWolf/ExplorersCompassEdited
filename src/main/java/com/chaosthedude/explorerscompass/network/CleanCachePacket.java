package com.chaosthedude.explorerscompass.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

import static com.chaosthedude.explorerscompass.worker.SearchWorkerManager.foundChunks;

public class CleanCachePacket {
    public CleanCachePacket() {
    }
    public CleanCachePacket(FriendlyByteBuf buf) {
    }
    public void toBytes(FriendlyByteBuf buf) {
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            foundChunks.removeIf(pair -> pair.getFirst().equals(Objects.requireNonNull(ctx.get().getSender()).getUUID()));
        });
        ctx.get().setPacketHandled(true);
    }
}
