package com.tenco.blog_v2.board;

import com.tenco.blog_v2.BoardNativeRepository;
import com.tenco.blog_v2.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class BoardController {

    // 네이티브 쿼리 연습
    private final BoardNativeRepository boardNativeRepository;
    // JPA API, JPQL
    private final BoardRepository boardRepository;
    private final HttpSession session;

    // 게시글 수정 화면 요청
    // board/{id}/update
    @GetMapping("/board/{id}/update-form")
    public String updateForm(@PathVariable(name = "id") Integer id, HttpServletRequest request) {
        // 1. 게시글 조회
        Board board = boardRepository.findById(id);
        // 2. 요청 속성에 조회한 게시글 속성 및 값 추가
        request.setAttribute("board", board);
        // 뷰 리졸브 - 템플릿 반환
        return "board/update-form"; // 폴더 찾아가는 경로 src/main/resources/templates/board/update-form.mustache
    }

    // 게시글 수정 기능 요청
    // board/{id}/update
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable(name = "id") Integer id, @ModelAttribute BoardDTO.UpdateDTO reqDto) {

        // 1. 데이터 바인딩 방식 수정
        // 2. 인증 검사 - 로그인 여부 판단
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 3. 권한 체크 - 본인이 작성한 게시글이 맞는지 확인
        Board board = boardRepository.findById(id);
        if (board == null) {
            return "redirect:/error-404";
        }
        if (!board.getUser().getId().equals(sessionUser.getId())) {
            return "redirect:/error-403"; // 권한이 없습니다. 추후 수정
        }
        // 4. 유효성 검사

        // 5. 서비스 측으로 위임 (직접 구현) - 레파지토리 사용
        boardRepository.updateByIdJPA(id, reqDto.getTitle(), reqDto.getContent());
        // 6. 리다이렉트 처리
        return "redirect:/board/" + id;
    }



    // 주소설계 http://localhost:8080/board/10/delete ( form 태그 활용이기 때문에 delete 선언 )
    // form 태그에서는 GET, POST 방식만 지원하기 때문.
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable(name = "id") Integer id) {
        // 유효성, 인증 검사
        // 세션에서 로그인 사용자 정보 가져오기 -> 인증검사(로그인 여부), 인가(권한 확인 - 내 글)
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 권한 확인
        Board board = boardRepository.findById(id);
        if (board == null) {
            return "redirect:/error-404";
        }
        if ( ! board.getUser().getId().equals(sessionUser.getId())) {
            return "redirect:/error-403"; // 추후 수정
        }

            // 작성자가 맞으면 삭제 수행
            boardRepository.deleteById(id);
        return "redirect:/";
}

// 특정 게시글 요청 화면
// 주소설계 http://localhost:8080/board/10
@GetMapping("/board/{id}")
public String detail(@PathVariable(name = "id") Integer id, HttpServletRequest request) {
        // JPA API 사용
        // Board board = boardRepository.findById(id);

        // JPQL FETCH join 사용
        Board board = boardRepository.findByIdJoinUser(id);
        request.setAttribute("board", board);
        return "board/detail";
    }


    @GetMapping("/")
    public String index(Model model) {

        //List<Board> boardList = boardNativeRepository.findAll();
        //코드 수정
        List<Board> boardList = boardRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "index";
    }

    // 주소설계 http://localhost:8080/board/save-form
    // 게시글 작성 화면
    @GetMapping("/board/save-form")
    public String saveForm() {
        return "board/save-form";
    }

    // 게시글 저장
    // 주소설계 http://localhost:8080/board/save-form
    @PostMapping("/board/save")
    public String save(@ModelAttribute BoardDTO.SaveDTO reqDto) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 파라미터가 올바르게 전달 되었는지 확인
        log.warn("save 실행: 제목={}, 내용{}", reqDto.getTitle(), reqDto.getContent());

        //boardNativeRepository.save(title, content);
        // SaveDTO 에서 toEntity 사용해서 Board 엔티티로 변환하고 인수 값으로 User 정보를 넣음
        boardRepository.save(reqDto.toEntity(sessionUser));
        return "redirect:/";
    }








}

