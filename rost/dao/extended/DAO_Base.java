/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import rost.dao.base.Sense;

@SuppressWarnings("unchecked")
class DAO_Base<J> implements rost.dao.base.DAO<J> {
	
	private DAO<?, J> dao = null;
	
	DAO_Base(DAO<?, J> dao) throws Exception {		
		this.dao = dao;
	}

	@Override
	public int add(Iterable<J> list) throws Exception {		
		return dao.add(list);
	}

	@Override
	public boolean add(J obj) throws Exception {
		return dao.add(obj);
	}

	@Override
	public int add(J... objs) throws Exception {		
		return dao.add(objs);
	}

	@Override
	public int delete(Iterable<J> list) throws Exception {		
		return dao.delete(list);
	}

	@Override
	public boolean delete(J obj) throws Exception {
		return dao.delete(obj);
	}

	@Override
	public int delete(J... objs) throws Exception {		
		return dao.delete(objs);
	}

	@Override
	public int delete(Predicate<J> t) throws Exception {		
		return dao.delete(t);
	}

	@Override
	public int deleteByPattern(String property, String regex) throws Exception {		
		return dao.deleteByPattern(property, regex);
	}

	@Override
	public int deleteByPropertyName(String property, Object value) throws Exception {		
		return dao.deleteByPropertyName(property, value);
	}

	@Override
	public boolean find(J obj) throws Exception {		
		return dao.find(obj);
	}

	@Override
	public List<J> get(Predicate<J> t) throws Exception {		
		return dao.get(t);
	}

	@Override
	public List<J> getAll() throws Exception {		
		return dao.getAll();
	}

	@Override
	public List<J> getAllOrderBy(String propertyName, Sense sense) throws Exception {		
		return dao.getAllOrderBy(propertyName, sense);
	}

	@Override
	public List<J> getByPattern(String property, String regex) throws Exception {		
		return dao.getByPattern(property, regex);
	}

	@Override
	public List<J> getByPatternOrderBy(String property, String regex, String propertyName, Sense sense)
			throws Exception {		
		return dao.getByPatternOrderBy(property, regex, propertyName, sense);
	}

	@Override
	public List<J> getByPropertyName(String property, Object value) throws Exception {		
		return dao.getByPropertyName(property, value);
	}

	@Override
	public List<J> getByPropertyNameOrderBy(String property, Object value, String propertyName, Sense sense)
			throws Exception {		
		return dao.getByPropertyNameOrderBy(property, value, propertyName, sense);
	}

	@Override
	public <R> List<R> getProperty(Supplier<R> supp, String propertyName) throws Exception {
		return dao.getProperty(supp, propertyName);
	}

	@Override
	public int set(String[] propertynames, Object[] values, Predicate<J> predicate) throws Exception {
		return dao.set(propertynames, values, predicate);
	}

	@Override
	public boolean update(J oldObj, J newObj) throws Exception {		
		return dao.update(oldObj, newObj);
	}

	@Override
	public int updateProperty(String propertyName, Object oldValue, Object newValue) throws Exception {		
		return dao.updateProperty(propertyName, oldValue, newValue);
	}
}