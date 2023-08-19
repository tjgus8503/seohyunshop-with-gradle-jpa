package seohyun.app.mall.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageRegister {

    public void CreateImages(MultipartFile[] image) throws IOException {

        List<String> list = new ArrayList<>();

        for (MultipartFile image1 : image) {
            // 1. 파일 저장 경로 설정 : 실제 서비스되는 위치(프로젝트 외부에 저장)
            String uploadPath = "/Users/parkseohyun/project/mall/src/main/java/seohyun/app/mall/imageUpload/";
            // 2. 원본 파일 이름 알아오기
            String originalFileName = image1.getOriginalFilename();
            // 3. 파일 이름 중복되지 않게 이름 변경(서버에 저장할 이름) UUID 사용
            UUID uuid2 = UUID.randomUUID();
            String savedFileName = uuid2.toString() + "_" + originalFileName;
            // 4. 파일 생성
            java.io.File file1 = new File(uploadPath + savedFileName);
            // 5. 서버로 전송
            image1.transferTo(file1);
            // model로 저장
            list.add(uploadPath + savedFileName);
        }
    }
}