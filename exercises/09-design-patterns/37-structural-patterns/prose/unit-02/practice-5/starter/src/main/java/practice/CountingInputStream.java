package practice;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("write read()");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException("write read(byte[], int, int)");
    }

    /** The number of bytes the reads have returned so far. */
    public long getCount() {
        throw new UnsupportedOperationException("write getCount");
    }
}
