package com.chaosthedude.explorerscompass.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import static com.chaosthedude.explorerscompass.util.StructureUtils.haveFound;

public class CleanCachePacket {
    private final UUID uuid;
    public CleanCachePacket(UUID uuid) {
        this.uuid = uuid;
    }
    public CleanCachePacket(PacketBuffer buf) {
        uuid = buf.readUniqueId();
    }
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            haveFound.removeIf(pair -> pair.getFirst().equals(uuid));
        });
        ctx.get().setPacketHandled(true);
    }
}
