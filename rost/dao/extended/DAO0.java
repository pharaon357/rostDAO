/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.Message.NULL;
import static rost.dao.extended.Message.PROBIHITED_PROPERTY_FORMAT;
import static rost.dao.extended.Message.TRYING_TO_DUPLICATE_ENTRIES;
import static rost.dao.extended.UtilityMethods.getter;
import static rost.dao.extended.UtilityMethods.isPropertyName;
import static rost.dao.extended.UtilityMethods.rostSort;
import static rost.dao.extended.UtilityMethods.setter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import rost.dao.base.Sense;

/** 
 This class introduces the instantiation of objects related to the use of reflection.

 * @param <I> The type of the IDs or single identifiers of entities that will be/are saved on the data support
 * @param <J> The type of entities that are going to be or are saved on the data support. <b>T</b> might generally be a JavaBean.
 * 
 * @author Ramsès TALLA (ROST)
 * 
 * @version rostDAO 1.0
 * 
 * @since rostDAO 1.0
 */

@SuppressWarnings("unchecked")
abstract class DAO0<I, J> implements DAO<I, J> {

	final List<String> fieldNames;

	final List<Field> fields;

	final Class<?> objClass;

	final String nameOfTheID;

	DAO0(Supplier<J> supp, String nameOfTheID, boolean checkID) throws Exception {

		this.objClass = supp.get().getClass();

		Field[] fields = objClass.getDeclaredFields();

		this.fields = new RostArrayList<Field>(fields.length, fields, false);

		fieldNames = this.fields.stream().map(Field::getName).toList();		

		if(checkID)
			check_property_names(nameOfTheID);

		this.nameOfTheID = nameOfTheID;
	}

	@Override
	public final boolean add(J obj) throws Exception {		
		return obj != null && add(Collections.singletonList(obj)) == 1;
	}

	@Override
	public final boolean delete(J obj) throws Exception {
		return obj != null && delete(Collections.singletonList(obj)) == 1;
	}

	/**
	 * This methods checks if the property's name respects the property's 
	 * naming rules and prevents from problems like SQL injections.
	 * 
	 * @param name The name of the property to check
	 * 
	 * @throws DAOException if one of the following case is encountered : <br />
	 *  * the property's name is null, <br />
	 *  * the property's name contains special characters exclude the underscore,  <br />
	 *  * the property's name is not an attribute of the enclosing JavaBean*/
	final void check_property_names(String... names) throws DAOException {

		for(String name : names) {

			if(name == null)
				throw new DAOException(NULL);

			if(!isPropertyName(name))
				throw new DAOException(String.format(PROBIHITED_PROPERTY_FORMAT.getText(), name));

			if(!fieldNames.contains(name))
				throw new DAOException("The property \""+name+ "\" is not an attribute of the JavaBean "+objClass.getName());
		}
	}

	@Override
	public final int add(J... objs) throws Exception {		
		return add(Arrays.stream(objs)::iterator);
	}

	@Override
	public final int delete(J... objs) throws Exception {
		return delete(Arrays.stream(objs)::iterator);
	}

	final Class<?> classOfProperty(String propertyName) {
		return fields.get(fieldNames.indexOf(propertyName)).getType();
	}

	@Override
	public final int delete(Predicate<J> t) throws Exception {
		return delete(get(t));
	}

	@Override	
	public final List<J> get(Predicate<J> t) throws Exception {

		List<J> list = getAll()
				.stream()
				.filter(t)
				.toList();

		return new RostArrayList<J>(list.size(), list.toArray(), false);
	}

	@Override
	public final List<J> getAllOrderBy(String propertyName, Sense sense) throws Exception {

		check_property_names(propertyName);

		return rostSort(getAll(), new RostComparator<J>(propertyName, sense));
	}

	@Override
	public final List<J> getByPropertyNameOrderBy(String property, Object value, String propertyName, Sense sense)
			throws Exception {

		check_property_names(property, propertyName);

		return rostSort(getByPropertyName(property, value), new RostComparator<J>(propertyName, sense));
	}

	@Override
	public final List<J> getByPatternOrderBy(String property, String regex, String propertyName, Sense sense)
			throws Exception {

		check_property_names(property, propertyName);

		return rostSort(getByPattern(propertyName, regex), new RostComparator<J>(propertyName, sense));
	}

	@Override
	public final int set(String[] propertynames, Object[] values, Predicate<J> predicate) throws Exception {

		if(propertynames.length >= fields.size())
			throw new DAOException(TRYING_TO_DUPLICATE_ENTRIES);

		if(propertynames.length > values.length)
			throw new DAOException("Update's operation failed. There are more properties provided than values.");

		else if(propertynames.length < values.length)
			throw new DAOException("Update's operation failed. There are more values provided than properties.");

		else {

			check_property_names(propertynames); // for security reasons

			int result = 0;

			List <J> objects = get(predicate);

			for(J object : objects) {

				J ty = (J) objClass.getConstructor().newInstance();

				for(int i = 0; i < fieldNames.size(); i++)
					objClass.getDeclaredMethod(setter(propertynames[i]), classOfProperty(propertynames[i]))
					.invoke(ty, objClass.getMethod(getter(fields.get(i))).invoke(object));

				for(int i = 0; i < propertynames.length; i++) 
					objClass.getDeclaredMethod(setter(propertynames[i]), classOfProperty(propertynames[i])).invoke(ty, values[i]);

				result += update(object, ty) ? 1 : 0;
			}

			return result;
		}
	}
}