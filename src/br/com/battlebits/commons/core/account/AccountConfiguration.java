package br.com.battlebits.commons.core.account;

import br.com.battlebits.commons.core.data.DataPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class AccountConfiguration {

	private boolean ignoreAll = false;
	private boolean tellEnabled = true;
	private boolean canPlaySound = true;
	private boolean showAlerts = true;
	private boolean staffChatEnabled = false;
	private boolean clanChatEnabled = false;
	@Setter
	protected transient BattlePlayer player;

	public AccountConfiguration(BattlePlayer player) {
		this.player = player;
	}

	public void setCanPlaySound(boolean canPlaySound) {
		if (this.canPlaySound != canPlaySound) {
			this.canPlaySound = canPlaySound;
			DataPlayer.saveConfigField(player, "canPlaySound");
		}
	}

	public void setClanChatEnabled(boolean clanChatEnabled) {
		if (this.clanChatEnabled != clanChatEnabled) {
			this.clanChatEnabled = clanChatEnabled;
			DataPlayer.saveConfigField(player, "clanChatEnabled");
		}
	}

	public void setIgnoreAll(boolean ignoreAll) {
		if (this.ignoreAll != ignoreAll) {
			this.ignoreAll = ignoreAll;
			DataPlayer.saveConfigField(player, "ignoreAll");
		}
	}

	public void setShowAlerts(boolean showAlerts) {
		if (this.showAlerts != showAlerts) {
			this.showAlerts = showAlerts;
			DataPlayer.saveConfigField(player, "showAlerts");
		}
	}

	public void setStaffChatEnabled(boolean staffChatEnabled) {
		if (this.staffChatEnabled != staffChatEnabled) {
			this.staffChatEnabled = staffChatEnabled;
			DataPlayer.saveConfigField(player, "staffChatEnabled");
		}
	}

	public void setTellEnabled(boolean tellEnabled) {
		if (this.tellEnabled != tellEnabled) {
			this.tellEnabled = tellEnabled;
			DataPlayer.saveConfigField(player, "tellEnabled");
		}
	}

}
