package de.cuuky.varo.player.event.events;

import de.cuuky.varo.player.VaroPlayer;
import de.cuuky.varo.player.event.BukkitEvent;
import de.cuuky.varo.player.event.BukkitEventType;

public class KickEvent extends BukkitEvent {

	public KickEvent() {
		super(BukkitEventType.KICKED);
	}

	@Override
	public void onExec(VaroPlayer player) {
		player.getStats().setBan();
		player.getStats().removeCountdown();
		player.getStats().addSessionPlayed();
	}
}
