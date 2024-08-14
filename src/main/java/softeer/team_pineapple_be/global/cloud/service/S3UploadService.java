package softeer.team_pineapple_be.global.cloud.service;

import com.amazonaws.services.s3.AmazonS3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * S3 업로드 서비스
 */
@Service
@RequiredArgsConstructor
public class S3UploadService {
  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  //  /**
  //   * S3에 UUID로 파일 이름 저장하고 그 파일의 URL 반환
  //   *
  //   * @param multipartFile
  //   * @return S3에 저장된 파일 URL
  //   * @throws IOException
  //   */
  //  public String saveFile(MultipartFile multipartFile) throws IOException {
  //    ObjectMetadata metadata = new ObjectMetadata();
  //    metadata.setContentLength(multipartFile.getSize());
  //    metadata.setContentType(multipartFile.getContentType());
  //    String originalFilename = multipartFile.getOriginalFilename();
  //    if (originalFilename == null) {
  //      throw new ValidationException("s3.file_name_null");
  //    }
  //    String extension = extractExtension(originalFilename);
  //    String fileName = UUID.randomUUID() + "." + extension;
  //    amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);
  //    String url = amazonS3.getUrl(bucket, fileName).toString();
  //    imageRepository.save(new Image(url));
  //    return url;
  //  }

  private String extractExtension(String originalFileName) {
    return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
  }
}