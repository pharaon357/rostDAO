/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

/**
 * This class represents an Exception that can be thrown while performing CRUDL methods
 * 
 * @author Ramsès TALLA
 * */
class DAOException extends Exception {

	private static final long serialVersionUID = 1L;

	public DAOException(Message message) {

		this(message.getText());
	}

	public DAOException(String message) {

		super(message);
	}
}