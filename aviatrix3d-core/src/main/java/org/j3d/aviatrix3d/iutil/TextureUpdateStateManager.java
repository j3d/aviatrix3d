/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.iutil;

// External imports
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.j3d.util.I18nManager;

// Local imports
// None

/**
 * Class that handles all of the update state messaging in a cross between
 * a listener and a traditional HashMap.
 * <p>
 *
 * Since the state manager manages update requests keyed by GL context, if an
 * update is recieved before the context has been registered with the map, then
 * they will disappear. This is particularly important if you are attempting to
 * make texture updates prior to the first frame rendering or if an update is
 * called on the first call of the app update observer.
 * <p>
 * <b>Internationalisation Resource Names</b>
 * <ul>
 * <li>illegalCapacityMsg: Error message when constructor capacity < 0</li>
 * <li>illegalLoadFactorMsg: Error message when constructor loadFactor <= 0</li>
 * <li>invalidUpdateStrategyMsg: The texture update strategy is not valid</li>
 * </ul>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class TextureUpdateStateManager implements SubTextureUpdateListener
{
    /** Message when the initial capacity is negative */
    private static final String BAD_CAPACITY_PROP =
        "org.j3d.aviatrix3d.management.TextureUpdateStateManager.illegalCapacityMsg";

    /** Message when the load factor is less than or equal to zero */
    private static final String BAD_LOAD_FACTOR_PROP =
        "org.j3d.aviatrix3d.management.TextureUpdateStateManager.illegalLoadFactorMsg";

    /** Message when the update strategy selected is invalid */
    private static final String BAD_STRATEGY_TYPE_PROP =
        "org.j3d.aviatrix3d.management.TextureUpdateStateManager.invalidUpdateStrategyMsg";

    /** Increment size for the pending update list */
    private static final int PENDING_LIST_INC = 10;

    /**
     * All sub image updates should be buffered until the next chance to
     * update. Best for when you are only updating small sections of the
     * screen.
     */
    public static final int UPDATE_BUFFER_ALL = 1;

    /**
     * Each update should discard any previous updates recieved. Best used
     * when you know you'll only be updating one area, overwritting any earlier
     * updates - For example video texturing.
     */
    public static final int UPDATE_BUFFER_LAST = 2;

    /**
     * Each update should check to see whether the area of this update overlaps
     * completely that of any other buffered updates. Useful if you're doing
     * scattered region updates where some parts may overlap but others don't.
     */
    public static final int UPDATE_DISCARD_OVERWRITES = 3;



    /** The hash table data. */
    private transient Entry[] table;

    /** The total number of entries in the hash table. */
    private transient int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     */
    private int threshold;

    /** The load factor for the hashtable. */
    private float loadFactor;

    /** Cache of the entry instances to prevent excessive object creation */
    private List<Entry> entryCache;

    /** Collection of update objects cached when not being used. */
    private List<TextureUpdateData> updateObjectCache;

    /** Set the texture format to use. Uses GL constants */
    private int format;

    /** The update strategy to use for sub-image updates. Defaults to keep latest */
    private int updateStrategy;

    /**
     * If an update is received and we don't yet have any GL entries as keys
     * then keep the pending updates here.
     */
    private List<TextureUpdateData> unassignedUpdates;

    /**
     * Innerclass that acts as a datastructure to create a new entry in the
     * table.
     */
    private static class Entry
    {
        /** The stored hash key of the object */
        int hash;

        /** Collection of current buffer items */
        TextureUpdateData[] updatesPending;

        /** Number of updates pending */
        int numUpdatesPending;

        /** Next item in this hash bucket */
        Entry next;
    }

    /**
     * Create a new state manager using the given strategy.
     *
     * @param strategy one of the UPDATE_ identifiers
     */
    public TextureUpdateStateManager(int strategy)
    {
        this(strategy, 2, 0.9f);
    }

    /**
     * Create a new instance of the updater.
     *
     * @param strategy one of the UPDATE_ identifiers
     * @param initialCapacity the initial capacity of the hashtable.
     * @param loadFactor the load factor of the underlying hashtable.
     */
    public TextureUpdateStateManager(int strategy,
                                     int initialCapacity,
                                     float loadFactor)
    {
        if(initialCapacity < 0)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg_pattern = intl_mgr.getString(BAD_CAPACITY_PROP);

			Locale lcl = intl_mgr.getFoundLocale();

			NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

			Object[] msg_args = { initialCapacity };
			Format[] fmts = { n_fmt };
			MessageFormat msg_fmt =
				new MessageFormat(msg_pattern, lcl);
			msg_fmt.setFormats(fmts);
			String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        if(loadFactor <= 0)
		{
			I18nManager intl_mgr = I18nManager.getManager();
			String msg_pattern = intl_mgr.getString(BAD_LOAD_FACTOR_PROP);

			Locale lcl = intl_mgr.getFoundLocale();

			NumberFormat n_fmt = NumberFormat.getNumberInstance(lcl);

			Object[] msg_args = { initialCapacity };
			Format[] fmts = { n_fmt };
			MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
			msg_fmt.setFormats(fmts);
			String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
		}

        checkUpdateStrategyType(strategy);

        if(initialCapacity == 0)
        {
            initialCapacity = 1;
        }

        updateStrategy = strategy;
        this.loadFactor = loadFactor;

        table = new Entry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);

        entryCache = new ArrayList<>(initialCapacity);
        updateObjectCache = new ArrayList<>(initialCapacity);
        unassignedUpdates = new ArrayList<>(initialCapacity);
    }


    //---------------------------------------------------------------
    // Methods defined by SubTextureUpdateListener
    //---------------------------------------------------------------

    @Override
    public void textureUpdated(int x,
                               int y,
                               int z,
                               int width,
                               int height,
                               int depth,
                               int level,
                               byte[] pixels)
    {
        if(isEmpty())
        {
            TextureUpdateData tud = getNewUpdate();
            tud.x = x;
            tud.y = y;
            tud.width = width;
            tud.height = height;
            tud.level = level;
            tud.depth = depth;
            tud.format = format;

            copyPixels(tud, pixels);

            unassignedUpdates.add(tud);
        }
        else
        {
            switch (updateStrategy)
            {
                case UPDATE_BUFFER_ALL:
                    updateAppend(x, y, width, height, depth, level, pixels);
                    break;

                case UPDATE_BUFFER_LAST:
                    updateReplace(x, y, width, height, depth, level, pixels);
                    break;

                case UPDATE_DISCARD_OVERWRITES:
                    updateOverlap(x, y, width, height, depth, level, pixels);
                    break;
            }
        }
    }

    //---------------------------------------------------------------
    // Local methods
    //---------------------------------------------------------------

    /**
     * Returns the number of contexts current registered in this manager.
     *
     * @return A value >= 0 for the number of valid contexts
     */
    public int size()
    {
        return count;
    }

    /**
     * Check to see if if this manager contains no current contexts registered.
     *
     * @return true if this manager has size() == 0, false otherwise
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * Set the new texture format to use. The format needs be one of the
     * GL.GL_* values, not one of the aviatrix values. This is passed directly
     * to the glSubTexImage() call.
     *
     * @param f The format type to use
     */
    public void setTextureFormat(int f)
    {
        format = f;
    }

    /**
     * Change the update strategy to use.
     *
     * @param strategy The new update type
     */
    public void setUpdateStrategy(int strategy)
    {
        checkUpdateStrategyType(strategy);
        updateStrategy = strategy;
    }

    /**
     * Returns the number of updates that are pending for a given context.
     *
     * @param gl The GL context to fetch the update array for
     * @return A number >= 0
     */
    public int getNumUpdatesPending(GL gl)
    {
        Entry tab[] = table;
        int hash = gl.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index] ; e != null ; e = e.next)
        {
            if(e.hash == hash)
            {
                return e.numUpdatesPending;
            }
        }

        return 0;
    }

    /**
     * Returns an array holding all the pending updates for the given GL
     * context and resets the number pending back to 0. The array is the
     * internal list and should be treated as read-only data. If the context
     * is unknown to this map, it will return null. The array should not
     * contain any data and the length is unimportant. Use {@link #getNumUpdatesPending(GL)}
     * to find out how many valid elements are in this array.
     *
     * @param gl The GL context to fetch the update array for
     * @return An array to read updates from, or null
     */
    public TextureUpdateData[] getUpdatesAndClear(GL gl)
    {
        Entry[] tab = table;
        int hash = gl.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index]; e != null; e = e.next)
        {
            if(e.hash == hash)
            {
                e.numUpdatesPending = 0;
                return e.updatesPending;
            }
        }

        return null;
    }

    /**
     * Register a new context instance with this manager.
     *
     * @param gl The GL context to remove as a key
     */
    public void addContext(GL gl)
    {
        // Makes sure the key is not already in the hashtable.
        Entry[] tab = table;
        int hash = gl.hashCode();;
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for(Entry e = tab[index]; e != null; e = e.next)
        {
            if(e.hash == hash)
            {
                return;
            }
        }

        if(count >= threshold)
        {
            // Rehash the table if the threshold is exceeded
            rehash();

            tab = table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }


        // Creates the new entry and initialise everything if needed.
        Entry e = getNewEntry();
        e.hash = hash;
        e.numUpdatesPending = 0;
        e.next = tab[index];

        if(e.updatesPending == null)
        {
            if(unassignedUpdates.isEmpty())
            {
                e.updatesPending = new TextureUpdateData[1];
            }
            else
            {
                int initial_size = unassignedUpdates.size();

                e.updatesPending = new TextureUpdateData[initial_size];

                unassignedUpdates.toArray(e.updatesPending);
                e.numUpdatesPending = initial_size;

                unassignedUpdates.clear();
            }
        }

        tab[index] = e;
        count++;
    }

    /**
     * Removes the context (and its corresponding updates) from the manager.
     * This method does nothing if the key is not registered.
     *
     * @param gl The GL context to remove as a key
     */
    public void removeContext(GL gl)
    {
        Entry[] tab = table;
        int hash = gl.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for(Entry e = tab[index], prev = null; e != null; prev = e, e = e.next)
        {
            if(e.hash == hash)
            {
                if(prev != null)
                {
                    prev.next = e.next;
                }
                else
                {
                    tab[index] = e.next;
                }

                count--;
                releaseEntry(e);
            }
        }
    }

    /**
     * Clears this manager so that it contains no keys.
     */
    public void clear()
    {
        if(count == 0)
            return;

        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            if(e == null)
                continue;

            while(e.next != null)
            {
                releaseEntry(e);

                Entry n = e.next;
                e.next = null;
                e = n;
            }

            tab[index] = null;
        }

        count = 0;
    }

    /**
     * Convenience method to empty the current pending updates list and drop
     * the values into the cache.
     */
    public void clearPendingUpdates()
    {
        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            while(e != null)
            {
                for(int i = 0; i < e.numUpdatesPending; i++)
                {
                    releaseUpdate(e.updatesPending[i]);
                    e.updatesPending[i] = null;
                }

                e.numUpdatesPending = 0;

                e = e.next;
            }
        }
    }

    /**
     * Increases the capacity of and internally reorganizes this
     * hashtable, in order to accommodate and access its entries more
     * efficiently.  This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     */
    private void rehash()
    {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        threshold = (int)(newCapacity * loadFactor);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;)
        {
            for (Entry old = oldMap[i] ; old != null ; )
            {
                Entry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * Process a replace mode update.
     *
     * @param x The start location x coordinate in texel space
     * @param y The start location y coordinate in texel space
     * @param width The width of the update in texel space
     * @param height The height of the update in texel space
     * @param level The mipmap level that changed
     * @param pixels Buffer of the data that has updated
     */
    private void updateReplace(int x,
                               int y,
                               int width,
                               int height,
                               int depth,
                               int level,
                               byte[] pixels)
    {
        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            while(e != null)
            {
                TextureUpdateData tud;

                if(e.updatesPending[0] != null)
                {
                    tud = e.updatesPending[0];
                }
                else
                {
                    tud = getNewUpdate();
                    e.updatesPending[0] = tud;
                }

                tud.x = x;
                tud.y = y;
                tud.width = width;
                tud.height = height;
                tud.depth = depth;
                tud.level = level;
                tud.format = format;

                copyPixels(tud, pixels);

                e.numUpdatesPending = 1;
                e = e.next;
            }
        }
    }

    /**
     * Process the update looking for where it would complete obscure other
     * updates already queued. If it finds them, replace or delete the
     * existing update.
     *
     * @param x The start location x coordinate in texel space
     * @param y The start location y coordinate in texel space
     * @param width The width of the update in texel space
     * @param height The height of the update in texel space
     * @param level The mipmap level that changed
     * @param pixels Buffer of the data that has updated
     */
    private void updateOverlap(int x,
                               int y,
                               int width,
                               int height,
                               int depth,
                               int level,
                               byte[] pixels)
    {
        // run through all the known updates looking for an something this is
        // bigger than.
        boolean update_found = false;

        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            while(e != null)
            {
                for(int i = 0; i < e.numUpdatesPending; i++)
                {
                    TextureUpdateData tud = e.updatesPending[i];

                    if((level == tud.level) &&
                       (x <= tud.x) && (y <= tud.y) &&
                       (x + width >= tud.x + tud.width)  &&
                       (y + height >= tud.y + tud.height))
                    {
                        // if we've found one overlap already, then we've got a
                        // big area covered. Just remove anything after the
                        // first as this update is going to overwrite them all.
                        if(update_found)
                        {
                            System.arraycopy(e.updatesPending,
                                             i + 1,
                                             e.updatesPending,
                                             i,
                                             e.numUpdatesPending - i - 1);
                            e.numUpdatesPending--;
                        }
                        else
                        {
                            tud.x = x;
                            tud.y = y;
                            tud.width = width;
                            tud.height = height;
                            tud.level = level;
                            tud.depth = depth;
                            tud.format = format;
                            update_found = true;

                            copyPixels(tud, pixels);
                        }
                    }
                }

                // If we didn't find any overlaps, just append it to the list
                // for this entry. May not always be the same for every entry
                // though, so don't use the generic updateAppend().
                if(!update_found)
                {
                    checkUpdateListSize(e);
                    TextureUpdateData tud;

                    if(e.updatesPending[e.numUpdatesPending] == null)
                    {
                        tud = getNewUpdate();
                        e.updatesPending[e.numUpdatesPending] = tud;
                    }
                    else
                    {
                        tud = e.updatesPending[e.numUpdatesPending];
                    }

                    e.numUpdatesPending++;
                    tud.x = x;
                    tud.y = y;
                    tud.width = width;
                    tud.height = height;
                    tud.depth = depth;
                    tud.level = level;
                    tud.format = format;

                    copyPixels(tud, pixels);

                    e = e.next;
                }
            }
        }
    }

    /**
     * Append the details onto the end of the list of updates.
     *
     * @param x The start location x coordinate in texel space
     * @param y The start location y coordinate in texel space
     * @param width The width of the update in texel space
     * @param height The height of the update in texel space
     * @param level The mipmap level that changed
     * @param pixels Buffer of the data that has updated
     */
    private void updateAppend(int x,
                              int y,
                              int width,
                              int height,
                              int depth,
                              int level,
                              byte[] pixels)
    {
        Entry[] tab = table;
        for(int index = tab.length; --index >= 0; )
        {
            Entry e = tab[index];

            while(e != null)
            {
                TextureUpdateData tud;
                checkUpdateListSize(e);

                if(e.updatesPending[e.numUpdatesPending] == null)
                {
                    tud = getNewUpdate();
                    e.updatesPending[e.numUpdatesPending] = tud;
                }
                else
                {
                    tud = e.updatesPending[e.numUpdatesPending];
                }

                e.numUpdatesPending++;
                tud.x = x;
                tud.y = y;
                tud.width = width;
                tud.height = height;
                tud.level = level;
                tud.depth = depth;
                tud.format = format;

                copyPixels(tud, pixels);

                e = e.next;
            }
        }
    }

    /**
     * Check and update the given pixel buffer size with the new data.
     *
     * @param tud The data holder to use as the source
     * @param pixels The data to be copied in
     */
    private void copyPixels(TextureUpdateData tud, byte[] pixels)
    {
        int req_size = tud.width * tud.height * bytesPerPixel();
        if((tud.pixels == null) || req_size > tud.pixels.capacity())
        {
            tud.pixels = ByteBuffer.allocateDirect(req_size);
            tud.pixels.order(ByteOrder.nativeOrder());
        }

        tud.pixels.clear();
        tud.pixels.put(pixels, 0, req_size);
    }

    /**
     * Convenience method that looks at the user provided image format and
     * returns the number of bytes per pixel
     *
     * @return A value between 1 and 4
     */
    private int bytesPerPixel()
    {
        int ret_val = 4;

        switch(format)
        {
            case GL.GL_RGB:
            case GL2.GL_BGR:
                ret_val = 3;
                break;

            case GL.GL_RGBA:
            case GL.GL_BGRA:
                ret_val = 4;
                break;

            case GL.GL_LUMINANCE_ALPHA:
                ret_val = 2;
                break;

            case GL.GL_LUMINANCE:
            case GL.GL_ALPHA:
            case GL2.GL_INTENSITY:
                ret_val = 1;
        }

        return ret_val;
    }

    /**
     * Grab a new entry. Check the cache first to see if one is available. If
     * not, create a new instance.
     *
     * @return An instance of the Entry
     */
    private Entry getNewEntry()
    {
        Entry ret_val;

        int size = entryCache.size();
        if(size == 0)
            ret_val = new Entry();
        else
            ret_val = entryCache.remove(size - 1);

        return ret_val;
    }

    /**
     * Release an entry back into the cache.
     *
     * @param e The entry to put into the cache
     */
    private void releaseEntry(Entry e)
    {
        e.numUpdatesPending = 0;
        entryCache.add(e);
    }

    /**
     * Grab a new entry. Check the cache first to see if one is available. If
     * not, create a new instance.
     *
     * @return An instance of the Entry
     */
    private TextureUpdateData getNewUpdate()
    {
        TextureUpdateData ret_val;

        int size = updateObjectCache.size();
        if(size == 0)
            ret_val = new TextureUpdateData();
        else
            ret_val = updateObjectCache.remove(size - 1);

        return ret_val;
    }

    /**
     * Release an entry back into the cache.
     *
     * @param tud The entry to put into the cache
     */
    private void releaseUpdate(TextureUpdateData tud)
    {
        updateObjectCache.add(tud);
    }

    /**
     * Check the pending updates list size and increase if needed.
     *
     * @param e The entry to check for enough size
     */
    private void checkUpdateListSize(Entry e)
    {
        if(e.numUpdatesPending < e.updatesPending.length)
            return;

        int old_size = e.updatesPending.length;
        int new_size = old_size + PENDING_LIST_INC;

        TextureUpdateData[] tmp = new TextureUpdateData[new_size];

        System.arraycopy(e.updatesPending, 0, tmp, 0, old_size);

        e.updatesPending = tmp;
    }

    /**
     * Internal convenience method that checks the validity of the update strategy
     * type passed in.
     *
     * @param type The type value to check
     * @throws IllegalArgumentException The strategy is not a recognised type
     */
    private void checkUpdateStrategyType(int type)
    {
        if(type != UPDATE_BUFFER_ALL && type != UPDATE_BUFFER_LAST && type != UPDATE_DISCARD_OVERWRITES)
        {
            I18nManager intl_mgr = I18nManager.getManager();
            String msg_pattern = intl_mgr.getString(BAD_STRATEGY_TYPE_PROP);

            Locale lcl = intl_mgr.getFoundLocale();

            Object[] msg_args = { type };
            MessageFormat msg_fmt = new MessageFormat(msg_pattern, lcl);
            String msg = msg_fmt.format(msg_args);

            throw new IllegalArgumentException(msg);
        }
    }
}
