package kaladin.zwolf.projects.lastfm.graph.analyzer.util;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public Stream<List<T>> stream() {
        return StreamSupport.stream(Spliterators
                .spliteratorUnknownSize(this, Spliterator.ORDERED), false);
    }
}
