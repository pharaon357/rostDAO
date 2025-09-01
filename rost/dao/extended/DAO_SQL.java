/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.Message.DUPLICATE_ID_ENTRY;
import static rost.dao.extended.Message.DUPLICATE_ITEM_ENTRY;
import static rost.dao.extended.Message.ITEM_FOUND;
import static rost.dao.extended.Message.NULL;
import static rost.dao.extended.Message.UPDATE_SAME_OBJECTS;
import static rost.dao.extended.UtilityMethods.cast;
import static rost.dao.extended.UtilityMethods.close;
import static rost.dao.extended.UtilityMethods.compare;
import static rost.dao.extended.UtilityMethods.containsItem;
import static rost.dao.extended.UtilityMethods.mappingField;
import static rost.dao.extended.UtilityMethods.unmapValueForSQL;
import static rost.dao.extended.UtilityMethods.unmapValuesForSQL;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

/**
DAO_SQL is the main or super class for DAOs having SQL relational database as data source
 *
 * @param <I> The type of the IDs or single identifiers of entities that will be/are saved on the data support
 * @param <J> The type of entities that are going to be or are saved on the data support. <b>T</b> might generally be a JavaBean.
 * 
 * @author Ramsès TALLA (ROST)
 * 
 * @version 1.0
 * 
 * @since 1.0
 */

@SuppressWarnings("unchecked")
final class DAO_SQL<I, J> extends DAO0<I, J> {

	private final Connection connect;

	private final String tableName; /* setting final to avoid TOCTTOU and therefore SQL injections*/

	private final int number_of_columns;

	private final String addStatement, deleteStatement, findStatement, updateStatement, updateByIDStatement;

	DAO_SQL(Supplier<J> supp, Connection connect, String nameOfTheID, boolean checkID) throws Exception {

		super(supp, nameOfTheID, checkID);

		tableName = objClass.getSimpleName();

		this.connect = connect;

		number_of_columns = fields.size();

		String blanks0 = fields.get(0).getName();
		String blanks = "?";
		String blanks1 = blanks0 + " = ?";
		String blanks2 = blanks1 + "";

		String field = null;

		for(int i = 1; i < number_of_columns; i++) {

			field = fields.get(i).getName();

			blanks0 = blanks0+ ", "+field;
			blanks = blanks.concat(", ?");
			blanks1 = blanks1 + " AND " + field +" = ?";
			blanks2 = blanks2 + ", " + field +" = ?";

		}

		// **************** addStatement ***********************

		addStatement = "INSERT INTO "+tableName+" ("+blanks0+")" +" VALUES ("+blanks+")";

		// **************** deleteStatement ***********************			

		deleteStatement = "DELETE FROM "+tableName +" WHERE "+ blanks1;

		// **************** findStatement ***********************

		findStatement = "SELECT * FROM "+tableName+" WHERE "+blanks1;

		// **************** updateStatement ***********************

		updateStatement = "UPDATE " +tableName+ " SET " + blanks2+ " WHERE "+blanks1;

		// **************** updateByIDStatement ***********************

		updateByIDStatement = "UPDATE "+tableName+ " SET "+ blanks2+ " WHERE " +this.nameOfTheID+ " = ?";

	}

	@Override
	public int add(Iterable<J> objs) throws Exception {

		PreparedStatement preparedStatement = connect.prepareStatement(addStatement);

		int number_of_adds = 0;

		try {

			for(J obj : objs) {

				if(find(obj)) 
					throw new DAOException(ITEM_FOUND);

				unmapValuesForSQL(obj, preparedStatement, 1, fields);

				number_of_adds = number_of_adds + preparedStatement.executeUpdate();

			}

			return number_of_adds;
		}

		finally {
			close(preparedStatement);
		}
	}

	@Override
	public int delete(Iterable<J> objs) throws Exception {

		PreparedStatement preparedStatement = connect.prepareStatement(deleteStatement);

		int number_of_deletion = 0;

		try {

			for(J obj : objs) {

				unmapValuesForSQL(obj, preparedStatement, 1, fields);

				number_of_deletion = number_of_deletion + preparedStatement.executeUpdate();
			} 

			return number_of_deletion;
		}

		finally {
			close(preparedStatement);
		}
	}

	@Override
	public boolean deleteByID(I id) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is correctly set to prevent from SQL injections*/

		PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM "+tableName+" WHERE "+nameOfTheID+" = ?");

		try {

			unmapValueForSQL(id, preparedStatement, 1, id.getClass());

			return preparedStatement.executeUpdate() == 1;

		} finally {
			close(preparedStatement);
		}
	}

	@Override
	public int deleteByPattern(String property, String regex) throws Exception {

		check_property_names(property); /* check the correctness of the property name to avoid SQL injections*/

		int number_of_deletion = 0;

		Statement statement = null;

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			statement = connect.createStatement();

			result = statement.executeQuery("SELECT * FROM "+tableName);

			preparedStatement = connect.prepareStatement("DELETE FROM "+tableName+ " WHERE " +property+ " = ?");

			while(result.next()) {

				Object data = result.getObject(property);

				if(String.valueOf(data).matches(regex)) {

					unmapValueForSQL(data, preparedStatement, 1);

					number_of_deletion += preparedStatement.executeUpdate();
				}
			}

			return number_of_deletion;

		} finally {
			close(preparedStatement, result, statement);
		}
	}

	@Override
	public int deleteByPropertyName(String property, Object value) throws Exception {

		check_property_names(property); /* checks if the name of the identifiers is correctly set. Prevent from SQL injections !*/

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connect.prepareStatement("DELETE FROM "+tableName+" WHERE "+property+" = ?");

			unmapValueForSQL(value, preparedStatement, 1);

			return preparedStatement.executeUpdate();

		} finally {
			close(preparedStatement);
		}
	}

	@Override
	public boolean find(J obj) throws Exception {

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			preparedStatement = connect.prepareStatement(findStatement);

			unmapValuesForSQL(obj, preparedStatement, 1, fields);

			result = preparedStatement.executeQuery();

			return result.next();

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public List<J> getAll() throws Exception {

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			int i = -1;

			Object[] array;

			// first step : counts the number of rows
			preparedStatement = connect.prepareStatement("SELECT COUNT(*) FROM " +tableName);

			result = preparedStatement.executeQuery();

			result.next();

			array = new Object[result.getInt(1)];

			close(result, preparedStatement);

			// second step : ORM
			preparedStatement = connect.prepareStatement("SELECT * FROM "+tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			result = preparedStatement.executeQuery();

			while(result.next()) {

				i++;

				J obj = (J) objClass.getConstructor().newInstance();

				for(Field field : fields) 
					mappingField(obj, field, result.getObject(field.getName()));

				if(!containsItem(array, obj))
					array[i] = obj;

				else
					throw new DAOException(DUPLICATE_ITEM_ENTRY);					
			}

			return new RostArrayList<J>(i + 1, array, true);

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public J getByID(I value) throws Exception {	

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is properly set to avoid SQL injections*/

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			preparedStatement = connect.prepareStatement("SELECT * FROM "+tableName+ " WHERE " +nameOfTheID+ " = ?");

			unmapValueForSQL(value, preparedStatement, 1);

			result = preparedStatement.executeQuery();

			if (!result.next()) 
				return null; 	            

			J obj = (J) objClass.getConstructor().newInstance();

			for(Field field : fields)
				mappingField(obj, field, result.getObject(field.getName()));

			return obj;

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public List<J> getByPattern(String property, String regex) throws Exception {

		int i = -1;

		Object[] array;

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			// first step : counts the number of rows

			preparedStatement = connect.prepareStatement("SELECT COUNT(*) FROM" +tableName);

			result = preparedStatement.executeQuery();

			result.next();

			array = new Object[result.getInt(1)];

			close(result, preparedStatement);

			// second step : ORM
			preparedStatement = connect.prepareStatement("SELECT * FROM "+tableName );

			result = preparedStatement.executeQuery();

			while(result.next()) {

				i++;

				if(String.valueOf(result.getObject(property)).matches(regex)) {

					J obj = (J) objClass.getConstructor().newInstance();

					for(Field field : fields) 
						mappingField(obj, field, result.getObject(field.getName()));

					if(!containsItem(array, obj))
						array[i] = obj;
					else
						throw new DAOException(DUPLICATE_ITEM_ENTRY);
				}
			}

			return new RostArrayList<J>(i + 1, array, true);

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public List<J> getByPropertyName(String property, Object value) throws Exception {

		// prevents from SQL injections !
		check_property_names(property);

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			int i = -1;

			Object[] array;

			// first step : counts the number of rows
			preparedStatement = connect.prepareStatement("SELECT COUNT(*) FROM " +tableName);

			result = preparedStatement.executeQuery();

			result.next();

			array = new Object[result.getInt(1)];

			close(result, preparedStatement);

			// second step : ORM
			preparedStatement = connect.prepareStatement("SELECT * FROM "+tableName+ " WHERE " +property+ " = ?");

			unmapValueForSQL(value, preparedStatement, i, classOfProperty(property));

			result = preparedStatement.executeQuery();

			while(result.next()) {

				i++;

				J obj = (J) objClass.getConstructor().newInstance();

				for(Field field : fields)
					mappingField(obj, field, result.getObject(field.getName()));

				if(!containsItem(array, obj))
					array[i] = obj;
				else
					throw new DAOException(DUPLICATE_ITEM_ENTRY);
			}

			return new RostArrayList<J>(i + 1, array, true);

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public <R> List<R> getProperty(Supplier<R> supp, String propertyName) throws Exception {

		check_property_names(propertyName); /* checks if the name of the identifiers is properly set, to bypass SQL injections*/

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			int i = -1;

			preparedStatement = connect.prepareStatement("SELECT COUNT(*) FROM "+tableName);

			result = preparedStatement.executeQuery();

			result.next();

			Object[] array = new Object[result.getInt(1)];

			close(result, preparedStatement);

			preparedStatement = connect.prepareStatement("SELECT "+propertyName+ " FROM " +tableName);

			result = preparedStatement.executeQuery();

			Field field = objClass.getDeclaredField(propertyName);

			while(result.next()) {

				i++;

				array[i] = (R)cast(field, result.getObject(1));
			}

			return new RostArrayList<R>(i + 1, array, false);

		} finally {
			close(result, preparedStatement);
		}
	}

	public List<I> getIDs() throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is properly set, to bypass SQL injections*/

		PreparedStatement preparedStatement = null;
		
		ResultSet result = null;

		try {

			int i = -1;

			preparedStatement = connect.prepareStatement("SELECT COUNT(*) FROM "+tableName);

			result = preparedStatement.executeQuery();

			result.next();

			Object[] array = new Object[result.getInt(1)];

			close(result, preparedStatement);

			preparedStatement = connect.prepareStatement("SELECT "+nameOfTheID+ " FROM " +tableName);

			result = preparedStatement.executeQuery();

			Field field = objClass.getDeclaredField(nameOfTheID);

			while(result.next()) {

				i++;

				I id = (I) cast(field, result.getObject(1));

				if(!containsItem(array, id))
					array[i] = id;
				else 
					throw new DAOException(DUPLICATE_ID_ENTRY);
			}

			return new RostArrayList<I>(i + 1, array, true);

		} finally {
			close(result, preparedStatement);
		}
	}

	@Override
	public boolean update(J oldObj, J newObj) throws Exception {

		PreparedStatement preparedStatement = null;

		if(oldObj == null || newObj == null) 
			throw new DAOException(NULL);

		if(compare(oldObj, newObj) || oldObj.equals(newObj)) 
			throw new DAOException(UPDATE_SAME_OBJECTS);

		try {
			preparedStatement = connect.prepareStatement(updateStatement); 

			unmapValuesForSQL(newObj, preparedStatement, 1, fields);

			unmapValuesForSQL(oldObj, preparedStatement, number_of_columns + 1, fields);

			return preparedStatement.executeUpdate() == 1;

		} finally {
			close(preparedStatement);	
		}
	}

	@Override
	public boolean updateByID(I id, J obj) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set, to avoid SQL injections*/

		J oldObj = getByID(id);

		PreparedStatement preparedStatement = null;

		if(oldObj == null)
			throw new DAOException(NULL);

		if(compare(oldObj, obj) || oldObj.equals(obj)) 
			throw new DAOException(UPDATE_SAME_OBJECTS);

		try {
			preparedStatement = connect.prepareStatement(updateByIDStatement);

			unmapValuesForSQL(obj, preparedStatement, 1, fields);

			preparedStatement.setObject(number_of_columns + 1, id);

			return preparedStatement.executeUpdate() == 1;

		} finally {
			close(preparedStatement);
		}
	}

	@Override
	public int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception {

		check_property_names(propertyName); /*Prevents from SQL injections*/

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connect.prepareStatement("UPDATE "+tableName+ " SET "+propertyName+ " = ? WHERE " +propertyName+ " = ?");

			Class<?> class8 = classOfProperty(propertyName);

			unmapValueForSQL(newValue, preparedStatement, 1, class8);

			unmapValueForSQL(oldValue, preparedStatement, 2, class8);

			return preparedStatement.executeUpdate();

		} finally {
			close(preparedStatement);
		}
	}

	@Override
	public boolean updatePropertyByID(I id, String propertyName, Object newValue) throws Exception {

		check_property_names(nameOfTheID, propertyName); /*Prevents from SQL injections*/

		PreparedStatement preparedStatement = null;

		try {

			preparedStatement = connect.prepareStatement("UPDATE "+tableName+ " SET "+propertyName+ " = ? WHERE "+nameOfTheID+" = ?");

			unmapValueForSQL(newValue, preparedStatement, 1, classOfProperty(propertyName));

			unmapValueForSQL(id, preparedStatement, 2, classOfProperty(nameOfTheID));

			return preparedStatement.executeUpdate() == 1;

		} finally {
			close(preparedStatement);
		}
	}
}