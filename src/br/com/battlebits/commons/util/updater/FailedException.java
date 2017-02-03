package br.com.battlebits.commons.util.updater;

import lombok.Getter;

public class FailedException extends Exception {

	private static final long serialVersionUID = 1L;
	@Getter
	private String message;

	public FailedException(String message) {
		this.message = message;
	}
}
