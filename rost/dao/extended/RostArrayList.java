/*
 * A new package for the DAO pattern in Java © 2024 by Ramses TALLA is licensed 
 * under CC BY-NC-ND 4.0. To view a copy of this license, 
 * visit https://creativecommons.org/licenses/by-nc-nd/4.0/*/

package rost.dao.extended;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

/**
 * A RostArrayList stands for an immutable list that will hold data read from the data 
 * source. One can not resize it and in other words perform operations like adding, updating 
 * or removing. This list is based on an array in order to increase the speed of the programs 
 * reading data stored in it. It has been designed like the well-known ArrayList of the standard 
 * library. But this class has been especially introduced in order to design the algorithms 
 * according to possible exigencies (e.g the complexity and the speed) coming in the future.
 * 
 *@author Ramsès TALLA
 *
 *@version 1.0
 *
 *@since rostDAO 1.0
 *
 *@see java.util.ArrayList 
 **/
@SuppressWarnings({"unchecked"})
class RostArrayList<E> implements List<E> {

	private final Object[] elements;

	private final int size;

	RostArrayList(int size, Object[] elements, boolean resize) {

		this.size = size;

		if(resize) {

			Object[] elements_tmp = new Object[this.size];	

			int index = 0;

			for(int i = 0; i < elements.length && index != size; i++) {

				Object element = elements[i];

				if(element != null)
					elements_tmp[index++] = element;
			}

			this.elements = elements_tmp;
		}		

		else
			this.elements = elements;

	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {

		return indexOf(o) >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object e : c)
			if (!contains(e))
				return false;
		return true;
	}

	@Override
	public E get(int index) {

		return (E) elements[index];
	}

	@Override
	public int indexOf(Object o) {

		for(int i = 0; i < size; i++)
			try {
				if(elements[i] == o || elements[i].equals(o) || UtilityMethods.compare(elements[i], o))
					return i;
			} catch (Exception e) {
				e.printStackTrace();
			}

		return -1;
	}

	@Override
	public boolean isEmpty() {

		return size == 0;
	}

	@Override
	public Iterator<E> iterator() {

		return new Iterator<>() {

			private int cursor;

			@Override
			public boolean hasNext() {
				return cursor < size;
			}

			@Override
			public E next() {
				if (cursor >= size)
					throw new NoSuchElementException();

				return (E) elements[cursor++];
			}
		};
	}

	@Override
	public int lastIndexOf(Object o) {

		for(int i = size - 1; i >= 0; i--)
			if(elements[i].equals(o) || elements[i] == o)
				return i;

		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {		
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {

		return new ListIterator<>() {

			private int cursor = index;

			@Override
			public boolean hasNext() {
				return cursor < size;
			}

			@Override
			public E next() {

				if (cursor >= size)
					throw new NoSuchElementException();

				cursor = cursor + 1;

				return (E) elements[cursor];
			}

			@Override
			public boolean hasPrevious() {
				return cursor != 0;
			}

			@Override
			public E previous() {

				if (cursor == 0)
					throw new NoSuchElementException();

				cursor = cursor - 1;

				return (E) elements[cursor];
			}

			@Override
			public int nextIndex() {

				return cursor;
			}

			@Override
			public int previousIndex() {
				return cursor - 1;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();				
			}

			@Override
			public void set(E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {

		return size;
	}

	@Override
	public void sort(Comparator<? super E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException("For security reasons, this immutable and immuable list does not support this operation.");
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("For security reasons, this immutable and immuable list does not support this operation.");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("For security reasons, this immutable and immuable list does not support this operation.");
	}

	public String toString() {
		Iterator<E> it = iterator();
		if (! it.hasNext())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;) {
			E e = it.next();
			sb.append(e == this ? "(this Collection)" : e);
			if (! it.hasNext())
				return sb.append(']').toString();
			sb.append(',').append(' ');
		}
	}
}