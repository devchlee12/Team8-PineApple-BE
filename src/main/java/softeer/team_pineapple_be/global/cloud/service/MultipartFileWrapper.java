package softeer.team_pineapple_be.global.cloud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 멀티파트 파일으로 형변환을 해주는 클래스
 */
@RequiredArgsConstructor
public class MultipartFileWrapper implements MultipartFile {
    private final InputStream inputStream;
    private final String fileName;
    private final long size;
    private final String contentType;

    private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();
    static {
        MIME_TYPE_MAP.put("jpg", "image/jpeg");
        MIME_TYPE_MAP.put("jpeg", "image/jpeg");
        MIME_TYPE_MAP.put("png", "image/png");
        MIME_TYPE_MAP.put("gif", "image/gif");
        MIME_TYPE_MAP.put("svg", "image/svg+xml");
    }

    public MultipartFileWrapper(InputStream inputStream, String fileName, long size) {
        this(inputStream, fileName, size, determineContentType(fileName));
    }
    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType; // MIME 타입은 필요에 따라 조정할 수 있습니다.
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    @Override
    public void transferTo(java.io.File file) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    private static String determineContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return MIME_TYPE_MAP.getOrDefault(extension, "application/octet-stream");
    }

    // 파일 확장자를 추출하는 메서드
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex != -1) ? fileName.substring(lastDotIndex + 1) : "";
    }
}
