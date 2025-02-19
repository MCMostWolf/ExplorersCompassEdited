package com.chaosthedude.explorerscompass.network;

import com.chaosthedude.explorerscompass.worker.SearchWorkerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CleanFoundPacket {
    private UUID id;
    public CleanFoundPacket() {}

    public CleanFoundPacket(UUID id) {
        this.id = id;
    }

    public CleanFoundPacket(FriendlyByteBuf buf) {
        id = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SearchWorkerManager.foundChunks.removeIf(pair -> pair.getFirst().equals(id));
        });
        ctx.get().setPacketHandled(true);
    }
}
