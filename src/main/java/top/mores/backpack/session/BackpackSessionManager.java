package top.mores.backpack.session;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackSessionManager {
    private BackpackSessionManager(){}

    private static final Map<UUID, BackpackSession> SESSIONS = new ConcurrentHashMap<>();

    public static void put(UUID uuid, BackpackSession session){
        SESSIONS.put(uuid, session);
    }

    public static BackpackSession get(UUID uuid){
        return SESSIONS.get(uuid);
    }

    public static BackpackSession remove(UUID uuid){
        return SESSIONS.remove(uuid);
    }

    public static Collection<BackpackSession> all(){
        return SESSIONS.values();
    }
}
