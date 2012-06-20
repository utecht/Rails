package rails.game.state;

/**
 * Change associated with HashMapState
 */
final class HashMapChange<K,V> extends Change {

    final private HashMapState<K,V> state;
    final private K key;
    final private V newValue;
    final private boolean remove;
    final private V oldValue;
    final private boolean existed;

    /**
     * Put element into map
     */
    HashMapChange(HashMapState<K,V> state, K key, V value) {
        super(state);
        this.state = state;
        this.key = key;
        newValue = value;
        remove = false;
        oldValue = state.get(key);
        existed = state.containsKey(key);
    }

    /**
     * Remove element from map
     */
    HashMapChange(HashMapState<K,V> state, K key) {
        super(state);
        this.state = state;
        this.key = key;
        newValue = null;
        remove = true;
        oldValue = state.get(key);
        existed = true;
    }

    @Override
    public void execute() {
        state.change(key, newValue, remove);
    }

    @Override
    public void undo() {
        state.change(key, oldValue, !existed);
    }

    @Override
    public HashMapState<K,V> getState() {
        return state;
    }

    @Override
    public String toString() {
        return "HashMapChange for " + state.getId() + ": key =  " + key + " newValue =  " + newValue +  " oldValue = " + oldValue + " remove " + remove + " existed =  " + existed;
    }

}
