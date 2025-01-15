package rsps.network;

import backend.service.ServiceLoader;

import java.util.Optional;

public final class PacketHandler {

    private static final PacketListener[] Listeners = new PacketListener[256];

    static {
        ServiceLoader.load("server.network.listeners", PacketListener.class, service -> {
            Listeners[service.getID()] = service;
            return true;
        });
    }

    public static Optional<PacketListener> get(int ID) {
        if (ID < 0 || ID >= Listeners.length)
            throw new IndexOutOfBoundsException("Packet Listener ID must be within 0 and 255");
        return Optional.ofNullable(Listeners[ID]);
    }

}
