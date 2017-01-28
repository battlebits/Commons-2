package br.com.battlebits.commons.core.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.report.Report.ReportInformation;

public class ReportManager {

	public static List<Report> getReports() {
		List<Report> reports = new ArrayList<>();
		for (BattlePlayer player : BattlebitsAPI.getAccountCommon().getPlayers()) {
			if (player.getReport() != null)
				reports.add(player.getReport());
		}
		return reports;
	}

	public static boolean addReport(BattlePlayer reported, BattlePlayer player, String reason) {
		Report report = reported.getReport();
		if (report == null) {
			report = reported.newReport();
		}
		if (!report.addReport(player.getUniqueId(), player.getName(), player.getReportPoints(), reason))
			return false;
		reported.setRejectionLevel(reported.getRejectionLevel() + player.getReportPoints());

		return true;
	}

	public static void banPlayer(BattlePlayer banned) {
		if (banned.getReport() == null) {
			return;
		}
		Report report = banned.getReport();
		for (UUID players : report.getPlayersReason().keySet()) {
			BattlePlayer bp = BattlePlayer.getPlayer(players);
			bp.setReportPoints(bp.getReportPoints() + 100);
		}
		banned.clearReport();
		banned.setRejectionLevel(0);
	}

	public static void denyReport(BattlePlayer reported) {
		if (reported.getReport() == null) {
			return;
		}
		Report report = reported.getReport();
		for (Entry<UUID, ReportInformation> players : report.getPlayersReason().entrySet()) {
			BattlePlayer bp = BattlePlayer.getPlayer(players.getKey());
			bp.setReportPoints(bp.getReportPoints() - 100);
			players.getValue().reject();
		}
		report.expire();
	}

	public static void mutePlayer(BattlePlayer banned) {
		if (banned.getReport() == null) {
			return;
		}
		Report report = banned.getReport();
		for (UUID players : report.getPlayersReason().keySet()) {
			BattlePlayer bp = BattlePlayer.getPlayer(players);
			bp.getAccountVersion();
		}
		banned.clearReport();
		banned.setRejectionLevel(0);
	}

}
