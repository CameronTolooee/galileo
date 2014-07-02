/*
Copyright (c) 2014, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
 */

package galileo.dht.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simple implementation of a fixed size array
 * 
 * @author Cameron
 * 
 * @param <T>
 */
public class FixedBuffer<T> implements Iterable<T> {

    // TODO: extract buffer size from available memory
    public final int BUFFER_SIZE = 30;
    private LinkedList<T> buffer;
    private int capacity;
    private int size;

    public FixedBuffer() {
        this.buffer = new LinkedList<T>();
        this.capacity = BUFFER_SIZE;
        this.size = 0;
    }

    public FixedBuffer(int capacity) {
        this.buffer = new LinkedList<T>();
        this.capacity = capacity;
        this.size = 0;
    }

    /**
     * Append an item to the buffer. Returns false if the buffer is full.
     * 
     * @param item
     *            The item to add to the buffer
     * @return False if the buffer is full, otherwise true
     */
    public boolean add(T item) {
        boolean result = true;
        if (size >= capacity) {
            result = false;
        } else {
            ++size;
            if (!buffer.contains(item)) {
                buffer.add(item);
            }
        }
        return result;
    }

    /**
     * Append a collections of items to the buffer. Returns false if the buffer
     * is full or if inserting all the items results in a buffer overflow. In
     * the case of an overflow no items are added to the buffer.
     * 
     * @param item
     *            The item to add to the buffer
     * @return False if the buffer is full, otherwise true if ALL the items are
     *         successfully added
     */
    public boolean add(Collection<T> items) {
        boolean result = true;
        if (size + items.size() >= capacity) {
            result = false;
        } else {
            for (T item : items) {
                add(item);
            }
        }
        return result;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public void clear() {
        buffer.clear();
        size = 0;
    }

    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public String toString() {
        String str = "{ ";
        for (int i = 0; i < buffer.size() - 1; ++i) {
            str += buffer.get(i) + ", ";
        }
        if (size > 0) {
            str += buffer.getLast() + " }";
        } else {
            str += " }";
        }
        return str;
    }

    @Override
    public Iterator<T> iterator() {
        return buffer.iterator();
    }
}
