package se.skaegg.discordbot.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.jpa.MemberEntity;
import se.skaegg.discordbot.jpa.MemberRepository;

@RestController
public class MemberController {

	MemberRepository memberRepository;

	public MemberController(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}


	@GetMapping(path = "/users", produces = "application/json")
	public List<MemberEntity> getAllMembers(@RequestParam(required = false) String serverId) {
		if (serverId != null) {
			return memberRepository.findAllByServerId(serverId);
		}
		else {
			return memberRepository.findAll();
		}
	}

	@GetMapping(path = "/user", produces = "application/json")
	public MemberEntity getMemberById(@RequestParam String memberId, @RequestParam String serverId) {
		return memberRepository.findByMemberIdAndServerId(memberId, serverId);
	}
}
