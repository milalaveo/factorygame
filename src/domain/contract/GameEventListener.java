package domain.contract;

import domain.model.GameEvent;

// I use this interface to receive game events.
public interface GameEventListener {
    void onEvent(GameEvent event);
}
