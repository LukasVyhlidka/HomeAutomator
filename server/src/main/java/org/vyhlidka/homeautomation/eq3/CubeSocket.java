package org.vyhlidka.homeautomation.eq3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

class CubeSocket {

    private static final Logger logger = LoggerFactory.getLogger(CubeSocket.class);

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final PrintWriter writer;

    public CubeSocket(String host, int port) {
        try {
            this.socket = new Socket();
            this.socket.setSoTimeout(5 * 1000);
            this.socket.connect(new InetSocketAddress(host, port), 5 * 1000);
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);

            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.error("Error socket initialization.", e);
            throw new RuntimeException("Error socket initialization");
        }
    }

    public void append(String cmd) {
        this.writer.append(cmd);
        this.writer.flush();
    }

    public String readLine() throws IOException {
        return this.bufferedReader.readLine();
    }

    public void clearReader() throws IOException {
        logger.debug("Going to clear the reader.");
        while (this.bufferedReader.ready()) {
            this.bufferedReader.read();
        }
        logger.debug("Reader cleared.");
    }

    public void close() throws IOException {
        this.bufferedReader.close();
        this.socket.close();

    }

    public boolean isClosed() {
        return this.socket.isClosed();
    }
}
