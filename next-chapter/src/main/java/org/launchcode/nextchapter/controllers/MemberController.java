package org.launchcode.nextchapter.controllers;

import jakarta.servlet.http.HttpSession;
import org.launchcode.nextchapter.data.BlogRepository;
import org.launchcode.nextchapter.data.ClubRepository;
import org.launchcode.nextchapter.data.MemberRepository;
import org.launchcode.nextchapter.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("members")
public class MemberController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private BlogRepository blogRepository;

    public Member getUserFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("user");
        if (userId == null) {
            return null;
        }
        Optional<Member> user = memberRepository.findById(userId);
        if (user.isEmpty()) {
            return null;
        }
        return user.get();
    }

    @GetMapping("home")
    public String displayMemberInfo(Model model, HttpSession session) {

        model.addAttribute("title", "Member Profile");
        model.addAttribute("clubs", clubRepository.findAll());
        model.addAttribute("member", getUserFromSession(session));


        Integer userId = (Integer) session.getAttribute("user");
        Optional<Member> currentUser = memberRepository.findById(userId);

        if (currentUser.isEmpty()) {
            return "redirect:/";
        }
        Member member = currentUser.get();

        return "members/index";
    }


}
