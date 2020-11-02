import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

interface HashFunction {
    public int hash(String data);
}

class FNVHash1 implements HashFunction {
    @Override
    public int hash(final String data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < data.length(); i++) {
            hash = (hash ^ data.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return Math.abs(hash);
    }
}

class ConsistantHash {
    private final HashFunction hashFunction;
    private final int virtualNodeNum; 
    private final SortedMap<Integer, String> hashCircle = new TreeMap<>();

    ConsistantHash(final List<String> nodes, final int virtualNodeNum, final HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        this.virtualNodeNum = virtualNodeNum;

        for (final String node : nodes) {
            add(node);
        }
    }

    public void add(final String node) {
        hashCircle.put(hashFunction.hash(node), node);
        for (int i = 0; i < virtualNodeNum; i++) {
            hashCircle.put(hashFunction.hash(node + i), node);
        }
    }

    public void remove(final String node) {
        hashCircle.remove(hashFunction.hash(node));
        for (int i = 0; i < virtualNodeNum; i++) {
            hashCircle.remove(hashFunction.hash(node + i));
        }
    }

    public String get(final String key) {
        if (hashCircle.isEmpty()) {
            return null;
        }

        int hash = hashFunction.hash(key);
        if (!hashCircle.containsKey(hash)) {
            final SortedMap<Integer, String> tailMap = hashCircle.tailMap(hash);
            hash = tailMap.isEmpty() ? hashCircle.firstKey() : tailMap.firstKey();
        }
        return hashCircle.get(hash);
    }
}

public class ConsistantHashTest {
    public static void main(final String[] args) {
        final int NODE_NUM = 10;
        final int VIRTUAL_NODE_NUM = 200;
        final int USER_KEY_NUM = 100_0000;
        
        // init server node
        final List<String> nodes = new ArrayList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            nodes.add("server-" + i);
        }
        final HashFunction fnvHash = new FNVHash1();
        final ConsistantHash hashCircle = new ConsistantHash(nodes, VIRTUAL_NODE_NUM, fnvHash);

        // init user keys
        final List<String> keys = new ArrayList<>();
        for (int i = 0; i < USER_KEY_NUM; i++) {
            keys.add("user-key-" + i);
        }

        // search server node by user key then record result 
        final SortedMap<String, Integer> result = new TreeMap<>();
        for (final String key : keys) {
            final String node = hashCircle.get(key);
            result.put(node, result.getOrDefault(node, 0) + 1);
            // System.out.println(node + ":" + key);
        }

        // calculate standard deviation and print
        double sum = 0;
        double standardDeviation = 0;
        final int average = USER_KEY_NUM / NODE_NUM;
        
        for (final Map.Entry<String, Integer> entry : result.entrySet()) {
            final String node = entry.getKey();
            final Integer numberOfKeysPerNode = entry.getValue();

            System.out.println(node + " -> " + numberOfKeysPerNode);
            sum += Math.pow( Math.abs(average - numberOfKeysPerNode), 2);
        }

        standardDeviation = Math.sqrt(sum / NODE_NUM);
        System.out.println("Standard Deviation: " + standardDeviation);
    }
}