/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * DrFTPD is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DrFTPD; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.drftpd.dynamicdata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;

/**
 * Implements Weak referenced Map for javabeans support.
 * 
 * @author djb61
 * @version $Id$
 */
public class WeakKeyedMap<K extends Key<?>, V> extends WeakHashMap<K,V> {

	public WeakKeyedMap() {
		super();
	}

	public WeakKeyedMap(Map<? extends K,? extends V> map) {
		super(map);
	}

	public synchronized Map<K,V> getAllObjects() {
		return Collections.unmodifiableMap(this);
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(Key<T> key) throws KeyNotFoundException {
		T ret = (T)get(key);
		if (ret == null) {
			throw new KeyNotFoundException();
		}
		return ret;
	}

	public <T> T getObject(Key<T> key, T def) {
		try {
			return getObject(key);
		} catch (KeyNotFoundException e) {
			return def;
		}
	}

	public boolean getObjectBoolean(Key<Boolean> key) {
		return getObject(key,false);
	}

	public Float getObjectFloat(Key<Float> key) {
		return getObject(key, 0F);
	}

	public Integer getObjectInteger(Key<Integer> key) {
		return getObject(key, 0);
	}

	public Long getObjectLong(Key<Long> key) {
		return getObject(key, 0L);
	}

	public String getObjectString(Key<String> key) {
		return getObject(key, "");
	}

	public void incrementInt(Key<Integer> key) {
		incrementInt(key, 1);
	}
	
	public void incrementInt(Key<Integer> key, int amount) {
		synchronized (this) {
			Integer i = getObject(key, 0);

			setObject(key, i + amount);
		}
	}

	public void incrementLong(Key<Long> key) {
		incrementLong(key, 1L);
	}

	public void incrementLong(Key<Long> key, long amount) {
		synchronized (this) {
			Long l = getObject(key, 0L);

			setObject(key, l + amount);
		}
	}

	public void setAllObjects(KeyedMap<? extends K,? extends V> m) {
		putAll(m.getAllObjects());
	}

	@SuppressWarnings("unchecked")
	public <T> void setObject(Key<T> key, T obj) {
        if (obj == null) {
            throw new NullPointerException(key + " - is null");
        }

		put((K) key, (V) obj);
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(Object value) {
		return super.containsValue(value);
	}

	@Override
	public synchronized V get(Object key) {
		return super.get(key);
	}

	@Override
	public synchronized V put(K key, V value) {
		if (key != null) {	
			return super.put(key,value);
		}
		return null;		
	}

	@Override
	public synchronized V remove(Object key) {
		return super.remove(key);
	}

	@Override
	public synchronized void putAll(Map<? extends K,? extends V> map) {
		super.putAll(map);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized Set<K> keySet() {
		return Collections.synchronizedSet(super.keySet());
	}

	@Override
	public synchronized Set<Entry<K,V>> entrySet() {
		return Collections.synchronizedSet(super.entrySet());
	}

	@Override
	public synchronized Collection<V> values() {
		return Collections.synchronizedCollection(super.values());
	}

	@Override
	public synchronized boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}
}