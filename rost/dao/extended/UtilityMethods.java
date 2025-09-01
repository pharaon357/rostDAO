/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.base.Format.ATTRIBUTE;
import static rost.dao.base.Format.TAG;
import static rost.dao.extended.Message.FORMAT_NOT_SUPPORTED;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rost.dao.base.Format;

@SuppressWarnings({ "unchecked", "rawtypes" })
final class UtilityMethods {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	/* perform a cast on the object value so that the resulting object has the same type 
	 as field*/
	public static final Object cast(Field field, Object value) throws Exception {

		if(field.getType() == Character.class || field.getType() == char.class) {

			return value.toString().charAt(0);
		}

		else if(field.getType() == Integer.class || field.getType() == int.class) {

			return Integer.valueOf(value.toString()).intValue();
		}

		else if(field.getType() == Long.class || field.getType() == long.class) {

			return Long.valueOf(value.toString()).longValue();
		}

		else if(field.getType() == Boolean.class || field.getType() == boolean.class) {

			return Boolean.valueOf(value.toString()).booleanValue();
		}

		else if(field.getType() == Float.class || field.getType() == float.class) {

			return Float.valueOf(value.toString()).floatValue();
		}

		else if(field.getType() == Double.class || field.getType() == double.class) {

			return Double.valueOf(value.toString()).doubleValue();
		}

		else if(field.getType() == Byte.class || field.getType() == byte.class) {

			return Byte.valueOf(value.toString()).byteValue();
		}

		else if(field.getType() == Short.class || field.getType() == short.class) {

			return Short.valueOf(value.toString()).shortValue();
		}

		else if( field.getType() == java.util.Date.class ) {

			return convertStringToDate(value.toString());
		}

		else 
			return field.getType().cast(value);
	}

	public static <T> T[] cloneArray(T[] array) {

		T[] newArray = (T[]) new Object[array.length];

		for(int i = 0; i < array.length; i++)
			newArray[i] = array[i];

		return newArray;
	}

	/*
	 * Creates a new object of type T and copies all attributes of the object bean into it.
	 * This ensures the existence of two equal object of a JavaBean without pointing to the 
	 * same reference.*/
	public static final <T> T cloneBean(T bean) throws Exception{

		Class<?> objClass = bean.getClass();

		List<Field>	fields = List.of(objClass.getDeclaredFields());

		Object clone = objClass.getConstructor().newInstance();

		for(Field field : fields) {

			objClass.getDeclaredMethod(setter(field.getName()), field.getType()).invoke(clone, objClass.getDeclaredMethod(getter(field)).invoke(bean));
		}

		return (T) clone;
	}

	/*
	 * Closes resources*/
	public static final void close(AutoCloseable... objs) throws Exception {

		for(AutoCloseable obj : objs) 
			if(obj != null) 
				obj.close();
	}

	/**
	 *Checks if two objects are the same or not. This method is based on the check of every properties of the two arguments. 

	 *@param first The first object the comparison will be done with
	 *@param second the second object the comparison will be done with
	 *
	 *@return <B>true</B> if the two arguments have the same properties and <b>false</b> if at least one of the properties of the two arguments are different
	 * @throws Exception 
	 */
	public static final <T> boolean compare(T first, T second) throws Exception {

		if(isPrimitiveOrWrapperOrStringOrDate(second))
			return first == second || first.equals(second) || String.valueOf(first).equals(String.valueOf(second));

		Class<? extends Object> objClass = first.getClass();

		List<Field> fields = List.of(objClass.getDeclaredFields());

		for(int i = 0, c = fields.size(); i < c; i++) {

			Method method = objClass.getDeclaredMethod(getter(fields.get(i)));

			Object param1 = method.invoke(first);
			Object param2 = method.invoke(second);

			if(param1 != param2 && !param1.equals(param2) && !String.valueOf(param1).equals(String.valueOf(param2)))
				return false;
		}

		return true;
	}

	public static final <T> boolean compareIDs(T id1, String id2) {

		return id1.getClass() == java.util.Date.class ? convertDateToString((java.util.Date)id1).matches(id2) : String.valueOf(id1).matches(id2);
	}

	public static final boolean compareProperties(Object first, Object second) {

		if(first == null && second == null) 
			return true;

		if((first == null && second != null ) || (first != null && second == null)) 
			return false;

		if(first.getClass() == java.util.Date.class && second.getClass() == java.util.Date.class) {
			return convertDateToString((java.util.Date)first).matches(convertDateToString((java.util.Date)second));
		}

		return first == second || first.equals(second) || String.valueOf(first).matches(String.valueOf(second));
	}

	public static final <T> boolean containsItem(T[] array, T obj) throws Exception {

		BiFunction<T, T, Boolean> tester = (obj0, obj1) -> {
			try {
				return obj0 != null && (obj0.equals(obj1) || compare(obj0, obj1));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		return Arrays.stream(array)
				.filter(Objects::nonNull)
				.anyMatch(object -> tester.apply(obj, object));

	}

	public static final <T> boolean containsItem(List<T> list, T obj) throws Exception {

		return containsItem(list.toArray(), obj);
	}

	public static final List<Integer> continued_fraction(int num, int den) {

		List<Integer> list = new ArrayList<Integer>();

		recursive_continued_fraction(num, den, list);

		return Collections.unmodifiableList(list);
	}

	public static final void iterative_continued_fraction(int num, int den, List<Integer> list) {

		while(num != 1 && den != 0) {

			list.add((int) Math.floor(num / den));

			num = num % den;

			if(num == 1) {
				list.add(den);
				break;
			}

			else {
				int c = num;
				num = den;
				den = c;
			}
		}
	}

	public static final void recursive_continued_fraction(int a, int b, List<Integer> list) {

		if(b != 0) {

			list.add(a / b);

			a = a % b;

			if(a == 1)			
				list.add(b);

			else
				recursive_continued_fraction(b, a, list);
		}	
	}

	public static final String convertDateToString(Date date) {

		Calendar tz = Calendar.getInstance();

		tz.setTime(date);

		if(tz.get(Calendar.MONTH) <= 9)
			return (tz.get(Calendar.YEAR) - 1900) +"-0"+(tz.get(Calendar.MONTH))+"-"+tz.get(Calendar.DAY_OF_MONTH);

		else
			return tz.get(Calendar.YEAR) +"-"+(tz.get(Calendar.MONTH))+"-"+tz.get(Calendar.DAY_OF_MONTH);

	}

	public static final Date convertStringToDate(String date) throws Exception {

		return date != null ? FORMATTER.parse(date) : null;
	}

	public static void formatXML(String inputFilePath, String outputFilePath) throws Exception {

		// Parse the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(inputFilePath));

		// Set up transformer for formatting
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// Write formatted XML to output file
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(outputFilePath));
		transformer.transform(source, result);
	}

	/*Converts a record (an entity) in a XML file to a Java object (JavaBean)*/
	public static final <T> T fromElementToObject(Class<? extends Object> objClass, Element el, Format format) throws Exception {

		Field[] fields = objClass.getDeclaredFields();

		T obj = null;

		if(format == Format.TAG) {

			obj = (T) objClass.getDeclaredConstructor().newInstance();

			for(Field field : fields)
				mappingField(obj, field, el.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());
		}

		if(format == Format.ATTRIBUTE) {

			obj = (T) objClass.getDeclaredConstructor().newInstance();

			for(Field field : fields)
				mappingField(obj, field, el.getAttribute(field.getName()));
		}
		return obj;
	}

	/* Returns the getter of a field according to the standard  naming conventions of Java*/
	public static final String getter(Field field) {

		if(field.getType() == boolean.class || field.getType() == Boolean.class)
			return "is" + upperCaseFirstLetter(field.getName());

		else
			return "get" + upperCaseFirstLetter(field.getName());
	}

	public static final String getter(String field) {
		return "get" + upperCaseFirstLetter(field);
	}

	/* Returns true if and only all elements of of a list are contained in another list.*/
	public static final <T> boolean homeomorph0(List<T> first, List<T> second) {

		if(first == null && second == null) 
			return true;

		if(first != null && second != null) {

			if(first.size() == second.size()) {
				for(int i = 0; i < first.size(); i++)
					if(!second.contains(first.get(i)))
						return false;

				return true;
			}
		}
		return false;
	}

	/* Returns true if and only all elements of of a list are contained in another list.*/
	public static <T> boolean homeomorph(List<T> list1, List<T> list2) {

		if (list1 == null || list2 == null) return false;

		if (list1.size() != list2.size()) return false;

		Function<List<T>, Map<T, Integer>> freq = list -> list.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

		return freq.apply(list1).equals(freq.apply(list2));
	}

	public static final <T> Queue<T> invertQueue(Queue<T> queue) {

		Queue<T> queue0 = new LinkedList<>(),
				queue1 = new LinkedList<>();

		int i = 0;

		while(!queue.isEmpty()) {
			queue1.add(queue.remove());	
			i++;
		}

		while(i > 0) {

			for(int j = 0; j < i; j++) {

				while(!queue1.isEmpty())
					queue0.add(queue1.remove());

				if(j != i - 1)
					queue1.add(queue0.remove());

				else
					queue.add(queue0.remove());
			}
			
			i--;
		}

		return queue;
	}

	public static final boolean isPrimitiveOrWrapperOrStringOrDate(Object obj) {

		Set<Class<?>> WRAPPER_TYPES = Set.of(
				Boolean.class, Byte.class, Character.class, Short.class,
				Integer.class, Long.class, Float.class, Double.class
				);

		if (obj == null)
			return false;

		Class<?> clazz = obj.getClass();
 
		return clazz.isPrimitive() || WRAPPER_TYPES.contains(clazz) || clazz.equals(String.class) || clazz.equals(java.util.Date.class);
	}

	public static final boolean isPropertyName(String name) {

		return name != null && name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
	}

	public static final String lowerCaseFirstLetter(String param) {

		Matcher matcher = Pattern.compile("^([A-Z])(\\w+)").matcher(param);

		matcher.find();

		return matcher.group(1).toLowerCase() + matcher.group(2);
	}

	public static final <T> void mappingField(T target, Field field, Object fieldValue) throws Exception {

		Class<?> targetClass = target.getClass();

		String setter = setter(field.getName());

		if(field.getType() == java.lang.Character.class || field.getType() == char.class) {

			try {
				targetClass.getDeclaredMethod(setter, char.class).invoke(target, String.class.getDeclaredMethod("charAt", int.class).invoke(fieldValue, 0));
			} catch (NoSuchMethodException e) {
				targetClass.getDeclaredMethod(setter, Character.class).invoke(target, String.class.getDeclaredMethod("charAt", int.class).invoke(fieldValue, 0));
			}
		}

		else if( field.getType() == java.util.Date.class) {

			targetClass.getDeclaredMethod(setter, field.getType()).invoke(target, convertStringToDate(fieldValue.toString()));

		}

		else if(field.getType() == int.class || field.getType() == Integer.class) {

			try {
				targetClass.getDeclaredMethod(setter, int.class).invoke(target, int.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Integer.class).invoke(target, Integer.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Integer.class).invoke(target, Integer.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, int.class).invoke(target, Integer.valueOf(fieldValue.toString()).intValue());
			}			
		}

		else if(field.getType() == byte.class || field.getType() == Byte.class) {

			try {
				targetClass.getDeclaredMethod(setter, byte.class).invoke(target, byte.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Byte.class).invoke(target, Byte.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Byte.class).invoke(target, Byte.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, byte.class).invoke(target, Byte.valueOf(fieldValue.toString()).byteValue());
			}	
		}

		else if(field.getType() == short.class || field.getType() == Short.class) {

			try {
				targetClass.getDeclaredMethod(setter, short.class).invoke(target, short.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Short.class).invoke(target, Short.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Short.class).invoke(target, Short.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, short.class).invoke(target, Short.valueOf(fieldValue.toString()).shortValue());
			}	
		}

		else if(field.getType() == long.class || field.getType() == Long.class) {

			try {
				targetClass.getDeclaredMethod(setter, long.class).invoke(target, long.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Long.class).invoke(target, Long.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Long.class).invoke(target, Long.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, long.class).invoke(target, Long.valueOf(fieldValue.toString()).longValue());
			}	
		}

		else if(field.getType() == float.class || field.getType() == Float.class) {

			try {
				targetClass.getDeclaredMethod(setter, float.class).invoke(target, float.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Float.class).invoke(target, Float.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Float.class).invoke(target, Float.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, float.class).invoke(target, Float.valueOf(fieldValue.toString()).floatValue());
			}	
		}

		else if(field.getType() == double.class || field.getType() == Double.class) {

			try {
				targetClass.getDeclaredMethod(setter, double.class).invoke(target, double.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Double.class).invoke(target, Double.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Double.class).invoke(target, Double.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, double.class).invoke(target, Double.valueOf(fieldValue.toString()).doubleValue());
			}	
		}

		else if(field.getType() == boolean.class || field.getType() == Boolean.class) {

			try {
				targetClass.getDeclaredMethod(setter, boolean.class).invoke(target, boolean.class.cast(fieldValue));
			} catch (NoSuchMethodException e) {

				try {
					targetClass.getDeclaredMethod(setter, Boolean.class).invoke(target, Boolean.class.cast(fieldValue));

				} catch (ClassCastException ex) {
					targetClass.getDeclaredMethod(setter, Boolean.class).invoke(target, Boolean.valueOf(fieldValue.toString()));

				}
			} catch(ClassCastException e) {
				targetClass.getDeclaredMethod(setter, boolean.class).invoke(target, Boolean.valueOf(fieldValue.toString()).booleanValue());
			}	
		}

		else
			targetClass.getDeclaredMethod(setter, field.getType()).invoke(target, field.getType().cast(fieldValue));
	}

	public static final <T> T[] resizeArray(int size, T[] array) {

		T[] newArray = (T[]) new Object[size];

		int index = 0;

		for(int i = 0; i < array.length && index != size; i++) 
			if(array[i] != null)
				newArray[index++] = array[i];

		return newArray;
	}

	public static final <T> List<T> rostSort(List<T> list, Comparator c) {

		Object[] array = list.toArray();

		int size = array.length;

		Arrays.sort(array, 0, size, c);

		return new RostArrayList<>(size, array, false);
	}

	public static final <T> boolean elementEqualsObject(T obj, Element el2) throws Exception {

		NodeList nodelist = el2.getChildNodes();

		for(int i = 0; i < nodelist.getLength(); i++) {

			Node node = nodelist.item(i);

			if(isPropertyName(node.getNodeName()) && !obj.getClass().getDeclaredMethod(getter(node.getNodeName())).toString().equals(node.getNodeValue()))
				return false;
		}

		return true;

		//		List<Node> list = new ArrayList<>();
		//
		//		for(int i = 0; i < nodelist.getLength(); i++) 
		//			list.add(nodelist.item(i));
		//
		//		return list
		//				.stream()
		//				.map(node -> {
		//					try {
		//						return obj.getClass().getDeclaredMethod(getter(node.getNodeName())).toString().equals(node.getNodeValue());
		//					} catch (NoSuchMethodException e) {
		//						throw new RuntimeException(e);
		//					} 
		//				})
		//				.noneMatch(entry -> entry == false);
	}

	public static final String setter(String field) {

		return "set" + upperCaseFirstLetter(field);
	}

	public static final <T> String unmapValuesForCSV(T obj, List<Field> fields) throws Exception {

		String data = "";

		Class<? extends Object> objClass = obj.getClass();

		for(int i = 0; i < fields.size(); i++) {

			Object addon = objClass.getDeclaredMethod(getter(fields.get(i))).invoke(obj);

			if(fields.get(i).getType() == java.util.Date.class)
				addon = convertDateToString((java.util.Date)addon);

			if(i != fields.size() - 1) {			

				data = data + addon + ",";
			}

			else 
				data = data + addon;
		}
		return data;
	}

	public static final <T> void unmapValuesForSQL(T obj, PreparedStatement preparedStatement, int start_index, List<Field> fields) throws Exception {

		int i = start_index;

		Class<? extends Object> objClass = obj.getClass();

		for(Field field : fields) {

			unmapValueForSQL(objClass.getDeclaredMethod(getter(field)).invoke(obj), preparedStatement, i, field.getType());

			i++;
		}	
	}

	public static final void unmapValueForSQL(Object value, PreparedStatement preparedStatement, int index) throws Exception {

		preparedStatement.setObject(index, value.getClass().equals(java.util.Date.class) ? new java.sql.Date( ((java.util.Date)value).getTime()) : value);

	}

	public static final void unmapValueForSQL(Object value, PreparedStatement preparedStatement, int index, Class<?> class_of_field) throws Exception {

		preparedStatement.setObject(index, class_of_field.equals(java.util.Date.class) ? new java.sql.Date( ((java.util.Date)value).getTime()) : value);

	}

	/**converts a JavaBean to a XML tag according to whether the attributes will be tags 
	 * or XML tags
	 * 
	 * @param obj the object being serialized
	 * @param document The document representing the XML file to write records 
	 * @param fields The fields being serialized
	 * @param format The format in which the attributes of the JavaBean will be stored
	 *
	 *@return an Element or a Node holding the record
	 * */
	public static final <T> Element unmapValuesForXML(T obj, Document document, List<Field> fields, Format format) throws Exception {

		Class<? extends Object> objClass = obj.getClass();

		Element element = document.createElement(lowerCaseFirstLetter(objClass.getSimpleName()));

		if(format == ATTRIBUTE) {

			for(int i = 0; i < fields.size(); i++) {

				Object addon = objClass.getDeclaredMethod(getter(fields.get(i))).invoke(obj);

				if(fields.get(i).getType() == java.util.Date.class)
					addon = convertDateToString((java.util.Date)addon);

				element.setAttribute(fields.get(i).getName(), addon.toString());
			}
		}

		else if(format == TAG) {

			for(int i = 0; i < fields.size(); i++) {

				Object addon = objClass.getDeclaredMethod(getter(fields.get(i))).invoke(obj);

				if(fields.get(i).getType() == java.util.Date.class)
					addon = convertDateToString((java.util.Date)addon);

				Element	elem = document.createElement(fields.get(i).getName());
				elem.appendChild(document.createTextNode(addon.toString()));

				element.appendChild(elem);
			}
		}

		else {
			throw new DAOException(FORMAT_NOT_SUPPORTED);
		}

		return element;
	}

	// Returns a String with its first letter capitalized
	public static final String upperCaseFirstLetter(String param) {

		if (param == null || param.isEmpty()) 
	        return param;
	    
	    return param.substring(0, 1).toUpperCase() + param.substring(1);
	}

	// Not to be instantiate !
	private UtilityMethods() {}
}