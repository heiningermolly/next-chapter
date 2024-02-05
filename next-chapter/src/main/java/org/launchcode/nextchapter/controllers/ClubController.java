package org.launchcode.nextchapter.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.launchcode.nextchapter.data.ClubRepository;
import org.launchcode.nextchapter.data.MemberRepository;
import org.launchcode.nextchapter.models.Club;
import org.launchcode.nextchapter.models.Member;
import org.launchcode.nextchapter.models.dto.ClubMemberDTO;
import org.launchcode.nextchapter.models.dto.CreateClubFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("clubs")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    MemberRepository memberRepository;

    @GetMapping
    public String displayClubInfo(Model model) {
        model.addAttribute("title", "Club Info");
        model.addAttribute("club", "INSERT CLUB INFO HERE");
        return "clubs/index";
    }


    @GetMapping("create")
    public String displayCreateClubForm(Model model) {
        model.addAttribute(new CreateClubFormDTO());
        model.addAttribute("title", "Create Club");
        return "clubs/create";
    }

    @PostMapping("create")
    public String processCreateClubForm(@ModelAttribute @Valid CreateClubFormDTO createClubFormDTO,
                                        Errors errors, Model model){

        if (errors.hasErrors()) {
            model.addAttribute("title", "Create Club");
            return "clubs/create";
        }

        Club existingClub = clubRepository.findByDisplayName(createClubFormDTO.getDisplayName());

        if (existingClub != null) {
            errors.rejectValue("displayName", "displayName.alreadyexists", "A club with that username already exists");
            model.addAttribute("title", "Create Club");
            return "clubs/create";
        }

        String password = createClubFormDTO.getPassword();
        String verifyPassword = createClubFormDTO.getVerifyPassword();
        if (!password.equals(verifyPassword)) {
            errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");
            model.addAttribute("title", "Create Club");
            return "clubs/create";
        }

        Club newClub = new Club(createClubFormDTO.getDisplayName(),
                createClubFormDTO.getActiveBook(),
                createClubFormDTO.getPassword());
        clubRepository.save(newClub);

        return "redirect:/clubs";
    }

    @GetMapping("detail")
    public String displayClubDetails(@RequestParam Integer clubId,
                                     Model model) {

        Optional<Club> result = clubRepository.findById(clubId);

        if (result.isEmpty()) {
            return "redirect:/";
        } else {
            Club club = result.get();
            model.addAttribute("title", club.getDisplayName());
            model.addAttribute("club", club);
        }
        return "clubs/detail";

    }

    @GetMapping("join")
    public String displayJoinClubForm(@RequestParam Integer clubId,
                                      Model model, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("user");
        Optional<Member> currentUser = memberRepository.findById(userId);
        Optional<Club> clubResult = clubRepository.findById(clubId);

        if (clubResult.isEmpty()) {
            return "redirect:/";
        } else if (currentUser.isEmpty()) {
            Club club = clubResult.get();
            model.addAttribute("title", "Please log in to join " + club.getDisplayName());
            return "clubs/join";
        } else {
                Member member = currentUser.get();
                Club club = clubResult.get();
                ClubMemberDTO clubMember = new ClubMemberDTO();
                clubMember.setMember(member);
                clubMember.setClub(club);
                model.addAttribute("title", "Join " + club.getDisplayName());
                model.addAttribute("club", club);
                model.addAttribute("clubId", clubId);
                model.addAttribute("clubMember", clubMember);
        }

        return "clubs/join";
    }

    @PostMapping("join")
    public String processJoinClubForm(@ModelAttribute @Valid ClubMemberDTO clubMember,
                                      Errors errors, Model model) {
        if(!errors.hasErrors()) {
            Member member = clubMember.getMember();
            Club club = clubMember.getClub();
            if(!club.getMembers().contains(member)) {
                club.getMembers().add(member);
                clubRepository.save(club);
            }
            model.addAttribute("title", club.getDisplayName());
            model.addAttribute("club", club);
            return "clubs/detail";
        }
        return "redirect:club/join";
    }

}
