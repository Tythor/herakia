package tythor.herakia.hazelcast;

import com.github.luben.zstd.Zstd;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tythor.herakia.utility.SpringUtil;

import java.io.IOException;

/**
 * A serializer for Hazelcast that uses Jackson for serialization and Zstd for compression.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class JacksonByteArraySerializer<T> implements ByteArraySerializer<T> {
    protected static final boolean enableLogging = false;
    protected static final ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

    private Class<T> type;

    @Override
    public byte[] write(Object object) throws IOException {
        if (enableLogging) return logWrite(object);

        return Zstd.compress(objectMapper.writeValueAsBytes(object));
    }

    @Override
    public T read(byte[] buffer) throws IOException {
        if (enableLogging) return logRead(buffer);

        if (type != null) {
            return objectMapper.readValue(Zstd.decompress(buffer), type);
        } else {
            return objectMapper.readValue(Zstd.decompress(buffer), new TypeReference<>() {});
        }
    }

    @Override
    public int getTypeId() {
        // Must not collide with other serializer ids
        return type != null ? Math.abs(type.getName().hashCode()) : 42069;
    }

    protected byte[] logWrite(Object object) {
        long startTime = System.nanoTime();

        // Serialize object to JSON bytes
        byte[] originalBytes = objectMapper.writeValueAsBytes(object);
        long serializationTime = System.nanoTime() - startTime;
        startTime = System.nanoTime();

        // Compress JSON bytes using Zstd
        byte[] compressedBytes = Zstd.compress(originalBytes);
        long compressionTime = System.nanoTime() - startTime;

        // Log performance metrics
        double compressionRatio = (double) originalBytes.length / compressedBytes.length;
        log.info("Serialization time: {}ms | Compression time: {}ms | Compression ratio: {} ({}KiB / {}KiB) | {}",
            String.format("%.3f", serializationTime / 1_000_000d),
            String.format("%.3f", compressionTime / 1_000_000d),
            String.format("%.2f", compressionRatio), String.format("%.2f", originalBytes.length / 1024d), String.format("%.2f", compressedBytes.length / 1024d),
            object instanceof String ? object : object.getClass().getName());

        return compressedBytes;
    }

    protected T logRead(byte[] buffer) {
        long startTime = System.nanoTime();

        // Decompress JSON bytes using Zstd
        byte[] decompressedBytes = Zstd.decompress(buffer, (int) Zstd.getFrameContentSize(buffer));
        long decompressionTime = System.nanoTime() - startTime;
        startTime = System.nanoTime();

        // Deserialize JSON bytes to object
        T object = type != null ? objectMapper.readValue(decompressedBytes, type) : objectMapper.readValue(decompressedBytes, new TypeReference<>() {});
        long deserializationTime = System.nanoTime() - startTime;

        // Log performance metrics
        log.info("Deserialization time: {}ms | Decompression time: {}ms | {}",
            String.format("%.3f", decompressionTime / 1_000_000d),
            String.format("%.3f", deserializationTime / 1_000_000d),
            object instanceof String ? object : object.getClass().getName());

        return object;
    }
}
