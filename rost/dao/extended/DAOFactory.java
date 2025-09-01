/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import java.sql.Connection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import rost.dao.base.Format;

public final class DAOFactory {

	/*Don't let anyone instantiate this class !*/
	private DAOFactory() {}

	public static <I, J> DAO<I, J> getDAOforSQL(Supplier<J> entitySupplier, Supplier<I> ID_Supplier, Connection connect, String nameOfTheID) throws Exception {
		return new DAO_SQL<I, J>(entitySupplier, connect, nameOfTheID, true);
	}

	public static <J> rost.dao.base.DAO<J> getDAOforSQL(Supplier<J> entitySupplier, Connection connect) throws Exception {
		return new DAO_Base<J>(new DAO_SQL<Object, J>(entitySupplier, connect, null, false));
	}

	public static <I, J> DAO<I, J> getDAOforXML(Supplier<J> entitySupplier, Supplier<I> ID_Supplier, String filePath, Format format, String nameOfTheID) throws Exception {
		return new DAO_XML<I, J>(entitySupplier, filePath, format, nameOfTheID, true);
	}

	public static <J> rost.dao.base.DAO<J> getDAOforXML(Supplier<J> entitySupplier, String filePath, Format format) throws Exception {
		return new DAO_Base<J>(new DAO_XML<Object, J>(entitySupplier, filePath, format, null, false));
	}

	public static <I, J> DAO<I, J> getDAOforCSV(Supplier<J> entitySupplier, Supplier<I> ID_Supplier, String filePath, String nameOfTheID, char separator) throws Exception {
		return new DAO_CSV<I, J>(entitySupplier, filePath, nameOfTheID, true, separator);		
	}

	public static <J> rost.dao.base.DAO<J> getDAOforCSV(Supplier<J> entitySupplier, String filePath, char separator) throws Exception {
		return new DAO_Base<J>(new DAO_CSV<Object, J>(entitySupplier, filePath, null, false, separator));		
	}

	private static <I, J> DAO<I, J> getDAO(Supplier<J> instanceSupplier, Supplier<I> ID_Supplier, Supplier<List<J>> retriever, Function<Iterable<J>, Integer> persistenceFunction, Function<Iterable<J>, Integer> removingFunction, String nameOfTheID, boolean checkID) throws Exception {
		return new AbstractDAO<I, J>(instanceSupplier, nameOfTheID, checkID) {

			@Override
			public List<J> getAll() throws Exception {
				return retriever.get();
			}

			@Override
			public int add(Iterable<J> list) throws Exception {
				return persistenceFunction.apply(list);
			}

			@Override
			public int delete(Iterable<J> list) throws Exception {
				return removingFunction.apply(list);
			}
		};
	}

	public static <I, J> DAO<I, J> getDAO(Supplier<J> instanceSupplier, Supplier<I> ID_Supplier, Supplier<List<J>> retriever, Function<Iterable<J>, Integer> persistenceFunction, Function<Iterable<J>, Integer> removingFunction, String nameOfTheID) throws Exception {
		return getDAO(instanceSupplier, ID_Supplier, retriever, persistenceFunction, removingFunction, nameOfTheID, true);	
	}
	
	public static <J> rost.dao.base.DAO<J> getDAO(Supplier<J> instanceSupplier, Supplier<List<J>> retriever, Function<Iterable<J>, Integer> persistenceFunction, Function<Iterable<J>, Integer> removingFunction) throws Exception {
		return new DAO_Base<J>(getDAO(instanceSupplier, Object::new, retriever, persistenceFunction, removingFunction, null, false));	
	}
}