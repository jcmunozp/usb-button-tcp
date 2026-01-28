package com.jcmp.usbbutton.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public class MessageCodec {
    private final ObjectMapper mapper = new ObjectMapper();
    public synchronized void writeJson(OutputStream out, Map<String,Object> msg) throws IOException {
        byte[] data = mapper.writeValueAsBytes(msg);
        ByteBuffer len = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(data.length);
        out.write(len.array()); out.write(data); out.flush();
    }
    @SuppressWarnings("unchecked")
    public Map<String,Object> readJson(InputStream in) throws IOException {
        byte[] hdr = in.readNBytes(4); if (hdr.length < 4) return null;
        int len = ByteBuffer.wrap(hdr).order(ByteOrder.BIG_ENDIAN).getInt();
        if (len <= 0 || len > 10_000_000) throw new IOException("Frame length invalid: " + len);
        byte[] payload = in.readNBytes(len); if (payload.length < len) return null;
        return new ObjectMapper().readValue(payload, Map.class);
    }
}
