package practice;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

    private long count;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            count++;
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = super.read(b);
        if (n > 0) {
            count += n;
        }
        return n;
    }

    /** The number of bytes the reads have returned so far. */
    public long getCount() {
        return count;
    }
}
