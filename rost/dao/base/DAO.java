/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.base;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 	DAO stands for Data Access Object and it is use to interact easily with a data support. 
 	Every DAO is stateless. This means that a DAO does not hold any information concerning 
 	data on the storage. It only performs manipulations requested from the model logic of a 
 	software.The <i>DAO</i> interface represents the top of the hierarchy of these objects. 
 	Each <i>DAO</i> must implements this interface. This interface is based on two generic 
 	parameters. 
 *
 * @param <J> The type of entities that are going to be or are saved on the data support. 
 * <b>T</b> is in this context a JavaBean. This is why the generic parameter is named as J.
 * 
 * @author Ramsès TALLA (ROST)
 * 
 * @version rostDAO 1.0
 * 
 * @since rostDAO 1.0
 */

@SuppressWarnings("unchecked")
public interface DAO<J> {

	/**
	 * This method takes takes many beans in form of an iterable object, converts them 
	 * into entities, and add them to the data source. This method may be in 
	 * particular very important if one wants to add many entities on the source
	 * one after one. This method must be so implement, that the resources used 
	 * to read and write on the data source must be opened only once and closed once and 
	 * only once all data have been added to the source. This ensures reduction of 
	 * the runtime and prevents from potential bugs coming from  multiple opening
	 * and closing resources.
	 * 
	 * @param objs The objects being added to the data source
	 * 
	 * @return The number of data added to the data source
	 * 
	 * @throws Exception
	 * */
	int add(Iterable<J> list) throws Exception;

	boolean add(J obj) throws Exception;

	/**
	 * This method takes any number of objects as parameters, converts them 
	 * into entities, and adds them to the data source. This method may be in 
	 * particular very important if one wants to add many entities on the source
	 * one after one. This method must be so implement, that the resources used 
	 * to read and write on the data source must be opened only once and closed once and 
	 * only once all data have been added to the source. This ensures reduction of 
	 * the runtime and prevents from potential bugs coming from  multiple opening
	 * and closing resources.
	 * 
	 * @param objs The objects being added to the data source
	 * 
	 * @return The number of data added to the data source
	 * 
	 * @throws Exception
	 * */
	int add(J... objs) throws Exception;

	/**
	 * This method takes takes many beans in form of an iterable object, converts them 
	 * into entities, and delete them from the data source. This method may be in 
	 * particular very important if one wants to add many entities on the source
	 * one after one. This method must be so implement, that the resources used 
	 * to read and write on the data source must be opened only once and closed once and 
	 * only once all data have been added to the source. This ensures reduction of 
	 * the runtime and prevents from potential bugs coming from  multiple opening
	 * and closing resources.
	 * 
	 * @param objs The objects being added to the data source
	 * 
	 * @return The number of data added to the data source
	 * 
	 * @throws Exception
	 * */
	int delete(Iterable<J> list) throws Exception;

	boolean delete(J obj) throws Exception;

	/**
	 * This method takes any number of objects as parameters, converts them 
	 * into entities, and removes them to the data source. This method may be in 
	 * particular very important if one wants to retrieve many entities on the source
	 * one after one. This method must be so implement, that the resources used 
	 * to read and write on the data source must be opened only once and closed once and 
	 * only once all data have been added to the source. This ensures reduction of 
	 * the runtime and prevents from potential bugs coming from  multiple opening
	 * and closing resources.
	 * 
	 * @param objs The objects being added to the data source
	 * 
	 * @return The number of data added to the data source
	 * 
	 * @throws Exception
	 **/
	int delete(J... objs) throws Exception;

	/**
	 * Deletes all records of the data source whose the object form satisfies a Predicate
	 * and return the number of records retrieved.
	 * 
	 * @param t The Predicate determining which records will be deleted
	 * 
	 * @return The number of record deleted
	 * 
	 * @throws Exception
	 * */
	int delete(Predicate<J> t) throws Exception;

	/**
	 * Deletes all entities of the data source whose the attribute 
	 * <b>property</b> matches the regular expression <b>regex</b>
	 *
	 * @return the final number of entities deleted by this method 
	 * 
	 * @param property the name of the property being checked
	 * @param regex the regular expression acting here as the deletion 
	 * criterion
	 * */
	int deleteByPattern(String property, String regex) throws Exception;

	/**
	 * Deletes all entities of the data source whose the attribute 
	 * <b>property</b> equals the argument <b>value</b>
	 *
	 * @return the final number of entities deleted by this method 
	 * 
	 * @param property the name of the property being checked
	 * @param value the value of the attribute <b>property</b> acting here as the deletion 
	 * criterion
	 * */
	int deleteByPropertyName(String property, Object value) throws Exception;

	/**
	 * Searches for a record on the data storage whose attribute values match 
	 * those of the argument <b> obj </b>
	 * 
	 * @param obj the object being found
	 * 
	 * @return <code><b>* true</b></code> if an entity on the data support with all properties of the object <b> obj </b> was found <br/>
	 * <code><b>* false</b></code> else or if an unexpected exception or error is thrown during the operation.
	 * 
	 * @throws Exception 
	 */
	boolean find(J obj) throws Exception;

	/**
	 * Returns a list all data from the persistence layer which object 
	 * form satisfies a Predicate
	 * 
	 * @param The predicate data will be selected by
	 * 
	 * @return an immutable list holding all data which object form satisfies 
	 * the given predicate
	 * 
	 * @throws Exception
	 * */
	List<J> get(Predicate<J> t) throws Exception;

	/**
	 * 
	 * Selects all data from the data support and returns a list of their beans.
	 * 
	 * @return a list of all entities on the data storage
	 * 
	 * @throws Exception 
	 */
	List<J> getAll() throws Exception;

	/**
	 * 
	 * Selects all data from the data support and returns a list of their 
	 * beans sorted according by a property and its sense.	 * 
	 * 
	 * @param propertyName The name of the property the beans will be sorted by.
	 * @param The sense of sorting the beans being returned.
	 * 
	 * @return a list of all entities on the data storage
	 * 
	 *@since rostDAO 2.0
	 *
	 * @throws Exception 
	 */
	List<J> getAllOrderBy(String propertyName, Sense sense) throws Exception;

	/**
	 * Select all entities of the data source whose the attribute 
	 * <b>property</b> matches the regular expression <b>regex</b>
	 *
	 * @return the list of all entities concerned by the selection criterion of 
	 * this method 
	 * 
	 * @param property the name of the property being checked
	 * 
	 * @param regex the regular expression acting here as the selection 
	 * criterion
	 * */
	List<J> getByPattern(String property, String regex) throws Exception;

	/**
	 * Select all entities of the data source whose the attribute 
	 * <b>property</b> matches the regular expression <b>regex</b>
	 * and sort them according by a property and its sense.
	 *
	 * @return the list of all entities as far as they are concerned by the 
	 * selecting criterion of this method 
	 * 
	 * @param property the name of the property being checked by the regex 
	 * @param regex the regular expression acting here as the selecting criterion
	 * @param propertyName The name of the property the beans will be sorted by.
	 * @param The sense of sorting the beans being returned
	 * 
	 * @since rostDAO 2.0
	 * 
	 * @throws Exception
	 * */
	List<J> getByPatternOrderBy(String property, String regex, String propertyName, Sense sense) throws Exception;

	/**
	 * Returns the list of all entities of the data storage whose a particular property 
	 * has a precise value. 
	 * 
	 * @param property The name of the property the entries will be checked by
	 * @param value The value after which the  entries will be hold in the final list.
	 * 
	 * @return the list of all entities as far as they are concerned by the 
	 * selecting criterion of this method 	 * 
	 * 
	 * @throws Exception*/
	List<J> getByPropertyName(String property, Object value) throws Exception;	

	/**
	 * Returns the list of all entities of the data storage whose a particular property 
	 * has a precise value and sort them according by a property and its sense.
	 *  
	 * @param property The name of the property the entries will be checked by
	 * @param value The value after which the  entries will be hold in the final list.
	 * @param propertyName The name of the property the beans will be sorted by.
	 * @param The sense of sorting the beans being returned
	 * 
	 * @return the list of all entities as far as they are concerned by the 
	 * selecting criterion of this method 
	 * 
	 * @since rostDAO 2.0
	 * 
	 * @throws Exception*/
	List<J> getByPropertyNameOrderBy(String property, Object value, String propertyName, Sense sense) throws Exception;

	<R> List<R> getProperty(Supplier<R> supp, String propertyName)  throws Exception;

	/**
	 * 
	 * Updates records on the storage which satisfies a predicate by setting their attributes 
	 * given in the array <i>propertynames</i> by the corresponding values in the array 
	 * <i<values</i>. Bear in mind that the values being set must be provided in the same 
	 * order as the names of attributes have been introduced in the array <i>propertynames</i>.
	 * some Exception may be thrown otherwise. Furthermore, this method will fail if one tries 
	 * to update all properties because it will lead to records duplication otherwise.
	 * 
	 * @param propertynames the array contain ing the names of attributes being updated
	 * @param values the array containing the values being set in the matching properties
	 * @param predicate The predicate defining which records should be updated
	 * 
	 * @return The number of records updated on the storage
	 * 
	 * @throws Exception an indeterminate Exception being thrown
	 * */
	int set(String[] propertynames, Object[] values, Predicate<J> predicate) throws Exception;

	/**
	 * Updates data of the data storage
	 * 
	 * @param oldObj the old object representing the old entry
	 * @param newObj the new object with new properties
	 * representing the new entry
	 * 
	 * @return <code><b>* true</b></code> if the old object has be found on 
	 * the data support and updated by the new properties of the new object
	 * 
	 * <br/><code><b>* false</b></code> otherwise
	 * 
	 * @throws Exception 
	 */	 
	boolean update(J oldObj, J newObj) throws Exception;

	/**
	 * Updates all records of the data source whose the attribute <b>property</b> equals the argument <b>oldValue</b> and replaces them by the value <b>newValue</b>.
	 * 
	 * @param propertyName the name of the property being checked
	 * @param oldValue the old value of the attribute 
	 * <b>propertyName</b> being updated.
	 * @param newValue the new value of the attribute 
	 * <b>propertyName</b> to set.
	 * 
	 * @return the number of entries updated
	 * 
	 * @throws Exception 
	 */
	int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception;	
}