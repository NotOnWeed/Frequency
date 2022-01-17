package xyz.elevated.frequency.check.impl.killaura;

import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import xyz.elevated.frequency.check.CheckData;
import xyz.elevated.frequency.check.type.PostCheck;
import xyz.elevated.frequency.data.PlayerData;
import xyz.elevated.frequency.wrapper.impl.client.WrappedPlayInFlying;
import xyz.elevated.frequency.wrapper.impl.client.WrappedPlayInUseEntity;

@CheckData(name = "KillAura (A)")
public final class KillAuraA extends PostCheck {

    private boolean sent;
    private long lastFlying, lastPacket;
    private double buffer;

    public KillAuraA(PlayerData playerData) {
        super(playerData, WrappedPlayInUseEntity.class);
    }

    @Override
    public void process(Object object) {
        if (object instanceof WrappedPlayInFlying) {
            long now = System.currentTimeMillis();
            long delay = now - lastPacket;

            if (sent) {
                if (delay > 40L && delay < 100L) {
                    buffer += 0.25;

                    if (buffer > 0.5) {
                        fail();
                    }
                } else {
                    buffer = Math.max(buffer - 0.025, 0);
                }

                sent = false;
            }

            lastFlying = now;
        } else if (object instanceof WrappedPlayInUseEntity) {
            WrappedPlayInUseEntity wrapper = (WrappedPlayInUseEntity) object;

            if (wrapper.getAction() != PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
                return;
            }

            long now = System.currentTimeMillis();
            long delay = now - lastFlying;

            if (delay < 10L) {
                lastPacket = now;
                sent = true;
            } else {
                buffer = Math.max(buffer - 0.025, 0.0);
            }
        }
    }
}
