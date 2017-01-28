package br.com.battlebits.commons.core.permission.group;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroup extends GroupInterface {

	@Override
	public List<String> getPermissions() {
		return new ArrayList<>();
	}
}
