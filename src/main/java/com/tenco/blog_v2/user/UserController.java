package com.tenco.blog_v2.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Slf4j
@Controller
public class UserController {

    private final UserRepository userRepository;
    private final HttpSession session;

    @PostMapping("/user/update")
    public String update(@ModelAttribute(name = "updateDTO") UserDTO.UpdateDTO reqDTO) {
        // 세션에서 로그인한 사용자 정보 가져오기
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 세션이 없으면 로그인 페이지로 이동
        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 유효성 검사 생략 x
        if (reqDTO.getPassword() == null || reqDTO.getPassword().trim().isEmpty()) {
            return "redirect:/";
        }
        if (reqDTO.getEmail() == null || reqDTO.getEmail().trim().isEmpty()) {
            return "redirect:/";
        }

        // 사용자 정보 가져오기 (세션 아이디 사용)
        // 조회한 엔티티에 정보 수정
        User updatedUser = userRepository.updateById(sessionUser.getId(), reqDTO.getPassword(), reqDTO.getEmail());

        // 세션 정보 동기화 처리
            session.setAttribute("sessionUser", updatedUser);
        return "redirect:/";
    }

    /**
     * 회원 정보 수정 페이지 요청
     * 주소 설계 - http://localhost:8080/user/update-form
     * @return 문자열
     * 반환되는 문자열을 뷰 리졸버가 처리하며,
     * mustache 템플릿 엔진을 통해서 뷰 파일을 렌더링 한다.
     */
    @GetMapping("/user/update-form")
    public String updateForm(HttpServletRequest request) {
        log.info("회원 정보 수정 페이지");

        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        User user = userRepository.findById(sessionUser.getId());
        request.setAttribute("user", user);

        return "user/update-form";
    }
            /**
             * 회원가입 기능 요청
             * @param reqDto
             * @return
             */
    @PostMapping("/join")
    public String join(@ModelAttribute(name = "joinDTO") UserDTO.JoinDTO reqDto) {
        userRepository.save(reqDto.toEntity());
        return "redirect:/login-form";
    }

    /**
     * 자원의 요청은 GET 방식이지만, 보안의 이유로 예외처리 !
     * 로그인 처리 메서드
     * 요청 주소 (POST) - http://localhost:8080/login
     * @param reqDto
     * @return
     */
    @PostMapping("/login")
    public String login(UserDTO.LoginDTO reqDto) {
        try {
            User sessionUser = userRepository.findByUsernameAndPassword(reqDto.getUsername(), reqDto.getPassword());
            session.setAttribute("sessionUser", sessionUser);
            return "redirect:/";
        } catch (Exception e) {
            // 로그인 실패
            return "redirect:/login-form?error";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 세션을 무효화 시킨다. (로그아웃)
        return "redirect:/";
    }

    /**
     * 회원가입 페이지 요청
     * 주소 설계 - http://localhost:8080/join-form
     * @param model
     * @return 문자열
     * 반환되는 문자열을 뷰 리졸버가 처리하며,
     * mustache 템플릿 엔진을 통해서 뷰 파일을 렌더링 한다.
     */
    @GetMapping("/join-form")
    public String joinForm(Model model) {
        log.info("회원가입 페이지");
        model.addAttribute("name", "회원가입 페이지");
        return "user/join-form"; // 템플릿 경로 - user/join-form.mustache
    }

    /**
     * 로그인 페이지 요청
     * 주소 설계 - http://localhost:8080/login-form
     * @param model
     * @return 문자열
     * 반환되는 문자열을 뷰 리졸버가 처리하며,
     * mustache 템플릿 엔진을 통해서 뷰 파일을 렌더링 한다.
     */
    @GetMapping("/login-form")
    public String loginForm(Model model) {
        log.info("로그인 페이지");
        model.addAttribute("name", "로그인 페이지");
        return "user/login-form";
    }





}
