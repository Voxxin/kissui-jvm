package cat.ella.kissui.util;

import cat.ella.kissui.KissUI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record IOUtility() {
    public static final Class<?> caller = KissUI.class;

    public static InputStream getResourceStream(String resourcePath) throws FileNotFoundException {
        InputStream stream = getResourceStreamNullable(resourcePath);
        if (stream == null) {
            throw new FileNotFoundException(
                    "Resource " + resourcePath + " not found " +
                            "(check your Properties, and make sure the file " +
                            "is in the resources folder/on classpath; or the URL is valid)"
            );
        }
        return stream;
    }

    public static InputStream getResourceStreamNullable(String resourcePath) {
        if (!resourcePath.contains(":/")) {
            InputStream stream = caller.getResourceAsStream(resourcePath);
            if (stream == null) {
                stream = caller.getResourceAsStream("/" + resourcePath);
            }
            return stream;
        } else {
            try {
                URL url = new URI(resourcePath).toURL();
                if (url.getProtocol().contains("http")) {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setUseCaches(true);
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (KissUI)");
                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(5000);
                    connection.setDoOutput(true);
                    return connection.getInputStream();
                } else {
                    return url.openStream();
                }
            } catch (Exception e) {
                KissUI.LOGGER.error("Failed to get resource " + resourcePath, e);
                return null;
            }
        }
    }

    public static boolean resourceExists(String resourcePath) {
        try (InputStream is = getResourceStreamNullable(resourcePath)) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static ByteBuffer toDirectByteBuffer(byte[] bytes) {
        return (ByteBuffer) ByteBuffer.allocateDirect(bytes.length)
                .order(ByteOrder.nativeOrder())
                .put(bytes)
                .flip();
    }

    public static ByteBuffer toDirectByteBufferNT(byte[] bytes) {
        return (ByteBuffer) ByteBuffer.allocateDirect(bytes.length + 1)
                .order(ByteOrder.nativeOrder())
                .put(bytes)
                .put((byte) 0)
                .flip();
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        input.close();
        return bytes;
    }



}
