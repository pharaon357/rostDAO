/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

/**
 * This enumeration holds possible messages to display while throwing a DAOException*/
enum Message {

	DUPLICATE_ID_ENTRY("Two or more than two objects have the same id on the data storage."),
	TRYING_TO_DUPLICATE_ENTRIES("This operation is not supported, because updating all properties leads to entries duplication."),
	DUPLICATE_ITEM_ENTRY("Two or more than two objects are identical on the data storage."),
	ITEM_FOUND("An entity with the same properties/attributes already exist on the data storage."), 
	ID_FOUND("An entity with the id %s already exists on the data storage."),
	NULL("The name of the attribute is null."),
	PROBIHITED_PROPERTY_FORMAT("The name of the desired attribute / identifier %s does not respect the rules of properties naming."),
	FORMAT_NOT_SUPPORTED("The XML saving properties format is not correctly set."),
	INVALID_HEADER("The header of the CSV file is not correct with respect to the field names."),
	UPDATE_SAME_OBJECTS("The object to be replaced and the object to replace are the same. No update is available.");

	Message(String text) {
		this.text = text;
	}

	private String text;	

	public String getText() {
		return text;
	}
}