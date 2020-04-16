package buffer;

import java.io.Reader;

public class Buffer {
    final static int DEFAULT_SIZE_BUFFER = 10;
    final static int END_OF_SOURCE_CODE = -1;

    private Reader reader;
    private int maxIndex = 0;
    private char localBuf[];

    private int indexBuf = 0;
    private boolean isEndSourceCode = false;

    private int row = 1;
    private int col = 0;

    public Buffer(Reader reader, int capacity) {
        if (capacity < 2) {
            throw new IllegalArgumentException("B: емкость должна быть больше 1");
        }

        this.reader = reader;
        localBuf = new char[capacity];

        writeInBuffer();
    }

    public Buffer(Reader reader) {
        this(reader, DEFAULT_SIZE_BUFFER);
    }

    public char getChar() {
        if (isBufferEnded(0)) {
            writeInBuffer();
        }
        col++;
        if(localBuf[indexBuf] == '\n'){
            col = 0;
            row++;
        }
        return localBuf[indexBuf++];
    }

    public int peekChar() {
        if (isBufferEnded(0)) {
            writeInBuffer();
        }
        if (isEndSourceCode) {
            return END_OF_SOURCE_CODE;
        }
        return localBuf[indexBuf];
    }

    public int peekSecondChar() {
        if (isBufferEnded(1)) {
            writeInBuffer();
        }
        if (isEndSourceCode) {
            return END_OF_SOURCE_CODE;
        }
        return localBuf[indexBuf + 1];
    }

    private boolean isBufferEnded(int shift) {
        return (indexBuf + shift == maxIndex);
    }

    private void writeInBuffer() {
        if (isBufferEnded(1) && !isBufferEnded(0)) {
            localBuf[0] = localBuf[indexBuf];
            indexBuf = 1;
        } else {
            indexBuf = 0;
        }
        try {
            int numCharRead = reader.read(localBuf, indexBuf, localBuf.length - indexBuf);
            if (numCharRead == END_OF_SOURCE_CODE) {
                isEndSourceCode = true;
            } else {
                maxIndex = numCharRead + indexBuf;
            }
        } catch (Exception e) {
            System.out.printf((char) 27 + "[31m B: ошибка чтения буфера\n");
            System.exit(0);
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
