package br.com.battlebits.commons.bukkit.protocol;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProtocolVersion {
	MINECRAFT_1_11_1(316, "v1_11", new String[0]), //
	MINECRAFT_1_11(315, "v1_11", new String[0]), //
	MINECRAFT_1_10(210, "v1_10", new String[0]), //
	MINECRAFT_1_9_4(110, "v1_9", new String[0]), //
	MINECRAFT_1_9_2(109, "v1_9", new String[0]), //
	MINECRAFT_1_9_1(108, "v1_9", new String[0]), //
	MINECRAFT_1_9(107, "v1_9", new String[0]), //
	MINECRAFT_1_8(47, "v1_8", new String[]{"*"}), //
	MINECRAFT_1_7_10(5, "v1_7", new String[]{"R3", "R4"}), //
	MINECRAFT_1_7_5(4, "v1_7", new String[]{"R1", "R2"}), //
	UNKNOWN(-1, null, null); //

	@Getter
	@NonNull
	private Integer id;
	@NonNull
	private String prefix;
	@NonNull
	private String[] suffix;
	
	public static ProtocolVersion getById(int id) {		
		for (ProtocolVersion version : values()) {
			if (id == version.getId()) {
				return version;
			}
		}
		return ProtocolVersion.UNKNOWN;
	}
	
	public static ProtocolVersion getByPackageVersion(String pkgVer) {
		for (ProtocolVersion version : values()) {
			if (version != ProtocolVersion.UNKNOWN && pkgVer.startsWith(version.prefix)) {
				for (String s : version.suffix) {
					if (pkgVer.endsWith(s) || pkgVer.equals("*")) {
						return version;
					}
				}
			}
		}
		return ProtocolVersion.UNKNOWN;
	}
}
