/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.base;

/**This enumeration holds all possibles formats to store records in XML files.
 * The values of this enumeration must be set while instantiating the class 
 * DAO_XML in its constructors. Bear in mind that some exceptions 
 * 
 * @author Ramsès TALLA
 * 
 * @version rostDAO 1.0
 * 
 * */
public enum Format {
	
	/**This value means that all attributes of the JavaBean are saved in the XML file
	 * in tags form. All attributes of the entities are surrounded by their corresponding 
	 * names of field in the JavaBean. If one has for example the entity "User", holding 
	 * two attributes naming "email" and "password", one would save the records of this 
	 * entity with the value TAG of this enumeration as follow:
	 * <user>
	 * 		<email>myemail@domain_name.com</email>
	 * 		<password>mypassword</password>
	 * </user>
	 * 
	 * On the above example, all attributes are enclosed by tags with the same names
	 * as appearing in the JavaBean.*/
	TAG, 
	
	/**This value means that all fields of the JavaBean are saved like XML attributes
	 * in the XML file. If one has for example the entity "User", holding two attributes 
	 * naming "email" and "password", one would save the records of this entity with the 
	 * value ATTRIBUTE of this enumeration as follow:
	 * <user email = "myemail@domain_name.com" password="mypassword" />s
	 * 
	 * On the above example, all attributes are enclosed by tags with the same names
	 * as appearing in the JavaBean.*/
	ATTRIBUTE
}