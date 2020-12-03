package com.rocketmq.store;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MappedFileTest {

    private final String STORE_MESSAGE = "Once, there was a chance for me!";

    @Test
    public void testSelectMappedBuffer() throws IOException {
        // 文件大小 64kb
        MappedFile mappedFile = new MappedFile("target/unit_test_store/MappedFileTest/000", 1024 * 64);
        boolean result = mappedFile.appendMessage(STORE_MESSAGE.getBytes());
        assertThat(result).isTrue();

        SelectMappedBufferResult selectMappedBufferResult = mappedFile.selectMappedBuffer(0);
        byte[] data = new byte[STORE_MESSAGE.length()];
        selectMappedBufferResult.getByteBuffer().get(data);
        String readString = new String(data, StandardCharsets.UTF_8);
        assertThat(readString).isEqualTo(STORE_MESSAGE);

        mappedFile.shutdown(1000);
        assertThat(mappedFile.isAvailable()).isFalse();
        selectMappedBufferResult.release();
        assertThat(mappedFile.isCleanupOver()).isTrue();
        assertThat(mappedFile.destroy(1000)).isTrue();
    }

    @Test
    public void testAppendBuffer() throws IOException {
        MappedFile mappedFile = new MappedFile("target/unit_test_store/MappedFileTest/000", 1024 * 64);
        boolean result = mappedFile.appendMessage2(STORE_MESSAGE.getBytes());
        assertThat(result).isTrue();
    }

}