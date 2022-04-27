package com.itdevcloud.japp.core.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CachedServletInputStream extends ServletInputStream {
	private static final Logger logger = LogManager.getLogger(CachedServletInputStream.class);

    private InputStream cachedInputStream;

    public CachedServletInputStream(byte[] cachedRequestBody) {
        this.cachedInputStream = new ByteArrayInputStream(cachedRequestBody);
    }

    @Override
    public boolean isFinished() {
        try {
            return cachedInputStream.available() == 0;
        } catch (IOException e) {
             e.printStackTrace();
             logger.error(e);
        }
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        return cachedInputStream.read();
    }

}