/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.Message.DUPLICATE_ID_ENTRY;
import static rost.dao.extended.Message.ID_FOUND;
import static rost.dao.extended.Message.ITEM_FOUND;
import static rost.dao.extended.Message.UPDATE_SAME_OBJECTS;
import static rost.dao.extended.UtilityMethods.cast;
import static rost.dao.extended.UtilityMethods.compare;
import static rost.dao.extended.UtilityMethods.compareIDs;
import static rost.dao.extended.UtilityMethods.containsItem;
import static rost.dao.extended.UtilityMethods.formatXML;
import static rost.dao.extended.UtilityMethods.fromElementToObject;
import static rost.dao.extended.UtilityMethods.getter;
import static rost.dao.extended.UtilityMethods.lowerCaseFirstLetter;
import static rost.dao.extended.UtilityMethods.mappingField;
import static rost.dao.extended.UtilityMethods.unmapValuesForXML;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rost.dao.base.Format;

/**
DAO_XML is the main or super class for DAOs having XML files as data source
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
final class DAO_XML<I, J> extends DAO0<I, J> {

	private Document document;

	private String filePath;

	private Format format;

	private StreamResult res;

	private DOMSource src;

	private final String tagName;

	private final Transformer transformer;

	DAO_XML(Supplier<J> supp, String filePath, Format format, String nameOfTheID, boolean checkID) throws Exception {

		super(supp, nameOfTheID, checkID);

		this.format = format;

		this.filePath = filePath;

		this.tagName = lowerCaseFirstLetter(objClass.getSimpleName());

		this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(filePath));

		this.transformer = TransformerFactory.newInstance().newTransformer();

		this.src = new DOMSource(this.document);

		this.res = new StreamResult(this.document.getDocumentURI());
	}

	@Override
	public int add(Iterable<J> objs) throws Exception {

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return 0;

		int result = 0;

		for(J obj : objs) {			

			Element element = unmapValuesForXML(obj, document, fields, format);	

			I id = null;

			if(nameOfTheID != null)
				id = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(obj);

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				if(nameOfTheID != null) {

					if(format == Format.TAG) 					
						if(compareIDs(id, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim()))
							throw new DAOException(String.format(ID_FOUND.getText(), id.toString()));

					if(format == Format.ATTRIBUTE)
						if(compareIDs(id, item.getAttribute(nameOfTheID)))
							throw new DAOException(String.format(ID_FOUND.getText(), id.toString()));
				}

				if(format == Format.TAG) {

					if(UtilityMethods.elementEqualsObject(obj, item))
						throw new DAOException(ITEM_FOUND);
				}

				if(format == Format.ATTRIBUTE)
					if(item.isEqualNode(element) || item.isSameNode(element))
						throw new DAOException(ITEM_FOUND);
			}

			document.getDocumentElement().appendChild(document.createTextNode("\n\n\t"));

			Node added = document.getDocumentElement().appendChild(element);

			result = result + (added == null ? 0 : 1);
		}

		if(result > 0)
			flush();

		return result;
	}

	@Override
	public int delete(Iterable<J> objs) throws Exception {

		int result = 0;

		NodeList list = document.getElementsByTagName(tagName);

		Node removed = null;

		if(list == null || list.getLength() == 0) 
			return 0;

		for(J obj : objs) {

			Element element = unmapValuesForXML(obj, document, fields, format);

			if(format == Format.TAG) {

				for(int i = 0; i < list.getLength() && removed == null; i++) {

					Element item = (Element) list.item(i);

					Element item0 = item;

					NodeList data = item0.getChildNodes();

					for(int j = 0, d = list.getLength(); j < d; j++)						
						if(data.item(j).getNodeName() == "#text")
							item0.removeChild(data.item(j));

					if(item0.isEqualNode(element) || item0.isSameNode(element))
						removed = document.getDocumentElement().removeChild(item);
				}
			}

			if(format == Format.ATTRIBUTE) {

				for(int i = 0; i < list.getLength() && removed == null; i++) {

					Element item = (Element) list.item(i);

					if(item.isEqualNode(element) || item.isSameNode(element))
						removed = document.getDocumentElement().removeChild(item);
				}
			}

			result = result + (removed == null ? 0 : 1);
		}

		if(result > 0)
			flush();

		return result;
	}

	@Override
	public boolean deleteByID(I id) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		NodeList list = document.getElementsByTagName(tagName);

		Node removed = null;

		if(list == null || list.getLength() == 0)
			return false;

		if(format == Format.TAG) 

			for(int i = 0; i < list.getLength() && removed == null; i++) {

				Element item = (Element) list.item(i);					
				if(compareIDs(id, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim()))
					removed = document.getDocumentElement().removeChild(item);
			}

		else if(format == Format.ATTRIBUTE)
			for(int i = 0; i < list.getLength() && removed == null; i++) {

				Element item = (Element) list.item(i);
				if(compareIDs(id, item.getAttribute(nameOfTheID)))
					removed = document.getDocumentElement().removeChild(item);
			}		

		boolean result = removed == null ? false : true;

		if(result == true)
			flush();

		return result;	
	}

	@Override
	public int deleteByPattern(String property, String regex) throws Exception {

		check_property_names(property);

		int number_of_deletion = 0;

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return 0;

		if(format == Format.TAG)  

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);
				if(item.getElementsByTagName(property).item(0).getChildNodes().item(0).getNodeValue().trim().matches(regex)) {
					document.removeChild(item);
					number_of_deletion++;
				}
			}

		else if(format == Format.ATTRIBUTE) 

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);
				if(item.getAttribute(nameOfTheID).matches(regex)) {
					document.removeChild(item);
					number_of_deletion++;
				}
			}

		if(number_of_deletion != 0)
			flush();

		return number_of_deletion;
	}

	@Override
	public int deleteByPropertyName(String property, Object value) throws Exception {

		check_property_names(property);

		int number_of_deletion = 0;

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return 0;

		if(format == Format.TAG)  

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);
				if(item.getElementsByTagName(property).item(0).getChildNodes().item(0).getNodeValue().trim().equals(String.valueOf(value))) {
					document.removeChild(item);
					number_of_deletion++;
				}
			}

		if(format == Format.ATTRIBUTE) 

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);
				if(item.getAttribute(nameOfTheID).matches(String.valueOf(value))) {
					document.removeChild(item);
					number_of_deletion++;
				}

			}

		if(number_of_deletion != 0)
			flush();

		return number_of_deletion;
	}

	@Override
	public boolean find(J obj) throws Exception {

		Element element = unmapValuesForXML(obj, document, fields, format);

		NodeList list = document.getElementsByTagName(tagName);

		boolean found = false;

		if(list == null || list.getLength() == 0) 
			return false;

		if(format == Format.TAG) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				Element item0 = item;

				NodeList data = item0.getChildNodes();

				for(int j = 0, d = list.getLength(); j < d; j++)						
					if(data.item(j).getNodeName() == "#text")
						item0.removeChild(data.item(j));

				J obj0 = fromElementToObject(objClass, item0, format);

				if(item0.isEqualNode(element) || item0.isSameNode(element)|| obj == obj0 || compare(obj0, obj))
					found = true;
			}
		}

		if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				J obj0 = fromElementToObject(objClass, item, format);

				if(item.isEqualNode(element) || item.isSameNode(element)|| obj == obj0 || compare(obj0, obj))
					found = true;

			}
		}
		return found;
	}

	/**
	 * Performs the modification on the XML file*/
	private void flush() throws Exception {

		transformer.transform(src, res);

		Path zu = Paths.get(filePath);

		String text = Files.newBufferedReader(zu).lines().collect(Collectors.joining());

		text = text.replaceAll(">([ \\t\\n\\r]+)<", "><");

		Files.newBufferedWriter(zu).append(text).flush();

		formatXML(filePath, filePath);

		text = text.replaceAll("<"+tagName+">", "\n<"+tagName+">");

		Files.newBufferedWriter(zu).append(text).flush();

		formatXML(filePath, filePath);
	}

	@Override
	public List<J> getAll() throws Exception {

		NodeList list = document.getElementsByTagName(tagName);

		Object[] array = new Object[list.getLength()];

		int j = -1;

		if(list == null || list.getLength() == 0) 
			return null;

		if(format == Format.ATTRIBUTE)
			for(int i = 0, c = list.getLength(); i < c; i++) {

				j++;

				Element item = (Element) list.item(i);

				J obj = (J) objClass.getConstructor().newInstance();

				for(Field field : fields)
					mappingField(obj, field, item.getAttribute(field.getName()).trim());

				if(!containsItem(array, obj))
					array[j] = obj;
				else
					throw new DAOException(Message.DUPLICATE_ITEM_ENTRY);
			}

		if(format == Format.TAG)
			for(int i = 0, c = list.getLength(); i < c; i++) {

				j++;

				Element item = (Element) list.item(i);

				J obj = (J) objClass.getConstructor().newInstance();

				for(Field field : fields)
					mappingField(obj, field, item.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());

				if(!containsItem(array, obj))
					array[j] = obj;
				else
					throw new DAOException(Message.DUPLICATE_ITEM_ENTRY);
			}
		
		flush();

		return new RostArrayList<J>(j + 1, array, true);
	}

	@Override
	public J getByID(I id) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		J obj = null;

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0)
			return null;

		if(format == Format.TAG)

			for(int i = 0; i < list.getLength(); i++) {

				Element item = (Element) list.item(i); 		

				if(compareIDs(id, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim())) {

					obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());
					break;
				}
			}

		if(format == Format.ATTRIBUTE) 

			for(int i = 0; i < list.getLength(); i++) {

				Element item = (Element) list.item(i);
				if(compareIDs(id, item.getAttribute(nameOfTheID))) {

					obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getAttribute(field.getName()));

					break;
				}
			}
		return obj;
	}

	@Override
	public List<J> getByPattern(String property, String regex) throws Exception {

		check_property_names(property);

		NodeList nodes = document.getElementsByTagName(tagName);

		int j = -1;

		Object[] array = new Object[nodes.getLength()];

		if(nodes == null || nodes.getLength() == 0) 
			return null;

		if(format == Format.TAG) {

			for(int i = 0, c = nodes.getLength(); i < c; i++) {

				j++;

				Element item = (Element) nodes.item(i);

				if( item.getElementsByTagName(property).item(0).getChildNodes().item(0).getNodeValue().trim().matches(regex) ) {

					J obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());

					array[j] = obj;
				}
			}
		}

		else if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = nodes.getLength(); i < c; i++) {

				j++;

				Element item = (Element) nodes.item(i);

				if( item.getAttribute(property).trim().matches(regex) ) {

					J obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());

					array[j] = obj;
				}
			}
		}
		return new RostArrayList<J>(j + 1, array, true);
	}

	@Override
	public List<J> getByPropertyName(String property, Object value) throws Exception {

		check_property_names(property);

		NodeList nodes = document.getElementsByTagName(tagName); 

		if(nodes == null || nodes.getLength() == 0) 
			return null;

		Object[] array = new Object[nodes.getLength()];

		int j = -1;

		Field field_property = objClass.getDeclaredField(property);

		if(format == Format.TAG) {
			for(int i = 0, c = nodes.getLength(); i < c; i++) {

				j++;

				Element item = (Element) nodes.item(i);

				String value1 = item.getElementsByTagName(property).item(0).getChildNodes().item(0).getNodeValue().trim();
				Object value2 = cast(field_property, value.toString());

				if(cast(field_property, value1) == value2 || cast(field_property,value1) == value || cast(field_property, value1).equals(value2)) {
					J obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getElementsByTagName(field.getName()).item(0).getChildNodes().item(0).getNodeValue().trim());

					array[j] = obj;
				}
			}
		}

		else if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = nodes.getLength(); i < c; i++) {

				j++;

				Element item = (Element) nodes.item(i);

				Object value1 = cast(field_property, item.getAttribute(property));
				Object value2 = cast(field_property, value.toString());

				if( value1.equals(value2) || value1 == value2 || value1 == value ) {
					J obj = (J) objClass.getDeclaredConstructor().newInstance();

					for(Field field : fields)
						mappingField(obj, field, item.getAttribute(field.getName()).trim());

					array[j] = obj;
				}
			}
		}

		return new RostArrayList<J>(j + 1, array, true);
	}

	@Override
	public List<I> getIDs() throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return null;

		Object[] array = new Object[list.getLength()];

		Field field = objClass.getDeclaredField(nameOfTheID);

		int j = -1;

		if(format == Format.ATTRIBUTE) 

			for(int i = 0, c = list.getLength(); i < c; i++) {

				j++;

				I id = (I) cast(field, ((Element) list.item(i)).getAttribute(nameOfTheID));

				if(!containsItem(array, id))
					array[j] = id;

				else 
					throw new DAOException(DUPLICATE_ID_ENTRY);				
			}

		else if(format == Format.TAG)

			for(int i = 0, c = list.getLength(); i < c; i++) {

				j++;

				I id = (I) cast(field, ((Element) list.item(i)).getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim());

				if(!containsItem(array, id))
					array[j] = id;

				else 
					throw new DAOException(DUPLICATE_ID_ENTRY);
			}
		return new RostArrayList<I>(j + 1, array, true);
	}

	@Override
	public <R> List<R> getProperty(Supplier<R> supp, String propertyName) throws Exception {

		check_property_names(propertyName); 

		NodeList list = document.getElementsByTagName(tagName);

		Field field = objClass.getDeclaredField(propertyName);

		if (list == null || list.getLength() == 0) 
			return null;

		List<Node> objs = new ArrayList<>();

		for (int i = 0; i < list.getLength(); i++) 
			objs.add(list.item(i));        

		if (format == null)
			return null;

		List<R> result = objs.stream()
				.map(node -> {
					try {                        
						return format == Format.ATTRIBUTE ? 
								(R) cast(field, ((Element) node).getAttribute(propertyName)) 
								: (R) cast(field, ((Element) node).getElementsByTagName(propertyName).item(0).getChildNodes().item(0).getNodeValue().trim());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				.collect(Collectors.toList()); 

		return new RostArrayList<R>(result.size(), result.toArray(), false);
	}

	@Override
	public boolean update(J oldObj, J newObj) throws Exception {

		Element oldElement = unmapValuesForXML(oldObj, document, fields, format);

		Element newElement = unmapValuesForXML(newObj, document, fields, format);

		if(oldElement.isEqualNode(newElement) || oldElement.isSameNode(newElement))
			throw new DAOException(UPDATE_SAME_OBJECTS);

		if(find(newObj))
			throw new DAOException(ITEM_FOUND);

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return false;

		Node replaced = null;

		if(format == Format.TAG) { 

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				if(nameOfTheID != null) {

					I id_of_new = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(newObj);
					I id_of_old = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(oldObj);

					if(id_of_new != id_of_old && compareIDs(id_of_new, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim()))
						throw new DAOException(ID_FOUND);
				}
			}

			if(format == Format.ATTRIBUTE) {

				for(int i = 0, c = list.getLength(); i < c; i++) {

					Element item = (Element) list.item(i);

					if(nameOfTheID != null) {

						I id_of_new = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(newObj);
						I id_of_old = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(oldObj);

						if(id_of_new != id_of_old && compareIDs(id_of_new, item.getAttribute(nameOfTheID)))
							throw new DAOException(ID_FOUND);
					}
				}
			}
		}

		if(format == Format.TAG) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				Element item0 = item;

				NodeList data = item0.getChildNodes();

				for(int j = 0, d = list.getLength(); j < d; j++)						
					if(data.item(j).getNodeName() == "#text")
						item0.removeChild(data.item(j));

				J obj = fromElementToObject(objClass, item0, format);

				if(item0.isEqualNode(oldElement) || item0.isSameNode(oldElement) || obj == oldObj || compare(obj, oldObj))
					replaced = document.getDocumentElement().replaceChild(newElement, item0);
			}
		}

		else if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				J obj = fromElementToObject(objClass, item, format);

				if(item.isEqualNode(oldElement) || item.isSameNode(oldElement) || obj == oldObj || compare(obj, oldObj))
					replaced = document.getDocumentElement().replaceChild(newElement, item);
			}
		}

		boolean result = replaced == null ? false : true;

		if(result == true)
			flush();

		return result;
	}

	@Override
	public boolean updateByID(I id, J obj) throws Exception {

		check_property_names(nameOfTheID); /* checks if the name of the identifiers is set*/

		Element element = unmapValuesForXML(obj, document, fields, format);

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return false;

		Node replaced = null;

		I id_of_obj = (I) objClass.getDeclaredMethod(getter(objClass.getDeclaredField(nameOfTheID))).invoke(obj);

		if(id_of_obj != id && getByID(id_of_obj) != null)
			throw new DAOException(ID_FOUND);

		if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i);

				if(compareIDs(id, item.getAttribute(nameOfTheID)) == true) {

					if(find(fromElementToObject(objClass, element, format)))
						throw new DAOException(ITEM_FOUND);

					replaced = document.getDocumentElement().replaceChild(element, item);
				}
			}

			if(format == Format.TAG) {

				for(int i = 0, c = list.getLength(); i < c; i++) {

					Element item = (Element) list.item(i);

					if(compareIDs(id, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim())) {

						if(find(fromElementToObject(objClass, element, format)))
							throw new DAOException(ITEM_FOUND);

						replaced = document.getDocumentElement().replaceChild(element, item);
					}
				}
			}
		}

		boolean result = replaced == null ? false : true;

		if(result == true)
			flush();

		return result;
	}

	@Override
	public int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception {

		check_property_names(propertyName);

		NodeList list = document.getElementsByTagName(tagName);

		int number_of_update = 0;

		if(list == null || list.getLength() == 0) 
			return 0;

		if(format == Format.TAG)
			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i), element = item;

				if(item.getElementsByTagName(propertyName).item(0).getChildNodes().item(0).getNodeValue().trim().equals(String.valueOf(oldValue))) {

					element.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).setNodeValue(String.valueOf(newValue));

					if(find(fromElementToObject(objClass, element, format)))
						throw new DAOException(ITEM_FOUND);

					number_of_update += document.getDocumentElement().replaceChild(element, item) == null ? 0 : 1;
				}
			}

		if(format == Format.ATTRIBUTE) 
			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i), element = item;

				if(item.getAttribute(propertyName).equals(String.valueOf(oldValue))) {

					element.setAttribute(propertyName, String.valueOf(newValue));

					if(find(fromElementToObject(objClass, element, format)))
						throw new DAOException(ITEM_FOUND);

					number_of_update = document.getDocumentElement().replaceChild(element, item) == null ? 0 : 1;
				}
			}

		if(number_of_update != 0)
			flush();

		return number_of_update;
	}

	@Override
	public boolean updatePropertyByID(I id, String propertyName, Object newValue) throws Exception {

		check_property_names(nameOfTheID, propertyName);

		if(propertyName.equals(nameOfTheID) && !compareIDs(id, String.valueOf(newValue)) && getIDs().stream().map(String::valueOf).anyMatch(identifier -> identifier.matches(String.valueOf(newValue))))
			throw new DAOException(ID_FOUND);

		NodeList list = document.getElementsByTagName(tagName);

		if(list == null || list.getLength() == 0) 
			return false;

		Node replaced = null;

		if(format == Format.TAG) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i), element = item;

				if(compareIDs(id, item.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).getNodeValue().trim())) {

					element.getElementsByTagName(nameOfTheID).item(0).getChildNodes().item(0).setNodeValue(String.valueOf(newValue));

					if(find(fromElementToObject(objClass, element, format)))
						throw new DAOException(ITEM_FOUND);

					replaced = document.getDocumentElement().replaceChild(element, item);

					break;
				}
			}
		}

		if(format == Format.ATTRIBUTE) {

			for(int i = 0, c = list.getLength(); i < c; i++) {

				Element item = (Element) list.item(i), element = item;

				if(compareIDs(id, item.getAttribute(nameOfTheID))) {

					element.setAttribute(propertyName, String.valueOf(newValue));

					if(find(fromElementToObject(objClass, element, format)))
						throw new DAOException(ITEM_FOUND);

					replaced = document.getDocumentElement().replaceChild(element, item);

					break;
				}
			}
		}

		boolean result = replaced == null ? false : true;

		if(result == true)
			flush();

		return result;
	}
}