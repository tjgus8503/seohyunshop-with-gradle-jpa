package seohyun.app.mall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seohyun.app.mall.models.ProductInquiries;
import seohyun.app.mall.models.Users;
import seohyun.app.mall.service.ProductQService;
import seohyun.app.mall.service.UsersService;
import seohyun.app.mall.utils.Jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/productq")
public class ProductQController {
    private final ProductQService productQService;
    private final UsersService usersService;
    private final Jwt jwt;

    // 상품 문의 등록
    // 일반 유저 등록 가능. (role = 1)
    @PostMapping("/createproductq")
    public ResponseEntity<Object> createProductQ(
            @RequestHeader String xauth, @RequestBody ProductInquiries productInquiries) throws Exception {
        try{
            Map<String, String> map = new HashMap<>();

            String decoded = jwt.VerifyToken(xauth);

            Users findUserId = usersService.findUserId(decoded);
            if (findUserId.getRole() == 1) {

                UUID uuid = UUID.randomUUID();
                productInquiries.setId(uuid.toString());

                productInquiries.setUserId(decoded);

                productQService.createProductQ(productInquiries);
                map.put("result", "success 등록이 완료되었습니다.");
            } else {
                map.put("result", "failed 등록 권한이 없습니다.");
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e){
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    // 상품 문의 수정
    // 본인만 수정 가능
    @PostMapping("/updateproductq")
    public ResponseEntity<Object> updateProductQ(
            @RequestHeader String xauth, @RequestBody ProductInquiries productInquiries) throws Exception {
        try{
            Map<String, String> map = new HashMap<>();

            String decoded = jwt.VerifyToken(xauth);

            Users findUserId = usersService.findUserId(decoded);
            if (findUserId.getRole() == 1) {
                productInquiries.setUserId(decoded);
                productQService.updateProductQ(productInquiries);
                map.put("result", "success 수정이 완료되었습니다.");
            } else {
                map.put("result", "failed 수정 권한이 없습니다.");
            }
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e){
            Map<String, String> map = new HashMap<>();
            map.put("error", e.toString());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
    }

    // 상품 문의 삭제
    // 일반 유저만 삭제 가능(본인)
}