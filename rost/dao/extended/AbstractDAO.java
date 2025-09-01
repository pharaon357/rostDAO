/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.Message.DUPLICATE_ID_ENTRY;
import static rost.dao.extended.Message.ID_FOUND;
import static rost.dao.extended.Message.ITEM_FOUND;
import static rost.dao.extended.UtilityMethods.cloneBean;
import static rost.dao.extended.UtilityMethods.compareIDs;
import static rost.dao.extended.UtilityMethods.compareProperties;
import static rost.dao.extended.UtilityMethods.containsItem;
import static rost.dao.extended.UtilityMethods.getter;
import static rost.dao.extended.UtilityMethods.setter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 <code>AbstractDAO</code> is the class simplifying the development of DAO by 
 providing by default twelve methods so that only three method must be 
 implemented. <code>AbstractDAO</code> class could be considered as the main
 entry class for Data Access Objects on other persistence formats like 
 YAML, JSON, Properties, just to name a few, because it allows using 15 
 methods just by implementing three of them beforehand.
 *
 * @param <I> The type of the IDs or single identifiers of entities that will be/are saved on the data support
 * @param <J> The type of entities that are going to be or are saved on the data support. <b>J</b> might generally be a JavaBean.
 * 
 * @author Ramsès TALLA (ROST)
 * 
 * @version rostDAO 1.0
 * 
 * @since rostDAO 1.0
 */

@SuppressWarnings("unchecked")
abstract class AbstractDAO<I, J> extends DAO0<I, J> {

	AbstractDAO(Supplier<J> supp, String nameOfTheID, boolean checkID) throws Exception {
		super(supp, nameOfTheID, checkID);
	}

	@Override
	public boolean deleteByID(I id) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		J obj = getByID(id);

		return obj == null ? false : delete(obj);
	}

	@Override
	public int deleteByPattern(String property, String regex) throws Exception {

		check_property_names(property); /* checks if the name of the identifiers is set*/

		return delete(obj -> {
			try {
				return String.valueOf(objClass.getDeclaredMethod(getter(objClass.getDeclaredField(property))).invoke(obj)).matches(regex);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		});
	}

	@Override
	public int deleteByPropertyName(String property, Object value) throws Exception  {

		check_property_names(property); /* checks if the name of the identifiers is set*/

		return delete(obj -> {
			try {
				return compareProperties(value, objClass.getDeclaredMethod(getter(objClass.getDeclaredField(property))).invoke(obj));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		});
	}

	@Override
	public boolean find(J obj) throws Exception {

		return obj == null ? false : containsItem(getAll(), obj);
	}

	@Override
	public J getByID(I id) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		// This implementation stops once it found the object since the ID must be unique !
		List<J> datas = getAll();

		for(J obj : datas)
			if(compareProperties(id, objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(obj)))
				return obj;

		return null;
	}

	public List<J> getByPattern(String property, String regex) throws Exception {

		check_property_names(property); /* checks if the name of the identifiers is set.Very meaningful by preventing injections for example*/

		return get(obj -> {
			try {
				return String.valueOf(objClass.getDeclaredMethod(getter(objClass.getDeclaredField(property))).invoke(obj)).matches(regex);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		});
	}

	@Override
	public List<J> getByPropertyName(String property, Object value) throws Exception  {

		check_property_names(property); /* checks if the name of the identifiers is set.Very meaningful by preventing injections for example*/

		return get(obj -> {
			try {
				return compareProperties(value, objClass.getDeclaredMethod(getter(objClass.getDeclaredField(property))).invoke(obj));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		});
	}

	@Override
	public List<I> getIDs() throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		List<J> objs = getAll();

		Object[] array = new Object[objs.size()];

		int i = -1;

		Method method = objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))); /* reduces the runtime by 
		avoiding instantiating the same object with the same value in each loop call
		this*/

		for(Object obj : objs) {

			i++;

			I id = (I) method.invoke(obj);

			if(!containsItem(array, id))
				array[i] = id;

			else
				throw new DAOException(DUPLICATE_ID_ENTRY);
		}

		return new RostArrayList<I>(i + 1, array, true);
	}

	@Override
	public <R> List<R> getProperty(Supplier<R> supp, String propertyName) throws Exception {

		check_property_names(propertyName);

		Method method = objClass.getDeclaredMethod(getter(objClass.getDeclaredField(propertyName)));

		List<R> list = getAll()
				.stream()
				.map(obj -> {
					try {
						return (R) method.invoke(obj);
					} catch (Exception e) {
						throw new RuntimeException(e);
					} 
				})
				.toList();	
		
		return new RostArrayList<R>(list.size(), list.toArray(), false);
	}

	@Override
	public boolean update(J oldObj, J newObj) throws Exception {		

		if(nameOfTheID != null) {

			Method method = objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID)));

			I id_of_old = (I) method.invoke(oldObj);
			I id_of_new = (I) method.invoke(newObj);

			if(id_of_new != id_of_old && getByID(id_of_new) != null)
				throw new DAOException(ID_FOUND);
		}

		List<J> data = getAll();

		if(!containsItem(data, oldObj))
			return false;

		if(containsItem(data, newObj))
			throw new DAOException(ITEM_FOUND);

		return delete(oldObj) && add(newObj); 
	}

	@Override
	public boolean updateByID(I id, J obj) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		if(!getIDs().contains(id))
			return false;

		I id_of_obj = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(obj);

		J obj0 = getByID(id_of_obj);

		if(id_of_obj != id && obj0 != null)
			throw new DAOException(ID_FOUND);

		return update(obj0, obj);
	}

	@Override
	public int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception {

		check_property_names(propertyName);

		Method method = objClass.getDeclaredMethod(setter(propertyName));

		List<J> objs = getByPropertyName(propertyName, newValue);

		int number_of_update = 0;

		for(J obj : objs) {

			J newObj = cloneBean(obj);

			method.invoke(newObj, newValue);

			number_of_update += update(obj, newObj) ? 1 : 0;
		}

		return number_of_update;
	}

	@Override
	public boolean updatePropertyByID(I id, String propertyName, Object newValue) throws Exception {

		J oldObj = getByID(id), newObj = cloneBean(oldObj);

		if(compareIDs(id, String.valueOf(objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(oldObj))))
			objClass.getDeclaredMethod(setter(propertyName), objClass.getDeclaredField(propertyName).getType()).invoke(newObj, newValue);

		return update(oldObj, newObj);	
	}
}