package seohyun.app.mall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seohyun.app.mall.models.Products;
import seohyun.app.mall.models.Users;
import seohyun.app.mall.service.ProductsService;
import seohyun.app.mall.service.UsersService;
import seohyun.app.mall.utils.Jwt;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/products")
public class ProductsController {
    private final ProductsService productsService;
    private final UsersService usersService;
    private final Jwt jwt;

    // 상품 등록
    // role = 2 만 상품 등록 가능.
    @PostMapping("/createproduct")
    public ResponseEntity<Object> createProduct(
            @RequestHeader String xauth, @ModelAttribute Products products,
            @RequestPart(required = false) MultipartFile[] image
    ) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();
            String decoded = jwt.VerifyToken(xauth);

            Users findUserId = usersService.findUserId(decoded);
            if (findUserId.getRole() != 2) {
                map.put("result", "failed 등록 권한이 없습니다.");
                return new ResponseEntity<>(map, HttpStatus.OK);
            }

                UUID uuid = UUID.randomUUID();
                products.setId(uuid.toString());
                products.setUserId(decoded);

                if (image != null) {
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
                        File file1 = new File(uploadPath + savedFileName);
                        // 5. 서버로 전송
                        image1.transferTo(file1);
                        // model로 저장

                        list.add(uploadPath + savedFileName);
                    }
                    String multiImages = String.join(",", list);
                    products.setImageUrl(multiImages);

                } else {
                    products.setImageUrl(null);
                }
                productsService.createProduct(products);
                map.put("result", "success 등록이 완료되었습니다.");

            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }


    // 상품 전체 조회
    // 페이지 단위 조회. 기본값(page = 0, limit = 10) 설정.
    @GetMapping("/getall")
    public ResponseEntity<Object> getAll(
            @RequestParam(value = "page", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10") Integer pageSize
    ) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();
            Page<Products> productsList = productsService.getAll(pageNumber, pageSize);
            return new ResponseEntity<>(productsList, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    // 상품 단일 조회
    @GetMapping("/getbyid")
    public ResponseEntity<Object> getById(
            @RequestParam String id
    ) throws Exception {
        try {

            Map<String, Object> product = productsService.getProductWithCount(id);
            return new ResponseEntity<>(product, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    // 상품 수정
    // 상품을 등록한 사람 or 관리자 (role = 3)만 수정 가능.
    @PostMapping("/updateproduct")
    public ResponseEntity<Object> updateProduct(
            @RequestHeader String xauth, @ModelAttribute Products products,
            @RequestPart(required = false) MultipartFile[] image) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();
            String decoded = jwt.VerifyToken(xauth);
            Users findUserId = usersService.findUserId(decoded);
            Products getById = productsService.getById(products.getId());

            if (!(decoded.equals(getById.getUserId())|| findUserId.getRole() == 3)) {
                map.put("result", "failed 수정 권한이 없습니다.");
                return new ResponseEntity<>(map, HttpStatus.OK);
            }
            // 기존 이미지를 변수에 담아둔`다.
            String priorImage = getById.getImageUrl();
            products.setUserId(decoded);
            if (image != null) {
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
                    File file1 = new File(uploadPath + savedFileName);
                    // 5. 서버로 전송
                    image1.transferTo(file1);
                    // model로 저장
                    list.add(uploadPath + savedFileName);
                }
                String multiImages = String.join(",", list);
                products.setImageUrl(multiImages);
            }
            productsService.updateProduct(products);
            map.put("result", "success 수정이 완료되었습니다.");
            // 기존 파일은 삭제
            if (priorImage != null) {
                List<String> test1 = List.of(priorImage.split(","));
                for (String file : test1) {
                    Files.delete(Path.of(file));
                }
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
            } catch(Exception e){
                Map<String, String> map = new HashMap<>();
                map.put("error", e.toString());
                return new ResponseEntity<>(map, HttpStatus.OK);
            }
        }

    // 상품 삭제
    // 상품을 등록한 사람 or 관리자 (role = 3)만 삭제 가능.
    @PostMapping("/deleteproduct")
    public ResponseEntity<Object> deleteProduct(
            @RequestHeader String xauth, @RequestBody Products products) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();

            String decoded = jwt.VerifyToken(xauth);

            Users findUserId = usersService.findUserId(decoded);
            Products getById = productsService.getById(products.getId());
            if (findUserId.getRole() == 3 || getById.getUserId().equals(decoded)) {

                // 등록되어있던 이미지 url 변수에 저장.
                String priorImage = getById.getImageUrl();

                productsService.deleteProduct(products);
                map.put("result", "success 삭제가 완료되었습니다.");

                // 서버에서 등록되어있던 이미지 삭제
                List<String> test1 = List.of(priorImage.split(","));
                for (String file : test1) {
                    Files.delete(Path.of(file));
                }
            } else {
                map.put("result", "failed 삭제 권한이 없습니다.");
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    // 상품 조회 (카테고리 별)
    @GetMapping("/getproductsbycate")
    public ResponseEntity<Object> getProductsByCate(
            @RequestParam Integer cateId) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();

            List<Products> getProductsByCate = productsService.getProductsByCate(cateId);
            return new ResponseEntity<>(getProductsByCate, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

//    @PostMapping("/updatestock")
//    public ResponseEntity<Object> updateStock(
//            @RequestHeader String xauth, @RequestBody List<Products> products
//    ) throws Exception {
//        try{
//            Map<String, String> map = new HashMap<>();
//
//            String decoded = jwt.VerifyToken(xauth);
//            Users findUserId = usersService.findUserId(decoded);
//            if (findUserId.getRole() != 3) {
//                map.put("result", "failed 수정 권한이 없습니다.");
//                return new ResponseEntity<>(map, HttpStatus.OK);
//            }
//            for (Products product : products) {
//                productsService.updateStock(product.getId(), product.getStock());
//            }
//            map.put("result", "success 수정이 완료되었습니다.");
//            return new ResponseEntity<>(map, HttpStatus.OK);
//            }
//        catch (Exception e){
//            Map<String, String> map = new HashMap<>();
//            map.put("error", e.toString());
//            return new ResponseEntity<>(map, HttpStatus.OK);
//        }
//
//    }



    // 상품 이미지 삭제
    @PostMapping("/deleteimage")
    public ResponseEntity<Object> deleteImage(
            @RequestHeader String xauth, @ModelAttribute Products products,
            @RequestPart(required = false) MultipartFile image
    ) throws Exception {
        try {
            Map<String, String> map = new HashMap<>();
            String decoded = jwt.VerifyToken(xauth);

            Users findUserId = usersService.findUserId(decoded);
            Products getById = productsService.getById(products.getId());
            if (findUserId.getRole() == 3 || getById.getUserId().equals(decoded)) {
                products.setUserId(decoded);

                Products findById = productsService.getById(products.getId());
                if (findById.getImageUrl() != null) {

                    Path filePath = Paths.get(findById.getImageUrl());
                    Files.delete(filePath);

                    productsService.updateProduct(products);
                    map.put("result", "success 수정이 완료되었습니다.");
                } else {
                    map.put("result", "failed 삭제할 이미지가 없습니다.");
                }
            } else {
                map.put("result", "failed 수정 권한이 없습니다.");
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
            } catch(Exception e){
                Map<String, String> map = new HashMap<>();
                map.put("error", e.toString());
                return new ResponseEntity<>(map, HttpStatus.OK);
            }

        }

        // 상품 이미지 여러장 삭제
        @PostMapping("/deleteproduct2")
        public ResponseEntity<Object> deleteProduct2(
                @RequestHeader String xauth, @RequestBody Map<String, String> req
                ) throws Exception {
            try {
                Map<String, String> map = new HashMap<>();

                String decoded = jwt.VerifyToken(xauth);

                Users findUserId = usersService.findUserId(decoded);
                Products getById = productsService.getById(req.get("id"));

                if (findUserId.getRole() == 3 || getById.getUserId().equals(decoded)) {

                    Products findById = productsService.getById(req.get("id"));
                    if (findById.getImageUrl() != null) {

                        List<String> test = List.of(findById.getImageUrl().split(","));
                        for (String file : test) {
                            Files.delete(Path.of(file));
                        }
                        findById.setImageUrl(null);
                        productsService.updateProduct(findById);
                        map.put("result", "삭제가 완료되었습니다.");
                    } else {
                        map.put("result", "failed 삭제할 이미지가 없습니다.");
                    }
                } else {
                    map.put("result", "failed 삭제 권한이 없습니다.");
                }
                return new ResponseEntity<>(map, HttpStatus.OK);
            } catch (Exception e) {
                Map<String, String> map = new HashMap<>();
                map.put("error", e.toString());
                return new ResponseEntity<>(map, HttpStatus.OK);
            }
        }

}