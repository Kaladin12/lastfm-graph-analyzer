package kaladin.zwolf.projects.lastfm.graph.analyzer.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChunkIterator<T> implements Iterator<List<T>> {
    private final Iterator<T> iterator;
    private final int chunkSize;

    public ChunkIterator(Iterator<T> iterator, int chunkSize) {
        this.iterator = iterator;
        this.chunkSize = chunkSize;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public List<T> next() {
        List<T> chunk = new ArrayList<>();
        for (int i = 0; i < chunkSize && iterator.hasNext(); i++) {
            chunk.add(iterator.next());
        }
        return chunk;
    }
}
