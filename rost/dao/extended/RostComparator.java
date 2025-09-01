/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import static rost.dao.extended.UtilityMethods.getter;

import java.util.Comparator;

import rost.dao.base.Sense;

class RostComparator<T> implements Comparator<T> {

	private String propertyName;

	private Sense sense;

	public RostComparator(String propertyName, Sense sense) {

		this.propertyName = propertyName;

		this.sense = sense;

	}

	@Override
	public int compare(T o1, T o2) {

		try {
			Class<? extends Object> objClass = o1.getClass();

			String field_name = getter(objClass.getDeclaredField(propertyName));

			String param1 = String.valueOf(objClass.getDeclaredMethod(field_name).invoke(o1));
			String param2 = String.valueOf(objClass.getDeclaredMethod(field_name).invoke(o2));

			if(sense == Sense.ASC)
				return param1.compareTo(param2);

			else if(sense == Sense.DESC)
				return param2.compareTo(param1);

			else
				return 0;
		} catch(Exception e) {
			return 0;
		}
	}
}