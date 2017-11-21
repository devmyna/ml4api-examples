package jp.co.test.ml.util.vectorize;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * @author Hiroki Ono
 */
public class IndexMap extends Int2IntOpenHashMap {

	private static final long serialVersionUID = 8182996670634508235L;

	public IndexMap() {
		super();
	}

	public IndexMap(final int expected) {
		super(expected);
	}

	/**
	 * @param m a type-specific map to be copied into the new hash map.
	 */
	public IndexMap(final Int2IntMap m) {
		super(m);
	}
	
	/**
	 * @param hashCode
	 * @param index
	 */
	public void putFeatureIndex(int hashCode, int index) {
		this.put(hashCode, index + 1);
	}

	/**
	 * @param hashCode
	 * @return
	 */
	public int getFeatureIndex(int hashCode) {
		return this.get(hashCode) - 1;
	}

}
