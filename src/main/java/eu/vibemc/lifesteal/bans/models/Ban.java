package eu.vibemc.lifesteal.bans.models;

import java.util.UUID;

public class Ban {
    public static final long PERMANENT_BAN_TIME = 5283862620L;

    private UUID playerUUID;
    private long unbanTime;

    public Ban(UUID playerUUID, long unbanTime) {
        this.playerUUID = playerUUID;
        this.unbanTime = unbanTime;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public long getUnbanTime() {
        // check if unbanTime is set
        if (this.unbanTime == 0) {
            return PERMANENT_BAN_TIME;
        }
        return this.unbanTime;
    }

    public void setUnbanTime(long unbanTime) {
        this.unbanTime = unbanTime;
    }
}
