package softeer.team_pineapple_be.global.cloud.service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import softeer.team_pineapple_be.domain.admin.exception.AdminErrorCode;
import softeer.team_pineapple_be.global.exception.RestApiException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * S3 업로드 서비스
 */
@Service
@RequiredArgsConstructor
public class S3UploadService {
  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

    /**
     * S3에 UUID로 파일 이름 저장하고 그 파일의 URL 반환
     *
     * @param multipartFile
     * @return S3에 저장된 파일 URL
     * @throws IOException
     */
  public String saveFile(MultipartFile multipartFile, String fileName) throws IOException {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(multipartFile.getSize());
    metadata.setContentType(multipartFile.getContentType());
    String originalFilename = multipartFile.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    fileName = fileName + UUID.randomUUID() + "." + extension ;
    amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
    String url = amazonS3.getUrl(bucket, fileName).toString();
    return url;
  }

  /**
   * 파일의 생성자를 추출하는 메서드
   * @param originalFileName 원본 파일명
   * @return 추출된 파일 확장자
   */
  private String extractExtension(String originalFileName) {
    return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
  }

  /**
   * ZIP 파일을 처리하여 각 파일을 S3에 업로드하고 주어진 프로세서를 통해 추가 작업을 수행
   *
   * @param file      업로드된 ZIP 파일
   * @param fileName  S3에 저장될 파일 경로
   * @param processor 파일 업로드 후 추가 작업을 수행할 프로세서
   * @throws RestApiException 파일 처리 중 오류가 발생할 경우 발생
   */
  public void processZipFile(MultipartFile file, String fileName, FileProcessor processor) {
    try (InputStream inputStream = file.getInputStream();
         ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.getName().startsWith("__MACOSX")) { // 집 파일 생성시 만들어지는 타입 제외
          zipInputStream.closeEntry();
          continue;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }

        InputStream fileInputStream = new ByteArrayInputStream(baos.toByteArray());
        MultipartFile multipartFile = new MultipartFileWrapper(fileInputStream, entry.getName(), entry.getSize());
        String fileUrl = saveFile(multipartFile, fileName);

        processor.process(multipartFile, fileUrl);
        zipInputStream.closeEntry();
      }

    } catch (IOException e) {
      throw new RestApiException(AdminErrorCode.SAVE_FAILURE);
    }
  }

  /**
   * 파일 업로드 후 추가 작업을 수행하는 함수형 인터페이스
   */
  @FunctionalInterface
  public interface FileProcessor {
    void process(MultipartFile multipartFile, String fileUrl);
  }

  /**
   * 업로드된 파일이 ZIP 형식인지 검증
   *
   * @param file 업로드된 파일
   * @throws RestApiException 파일 형식이 ZIP이 아닌 경우 발생
   */
  public void validateZipFile(MultipartFile file) {
    if (!file.getOriginalFilename().endsWith(".zip")) {
      throw new RestApiException(AdminErrorCode.NOT_ZIP_FILE);
    }
  }
}