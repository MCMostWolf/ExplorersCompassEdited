package com.chaosthedude.explorerscompass.network;

import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class CleanFoundPacket {
    public CleanFoundPacket() {}

    public CleanFoundPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> SearchWorkerManager.foundChunks.removeIf(pair -> pair.getFirst().equals(Objects.requireNonNull(ctx.get().getSender()).getUUID())));
        ctx.get().setPacketHandled(true);
    }
}
