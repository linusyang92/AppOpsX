package com.zzzmode.adblib;


import com.cgutman.adblib.AdbStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Stack;

/**
 * Created by zl on 2017/2/16.
 */

public class LineReader implements Closeable {

    private static final int CR = 13;
    private static final int LF = 10;

    private static final int EOF = -1;


    private AdbStream adbStream;
    private String charsetName = "US-ASCII";

    public LineReader(AdbStream adbStream, String charsetName) {
        this.adbStream = adbStream;
        this.charsetName = charsetName;
    }

    public LineReader(AdbStream adbStream) {
        this.adbStream = adbStream;
    }


    private ByteBuffer byteBuffer = ByteBuffer.allocate(8192);

    private Stack<String> remainLines = new Stack<>();

    public String readLine() throws IOException, InterruptedException {

        while (!adbStream.isClosed()) {
            if (!remainLines.isEmpty()) {
                return remainLines.pop();
            }

            byte[] read = adbStream.read();

            if (read != null) {

                int startPos = 0;
                for (int i = 0; i < read.length; i++) {
                    if (read[i] == LF || read[i] == CR) {
                        //\n \r
                        int count = i - startPos;
                        if (count > 0) {

                            byteBuffer.put(read, startPos, count);
                            byteBuffer.flip();

                            remainLines.push(new String(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(),
                                    byteBuffer.remaining(), charsetName));
                            byteBuffer.clear();
                        }
                        startPos = i + 1;
                    }
                }
                int r = read.length - startPos;
                if (r > 0) {
                    byteBuffer.put(read, startPos, r);
                } else {
                    byteBuffer.clear();
                }
            }

            if (!remainLines.isEmpty()) {
                return remainLines.pop();
            }

        }
        return null;
    }


    @Override
    public void close() throws IOException {
        adbStream.close();
    }
}
