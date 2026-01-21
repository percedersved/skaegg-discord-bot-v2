package se.skaegg.discordbot.dto;

import se.skaegg.discordbot.entity.Member;

public record WordleStatsSummary(Member member, Long totalRows, Double averageTries) {}
