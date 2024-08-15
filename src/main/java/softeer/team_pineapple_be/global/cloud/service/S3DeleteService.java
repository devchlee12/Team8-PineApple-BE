package softeer.team_pineapple_be.global.cloud.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class S3DeleteService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void deleteFolder(String folderName) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(folderName);
        ListObjectsV2Result result;

        do {
            result = amazonS3.listObjectsV2(request);
            List<S3ObjectSummary> objects = result.getObjectSummaries();

            if (!objects.isEmpty()) {
                List<String> keysToDelete = new ArrayList<>();
                for (S3ObjectSummary object : objects) {
                    keysToDelete.add(object.getKey());
                }

                // 객체 삭제 요청
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                        .withKeys(keysToDelete.toArray(new String[0]));
                amazonS3.deleteObjects(deleteObjectsRequest);
            }

            // 다음 페이지가 있는지 확인
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
    }
}
