/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.Message.DUPLICATE_ID_ENTRY;
import static rost.dao.extended.Message.DUPLICATE_ITEM_ENTRY;
import static rost.dao.extended.Message.ID_FOUND;
import static rost.dao.extended.Message.INVALID_HEADER;
import static rost.dao.extended.Message.ITEM_FOUND;
import static rost.dao.extended.Message.UPDATE_SAME_OBJECTS;
import static rost.dao.extended.UtilityMethods.cast;
import static rost.dao.extended.UtilityMethods.close;
import static rost.dao.extended.UtilityMethods.compareIDs;
import static rost.dao.extended.UtilityMethods.compareProperties;
import static rost.dao.extended.UtilityMethods.containsItem;
import static rost.dao.extended.UtilityMethods.homeomorph;
import static rost.dao.extended.UtilityMethods.mappingField;
import static rost.dao.extended.UtilityMethods.unmapValuesForCSV;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 DAO_CSV is the main or super class for DAOs having CSV files as data source
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

@SuppressWarnings({"unchecked"})
final class DAO_CSV<I, J> extends DAO0<I, J> {

	private String filePath;

	private String header;

	private List<String> lines = new ArrayList<>();

	private BufferedReader reader = null;

	private char separator;

	private Stream<String> stream;

	private BufferedWriter writer = null;

	DAO_CSV(Supplier<J> supp, String filePath, String nameOfTheID, boolean checkID, Character separator) throws Exception {

		super(supp, nameOfTheID, checkID);

		this.filePath = filePath;

		this.separator = separator == null ? ',' : separator;

		/* removes superfluous spaces and lines from the CSV file */
		try {

			reader = Files.newBufferedReader(Paths.get(filePath));

			Collections.addAll(lines, reader.lines().toList().toArray(new String[10]));

			lines.removeIf(t -> t != null && t.matches("[\\t\\s\\r\\n]*"));

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));

			lines.remove(0);

			for(String line : lines) {
				if(line != null) {
					writer.newLine();				
					writer.write(line);
				}
			}
			writer.flush();

		} finally {

			close(reader, writer);
			lines = new ArrayList<>();
		}		
	}

	@Override
	public int add(Iterable<J> objs) throws Exception {

		int index_of_id_on_csv = nameOfTheID == null ? -1 : List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

		long count = lines.size();

		try {

			init();

			for(J obj : objs) {				

				String data0 = unmapValuesForCSV(obj, fields);

				List<String> data1 = List.of(data0.split(String.valueOf(separator)));

				for(int i = 1, c = lines.size(); i < c; i++) {

					if(index_of_id_on_csv != -1) {

						String id_on_csv = lines.get(i).split(String.valueOf(separator))[index_of_id_on_csv];
						String id_on_obj = data0.split(String.valueOf(separator))[index_of_id_on_csv];

						if(id_on_csv.matches(id_on_obj))
							throw new DAOException(String.format(ID_FOUND.getText(), id_on_obj.toString()));
					}

					if(	homeomorph(List.of(lines.get(i).split(String.valueOf(separator))), data1) )
						throw new DAOException(ITEM_FOUND);

				}

				writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.WRITE, StandardOpenOption.APPEND);

				writer.newLine();

				writer.write(data0);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			long count2 = reader.lines().toList().size();

			return (int) Math.abs(count - count2);

		} finally {
			close(stream, reader, writer);
		}
	}

	/**This method checks if all attributes specified at the header of the 
	 * CSV are the same as the fields of the JavaBean. This void method 
	 * throws an exception if the two list are not -in position loan- the 
	 * same. A valid header must be specified at the top of the CSV file. 
	 * Otherwise, an exception is thrown. By “valid header”, we mean a 
	 * header containing the names of all fields of the JavaBeans, one 
	 * another separated by a comma.*/
	private void checkHeader(String header, List<String> fieldsNames) throws Exception {

		if(!homeomorph(List.of(header.split(String.valueOf(separator))), fieldsNames)) 
			throw new DAOException(INVALID_HEADER);
	}

	@Override
	public int delete(Iterable<J> objs) throws Exception {

		try {

			init();

			long count = lines.size();

			for(J obj : objs) {

				String tz = unmapValuesForCSV(obj, fields);

				List<String> values1 =	List.of(tz.split(String.valueOf(separator)));

				boolean removed = false;

				for(int i = 0, c = lines.size(); i < c && removed == false; i++) {

					List<String> values2 = List.of(lines.get(i).split(String.valueOf(separator)));

					if(homeomorph(values1, values2)) 
						removed = lines.remove(tz) ? true : false; 

				}
			}

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			long count2 = stream.toList().size();

			return (int) Math.abs(count - count2);

		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public boolean deleteByID(I id) throws Exception {

		try {

			init();

			check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

			long count = lines.size();

			int index_of_id_on_csv = List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

			boolean removed = false;

			String data = null;

			for(int i = 1, c = lines.size(); i < c && removed == false; i++) {

				data = lines.get(i);

				String id_on_csv = data.split(String.valueOf(separator))[index_of_id_on_csv];

				if(compareIDs(id, id_on_csv) == true) {
					removed = lines.remove(data);
					break;
				}
			}

			if(removed == false) 
				return false;

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			long count2 = stream.toList().size();

			return Math.abs(count - count2) ==  1L ? true : false;

		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public int deleteByPattern(String property, String regex) throws Exception {

		try {

			init();

			check_property_names(property); /* checks if the name of the identifiers is set*/

			int number_of_deletion = 0;

			int index_of_property_on_csv = List.of(header.split(String.valueOf(separator))).indexOf(property);

			String data = null;

			for(int i = 1, c = lines.size(); i < c; i++) {

				data = lines.get(i);

				String property_on_csv = data.split(String.valueOf(separator))[index_of_property_on_csv];

				if(property_on_csv.matches(regex)) {

					number_of_deletion +=  lines.remove(data) == true ? 1 : 0;
				}
			}

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			return number_of_deletion;

		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public int deleteByPropertyName(String property, Object value) throws Exception {

		try {

			check_property_names(property); /* checks if the name of the identifiers is set*/

			init();

			int count = lines.size();

			int index_of_property_on_csv = List.of(header.split(String.valueOf(separator))).indexOf(property);

			String data = null;

			for(int i = 1, c = lines.size(); i < c; i++) {

				data = lines.get(i);

				String property_on_csv = data.split(String.valueOf(separator))[index_of_property_on_csv];

				if(compareProperties(value, property_on_csv) == true) {
					lines.remove(data);
				}
			}

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			int count2 = stream.toList().size();

			return Math.abs(count - count2);

		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public boolean find(J obj) throws Exception {

		try {

			init();

			boolean found = false;

			for(String line : lines) {

				if(line.matches(unmapValuesForCSV(obj, fields))) {
					found = true;					 
					break;
				}
			}

			return found;

		} finally {
			close(stream, reader);
		}
	}

	@Override
	public List<J> getAll() throws Exception {

		try {

			int k = -1;

			init();

			Object[] array = new Object[lines.size() - 1];

			String fields[] = header.split(String.valueOf(separator));

			for(int i = 1, c = lines.size(); i < c; i++) {

				k++;

				J obj = (J) objClass.getConstructor().newInstance();

				String[] dts  = lines.get(i).split(String.valueOf(separator));

				for(int j = 0; j < fields.length; j++)
					mappingField(obj, objClass.getDeclaredField(fields[j]), dts[j]);

				if(!containsItem(array, obj))
					array[i] = obj;

				else
					throw new DAOException(DUPLICATE_ITEM_ENTRY);
			}

			return new RostArrayList<J>(k + 1, array, true);

		} finally {
			close(stream, reader);
		}
	}

	@Override
	public J getByID(I id) throws Exception {

		try {

			init();

			check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

			String[] fields = header.split(String.valueOf(separator));

			checkHeader(header, fieldNames);

			int index = List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

			J obj = null;

			for(String line : lines) {

				if(compareIDs(id, line.split(String.valueOf(separator))[index])) {

					String dts[] = line.split(String.valueOf(separator));

					obj = (J) objClass.getConstructor().newInstance();

					for(int j = 0; j < fields.length; j++)
						mappingField(obj, objClass.getDeclaredField(fields[j]), dts[j]);

					break;
				}
			}

			return obj;

		} finally {			
			close(stream, reader);
		}
	}

	@Override
	public List<J> getByPattern(String property, String regex) throws Exception {

		try {

			int k = -1;

			init();

			check_property_names(property);

			checkHeader(header, fieldNames);

			Object[] array = new Object[lines.size() - 1];

			String fields[] = header.split(String.valueOf(separator));

			int index_of_property_on_csv = List.of(header.split(String.valueOf(separator))).indexOf(property);

			for(int i = 1, c = lines.size(); i < c; i++) {

				k++; 

				J obj = (J) objClass.getConstructor().newInstance();

				String[] dts  = lines.get(i).split(String.valueOf(separator));

				if(lines.get(i).split(String.valueOf(separator))[index_of_property_on_csv].matches(regex)) {

					for(int j = 0; j < fields.length; j++) 
						mappingField(obj, objClass.getDeclaredField(fields[j]), dts[j]);

					if(!containsItem(array, obj))
						array[i] = obj;
				}
			}

			return new RostArrayList<J>(k + 1, array, true);

		} finally {
			close(stream, reader);
		}		
	}

	@Override
	public List<J> getByPropertyName(String property, Object value) throws Exception {

		try {

			int k = -1;

			init();

			check_property_names(property);

			Object[] array = new Object[lines.size() - 1];

			checkHeader(header, fieldNames);

			String fields[] = header.split(String.valueOf(separator));

			int index = List.of(header.split(String.valueOf(separator))).indexOf(property);

			for(int i = 1, c = lines.size(); i < c; i++) {

				k++;

				J obj = (J) objClass.getConstructor().newInstance();

				String[] dts  = lines.get(i).split(String.valueOf(separator));

				if(String.valueOf(value).equals(lines.get(i).split(String.valueOf(separator))[index])) {

					for(int j = 0; j < fields.length; j++) 
						mappingField(obj, objClass.getDeclaredField(fields[j]), dts[j]);

					if(!containsItem(array, obj))
						array[i] = obj;
				}
			}

			return new RostArrayList<J>(k + 1, array, true);

		} finally {
			close(stream, reader);
		}
	}

	public List<I> getIDs() throws Exception {

		try {

			int k = -1;

			init();

			check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

			Object[] array = new Object[lines.size() - 1];

			int index = List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

			Field field = objClass.getDeclaredField(nameOfTheID);

			for(int i = 1; i < lines.size(); i++) {	

				k++;

				I id = (I)cast(field, lines.get(i).split(String.valueOf(separator))[index]);

				if(!containsItem(array, id))
					array[k] = id;
				else 
					throw new DAOException(DUPLICATE_ID_ENTRY);
			}

			return new RostArrayList<I>(k + 1, array, true);

		} finally {
			close(stream, reader);
		}
	}

	@Override
	public <R> List<R> getProperty(Supplier<R> supp, String propertyName) throws Exception {

		try {

			init();

			check_property_names(propertyName); /* checks if the name of the identifiers is set*/

			int index = List.of(header.split(String.valueOf(separator))).indexOf(propertyName);

			Field field = objClass.getDeclaredField(propertyName);

			lines.remove(0);

			return new RostArrayList<R>(lines.size(), lines
					.stream()
					.map(line -> {
						try {
							return (R) cast(field, line.split(String.valueOf(separator))[index]);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					})
					.toArray(), false);

		} finally {
			close(stream, reader);
		}
	}

	/*This methods initializes the fields being used for the CRUDL methods
	 *  and alleviates the codes of all methods*/
	private void init() throws Exception {

		reader = Files.newBufferedReader(Paths.get(filePath));

		lines = reader.lines().toList();

		header = lines.get(0);

		checkHeader(header, fieldNames);
	}

	@Override
	public boolean update(J oldObj, J newObj) throws Exception {

		try {

			init();

			long count = lines.size();

			String oldData = null, newData = null;

			if(oldObj != null && newObj != null) {

				oldData = unmapValuesForCSV(oldObj, fields);
				newData = unmapValuesForCSV(newObj, fields);

				if(oldData.matches(newData))
					throw new DAOException(UPDATE_SAME_OBJECTS);
			} else {
				return false;
			}

			int index_of_id_on_csv = nameOfTheID == null ? -1 : List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

			for(int i = 1, c = lines.size(); i < c; i++) {

				if(index_of_id_on_csv != -1) {

					String id_on_csv = lines.get(i).split(String.valueOf(separator))[index_of_id_on_csv];
					String id_on_newobj = newData.split(String.valueOf(separator))[index_of_id_on_csv];
					String id_on_oldobj = newData.split(String.valueOf(separator))[index_of_id_on_csv];

					if(id_on_csv.matches(id_on_newobj) && !id_on_newobj.matches(id_on_oldobj))
						throw new DAOException(ID_FOUND);
				}

				if(	homeomorph(List.of(lines.get(i).split(String.valueOf(separator))), List.of(newData.split(String.valueOf(separator)))) )
					throw new DAOException(ITEM_FOUND);

			}		

			boolean removed = false;

			int i;

			for(i = 1; i < lines.size() && removed == false; i++) {

				if(lines.get(i).matches(oldData)) {
					removed = lines.remove(oldData);
				}
			}

			if(removed == true) {
				lines.add(i, newData);
			} 

			else {
				return false;
			}

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			int count2 = stream.toList().size();

			return Math.abs(count - count2) ==  0L ? true : false;
		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public boolean updateByID(I id, J obj) throws Exception {

		try {

			init();

			check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

			String data0 = unmapValuesForCSV(obj, fields);

			List<String> data1 = List.of(data0.split(String.valueOf(separator)));

			long count = lines.size();

			int index_of_id_on_csv = List.of(header.split(String.valueOf(separator))).indexOf(nameOfTheID);

			boolean removed = false;

			String data = null;

			for(int i = 1, c = lines.size(); i < c; i++) {

				if(index_of_id_on_csv != -1) {

					String id_on_csv = lines.get(i).split(String.valueOf(separator))[index_of_id_on_csv];
					String id_on_obj = data1.get(index_of_id_on_csv);

					if(id_on_csv.matches(id_on_obj))
						throw new DAOException(ID_FOUND);
				}

				if(	homeomorph(List.of(lines.get(i).split(String.valueOf(separator))), data1) )
					throw new DAOException(ITEM_FOUND);
			}

			int i;

			for(i = 1; i < lines.size() && removed == false; i++) {

				data = lines.get(i);

				String id_on_csv = data.split(String.valueOf(separator))[index_of_id_on_csv];

				if(compareIDs(id, id_on_csv) == true) {
					removed = lines.remove(data);
					break;
				}
			}

			if(removed == false) 
				return false;

			else
				lines.add(i, unmapValuesForCSV(obj, fields));

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));
			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			long count2 = stream.toList().size();

			return Math.abs(count - count2) ==  0L ? true : false;

		} finally {
			close(stream, writer, reader);
		}
	}

	@Override
	public int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception {

		try {

			init();

			int number_of_update = 0;

			check_property_names(propertyName, nameOfTheID);

			List<String> fields = List.of(header.split(String.valueOf(separator)));

			int index_of_property = fields.indexOf(propertyName), index_of_id = fields.indexOf(nameOfTheID);

			for(int i = 1, c = lines.size(); i < c; i++) {

				String[] data = lines.get(i).split(String.valueOf(separator));

				String id_on_obj = data[index_of_id];

				if(data[index_of_property].equals(String.valueOf(oldValue))) {

					lines.remove(i);

					data[index_of_property] = String.valueOf(newValue);

					for(int j = 1, d = lines.size(); j < d; j++) {

						if(index_of_id != -1)
							if(lines.get(i).split(String.valueOf(separator))[index_of_id].matches(id_on_obj))
								throw new DAOException(ID_FOUND);

						if(homeomorph(List.of(lines.get(i).split(String.valueOf(separator))), List.of(data)))
							throw new DAOException(ITEM_FOUND);
					}

					lines.add(i, Stream.of(data).collect(Collectors.joining(String.valueOf(separator))));
					number_of_update++;					
				}
			}

			writer = Files.newBufferedWriter(Paths.get(filePath));

			writer.write(lines.get(0));

			lines.remove(0);

			for(String line : lines) {
				writer.newLine();
				writer.write(line);
			}

			writer.flush();

			return number_of_update;
		} finally {
			close(writer, stream, reader);
		}
	}

	@Override
	public boolean updatePropertyByID(I id, String propertyName, Object newValue) throws Exception {

		try {

			init();

			long count = lines.size();

			check_property_names(propertyName, nameOfTheID);

			List<String> fields = List.of(header.split(String.valueOf(separator)));

			int index_of_property = fields.indexOf(propertyName), index_of_id = fields.indexOf(nameOfTheID);

			for(int i = 1, c = lines.size(); i < c; i++) {

				String[] data = lines.get(i).split(String.valueOf(separator));

				String id_on_obj = data[index_of_id];

				if(compareIDs(id, data[index_of_id])) {

					lines.remove(i);

					data[index_of_property] = String.valueOf(newValue);

					for(int j = 1, d = lines.size(); j < d; j++) {

						if(lines.get(i).split(String.valueOf(separator))[index_of_id].matches(id_on_obj))
							throw new DAOException(ID_FOUND);

						if(	homeomorph(List.of(lines.get(i).split(String.valueOf(separator))), List.of(data)) )
							throw new DAOException(ITEM_FOUND);
					}

					lines.add(i, Stream.of(data).collect(Collectors.joining(String.valueOf(separator))));

					writer = Files.newBufferedWriter(Paths.get(filePath));

					writer.write(lines.get(0));
					lines.remove(0);

					for(String line : lines) {
						writer.newLine();
						writer.write(line);
					}

					writer.flush();

					break;
				}
			}

			reader = Files.newBufferedReader(Paths.get(filePath));

			stream = reader.lines();

			long count2 = stream.toList().size();

			return Math.abs(count - count2) ==  0L ? true : false;

		} finally {
			close(writer, stream, reader);
		}
	}
}