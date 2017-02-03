package br.com.battlebits.commons.util.updater;

import lombok.Getter;

public class UpdatedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Getter
	private String message;

	public UpdatedException(String message) {
		this.message = message;
	}

}
