/*
* A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import java.util.List;

/**
 	DAO stands for Data Access Object and it is use to interact easily with a data support. 
 	Every DAO is stateless. This means that a DAO does not hold any information concerning 
 	data on the storage. It only performs manipulations requested from the model logic of a 
 	software.The <i>DAO</i> interface represents the top of the hierarchy of these objects. 
 	Each <i>DAO</i> must implements this interface. This interface is based on two generic 
 	parameters. 
 *
 * @param <I> The type of the IDs or single identifiers of entities that will be/are saved 
 * on the data support. Even when records are not managed by IDs, this generic parameter 
 * must be set. One can set for example Object and omit the name of IDs while instantiating 
 * the corresponding class.
 * @param <J> The type of entities that are going to be or are saved on the data support. 
 * <b>T</b> might generally be a JavaBean.
 * 
 * @author Ramsès TALLA (ROST)
 * 
 * @version rostDAO 1.0
 * 
 * @since rostDAO 1.0
 */

 public interface DAO<I, J> extends rost.dao.base.DAO<J> {

	/**
	 * Searches for an entity on the data support whose identifier (id) 
	 * matches the parameter <b> id </b> on the data support and deletes
	 * it from the data storage.
	 * 
	 * @param id the identifier (id) of the object being deleted
	 * 
	 * @return  <code><b>* true</b></code> if an entity with the given 
	 * identifier (id) exists on the data support was found and 
	 * successfully deleted <br/>
	 * 
	 * <code><b>* false</b></code> either if the id doesn't exist on the data 
	 * support or if an entity with the given id was found but wasn't deleted 
	 * from the data support due to an unexpected error or exception
	 * 
	 * @throws Exception  
	 */
	boolean deleteByID(I id) throws Exception;
	
	/**
	 * Searches for an entity on the data source whose identifier (id) 
	 * equals the parameter <b> id </b>
	 * 
	 * @param id the identifier (primary key) of the object being found
	 * 
	 * @return <code><b> an object of type T</b></code> if an object 
	 * with the given id was found on the data support <br/>
	 * <code><b>null</b></code> otherwise or if an exception or error is 
	 * thrown during the operation.
	 * 
	 * @throws Exception 
	 */
	J getByID(I id) throws Exception;
	
	/**
	 * 
	 * @return a list of all identifiers of serialized entries on the data 
	 * support. This method can be very helpful to check during the addition 
	 * of a new object whether an identifier (or primary key) already exists
	 * on the data storage or not. Identifiers must be single, duplication 
	 * of identifiers is strictly forbidden !
	 *  
	 * @throws Exception 
	 */
	List<I> getIDs() throws Exception;
	
	/**
	 * Updates data on the data support
	 * 
	 * @param id the id of the old entity to be updated
	 * @param obj the new object with new properties
	 * @return <code><b>* true</b></code> if an entity with the given id has been found on the data support and updated by the new properties of the new object
	 * <br/><code><b>* false</b></code> otherwise
	 * 
	 * @throws Exception 
	 */
	boolean updateByID(I id, J obj) throws Exception;

	/**
	 * Updates the record of the data source having the identifier <b>id</b> 
	 * and replaces the property <b>property</b>by the argument 
	 * <b>newValue</b>.
	 * 
	 * @param id the id of the old entity to be updated
	 * @param propertyName the name of the property being checked
	 *  
	 * <b>propertyName</b> being updated.
	 * @param newValue the new value of the attribute 
	 * <b>propertyName</b> to set.
	 * 
	 * @return <code><b>* true</b></code> if an entity with the given identifier and property 
	 * has been found on the data support and updated by the new property 
	 * of the new object
	 * <br/><code><b>* false</b></code> otherwise
	 * 
	 * @throws Exception  
	 */
	boolean updatePropertyByID(I id, String propertyName, Object newValue) throws Exception;	
}