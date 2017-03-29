package br.com.battlebits.commons.bukkit.tcbo3;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */

@AllArgsConstructor
class BO3Block
{
	@Getter
	protected int x, y, z;
	@Getter
	protected Material material;
	@Getter
	protected byte data;
}
