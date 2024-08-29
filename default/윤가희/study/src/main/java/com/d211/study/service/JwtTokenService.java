package com.d211.study.service;

import com.d211.study.config.jwt.JwtToken;
import com.d211.study.config.jwt.JwtTokenProvider;
import com.d211.study.domain.Member;
import com.d211.study.exception.auth.AuthenticationFailedException;
import com.d211.study.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
    private final MemberRepository memberRepository;

    // JWT
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // accessToken, refreshToken 발급
    @Transactional
    public JwtToken generateToken(String userName, String password) {
        // userName를 기반으로 Authentication 객체 생성
        Authentication authentication = authenticate(userName, password);
        // 인증 정보를 기반으로 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
        // 리프레시 토큰 저장
        saveRefreshToken(userName, jwtToken.getRefreshToken());

        return jwtToken;
    }

    private Authentication authenticate(String userName, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, password);
            return authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            log.error("사용자 인증 실패: " + e.getMessage());
            throw new AuthenticationFailedException("사용자 인증에 실패했습니다.", e);
        }
    }

    // refreshToken 업데이트
    @Transactional
    public void saveRefreshToken(String userName, String newRefreshToken) {
        Member user = memberRepository.findByMemberEmail(userName)
                .orElseThrow(() -> new EntityNotFoundException("해당 userName으로 사용자를 찾을 수 없습니다."));

        // 기존 객체의 리프레시 토큰만 업데이트
        user.setMemberRefreshToken(newRefreshToken);

        // 업데이트한 회원 저장
        memberRepository.save(user);
    }

    // refreshToken 조회
    public String getRefreshToken(String userName) {
        return memberRepository.findMemberRefreshTokenByMemberEmail(userName)
                .orElseThrow(() -> new EntityNotFoundException("해당 refreshToken으로 사용자를 찾을 수 없습니다."));
    }
}
