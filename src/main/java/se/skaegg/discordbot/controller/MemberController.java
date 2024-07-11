package se.skaegg.discordbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.repository.MemberRepository;
import se.skaegg.discordbot.service.MemberService;

import java.util.List;

@RestController
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}


	@GetMapping(path = "/users", produces = "application/json")
	public List<Member> getAllMembers(@RequestParam(required = false) String serverId) {
		return memberService.getMember(serverId);
	}

	@GetMapping(path = "/user", produces = "application/json")
	public Member getMemberById(@RequestParam String memberId, @RequestParam String serverId) {
		return memberService.getMemberByIdAndServer(memberId, serverId);
	}
}
