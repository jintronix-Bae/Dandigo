package Dandigo.SOS_map.global.security;

import Dandigo.SOS_map.domain.user.entity.Member;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(("회원을 찾을 수 없습니다.")));
        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .authorities(member.getRole().name())
                .build();
    }
}
