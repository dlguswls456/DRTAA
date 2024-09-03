package com.d211.drtaa.domain.user.service;

import com.d211.drtaa.domain.user.dto.request.FormLoginRequestDTO;
import com.d211.drtaa.domain.user.dto.request.SocialLoginRequestDTO;
import com.d211.drtaa.domain.user.dto.response.UserInfoResponseDTO;
import com.d211.drtaa.global.config.jwt.JwtToken;
import com.d211.drtaa.domain.user.entity.User;
import com.d211.drtaa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public JwtToken FormLogin(FormLoginRequestDTO request) {
        // 사용자 검증
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserProviderId());
        
        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches(request.getUserPassword(), userDetails.getPassword())) 
            throw new BadCredentialsException("유효하지 않은 비밀번호입니다.");

        // JWT 토큰 발급
        JwtToken tokens = jwtTokenService.generateToken(request.getUserProviderId(), request.getUserPassword());

        return tokens;
    }

    @Override
    public JwtToken SocialLogin(SocialLoginRequestDTO request) {
        // 사용자 검증
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserProviderId());

        // 비밀번호 매칭 확인
        if (!passwordEncoder.matches("", userDetails.getPassword()))
            throw new BadCredentialsException("유효하지 않은 비밀번호입니다.");

        // JWT 토큰 발급
        JwtToken tokens = jwtTokenService.generateToken(request.getUserProviderId(), "");

        return tokens;
    }

    @Override
    public UserInfoResponseDTO info(String userProviderId) {
        // 사용자 찾기
        User user = userRepository.findByUserProviderId(userProviderId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 userProviderId의 맞는 회원을 찾을 수 없습니다."));

        // 필요한 정보만 UserInfoResponseDTO로 편집해 반환
        UserInfoResponseDTO userInfo = UserInfoResponseDTO.builder()
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .userNickname(user.getUserNickname())
                .userLogin(user.getUserLogin())
                .userIsAdmin(user.isUserIsAdmin())
                .build();

        return userInfo;
    }

    @Override
    public void delete(String userEmail) {
        userRepository.deleteByUserEmail(userEmail);
    }
}
