package mods.flammpfeil.slashblade.capability.mobeffect;

public class MobEffectState implements IMobEffectState {

    long stunTimeout = -1;
    long freezeTimeout = -1;

    int stunLimit = 200;
    int freezeLimit = 200;

    @Override
    public void setStunTimeOut(long timeout) {
        stunTimeout = timeout;
    }

    @Override
    public long getStunTimeOut() {
        return stunTimeout;
    }

    @Override
    public void setFreezeTimeOut(long timeout) {
        freezeTimeout = timeout;
    }

    @Override
    public long getFreezeTimeOut() {
        return freezeTimeout;
    }

    @Override
    public int getStunLimit() {
        return stunLimit;
    }

    @Override
    public void setStunLimit(int limit) {
        this.stunLimit = limit;
    }

    @Override
    public int getFreezeLimit() {
        return freezeLimit;
    }

    @Override
    public void setFreezeLimit(int limit) {
        this.freezeLimit = limit;
    }
}
