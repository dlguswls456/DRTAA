package com.d211.drtaa.user.controller;


import com.d211.drtaa.config.jwt.JwtToken;
import com.d211.drtaa.exception.user.UserCreationException;
import com.d211.drtaa.user.dto.request.FormLoginRequestDTO;
import com.d211.drtaa.user.dto.request.SignUpRequestDTO;
import com.d211.drtaa.user.dto.response.UserInfoResponseDTO;
import com.d211.drtaa.user.service.CustomUserDetailsServiceImpl;
import com.d211.drtaa.user.service.UserService;
import com.d211.drtaa.user.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 컨트롤러", description = "회원관련 기능 수행")
public class UserController {

    private final CustomUserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "Form 회원가입")
    public ResponseEntity signUp(@RequestBody SignUpRequestDTO request) {
        try {
            userDetailsService.createUser(request);

            // 200, 클라이언트 요청 성공
            return ResponseEntity.ok("회원가입 성공");
        } catch (DataIntegrityViolationException e) {
            // 409, 리소스 간의 충돌이 발생했을 때
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (UserCreationException e) {
            // 400, 잘못된 요청에 대한 예외 처리
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 500, 서버 오류가 발생했을 때
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "Form 로그인")
    public ResponseEntity login(@RequestBody FormLoginRequestDTO request) {
        try {
            JwtToken tokens = userService.login(request);

            // 200, 클라이언트 요청 성공
            return ResponseEntity.ok(tokens);
        } catch(UsernameNotFoundException e) {
            // 404, 클라이언트 요청 탐색 실패
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 400, 잘못된 요청
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/info")
    @Operation(summary = "회원 정보 조회", description = "액세스 토큰을 사용해 회원 정보 조회")
    public ResponseEntity info(Authentication authentication) {
        try {
            UserInfoResponseDTO response = userService.info(authentication.getName());

            // 200, 클라이언트 요청 성공
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            // 404, 클라이언트 요청 탐색 실패
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 400, 잘못된 요청
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
