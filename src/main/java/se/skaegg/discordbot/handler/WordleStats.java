package se.skaegg.discordbot.handler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.dto.WordleStatsSummary;
import se.skaegg.discordbot.dto.WordleStatsTriesCount;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.repository.MemberRepository;
import se.skaegg.discordbot.repository.WordleStatsRepository;

@Component
public class WordleStats implements SlashCommand {

    private static final Pattern LINE_PATTERN = Pattern.compile("(?m)^\\s*(?:\\p{So}\\s*)?(X|\\d)/6:\\s+(.+)$");
    private static final Pattern USER_PATTERN = Pattern.compile("<@!?(\\d+)>");

    WordleStatsRepository wordleStatsRepository;
    MemberRepository memberRepository;

    public WordleStats(WordleStatsRepository wordleStatsRepository, MemberRepository memberRepository) {
        this.wordleStatsRepository = wordleStatsRepository;
        this.memberRepository = memberRepository;
    }


    @Override
    public String getName() {
        return "wordlestats";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        ApplicationCommandInteractionOption subCommand = event.getOptions().getFirst();
        String subCommandName = subCommand.getName();

        if (subCommandName.equals("mina")) {
            return getPersonalStats(event).then();
        }
        else if (subCommandName.equals("snitt")) {
            getAverageTopList(event);
        }
        return Mono.empty();
    }

    private Mono<Message> getPersonalStats(ChatInputInteractionEvent event) {
        event.deferReply().withEphemeral(true).subscribe();

        long memberId = event.getInteraction().getMember().get().getId().asLong();
        List<WordleStatsTriesCount> wordleStatsTriesCount = wordleStatsRepository.findTriesDistribution(memberId);

        // Ensure count 0 also shown
        for (int i = 0; i <= 6; i++) {
            final int tries = i;
            boolean hasEntry = wordleStatsTriesCount.stream().anyMatch(stat -> stat.tries() == tries);
            if (!hasEntry) {
                wordleStatsTriesCount.add(new WordleStatsTriesCount(tries, 0L));
            }
        }

        int totalGames = wordleStatsTriesCount.stream()
                .mapToInt(stat -> Math.toIntExact(stat.count()))
                .sum();

        String description = wordleStatsTriesCount.stream()
                .sorted((a, b) -> Integer.compare(a.tries(), b.tries()))
                .map(stat -> String.format(
                        "%s/6 - %d",
                        stat.tries() == 0 ? "X" : stat.tries(),
                        stat.count()
                ))
                .collect(Collectors.joining("\n"));

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Dina wordle stats - Totalt antal: " + totalGames)
                .addField(EmbedCreateFields.Field.of("Försök - Antal", description, true))
                .build();

        return event.createFollowup()
                .withEmbeds(embed)
                .withEphemeral(true);
    }

    private void getAverageTopList(ChatInputInteractionEvent event) {
        event.deferReply().subscribe();

        List<WordleStatsSummary> avgSummaryList = wordleStatsRepository.findMemberStatsSummary();

        String description = avgSummaryList.stream()
                .sorted((a, b) -> Double.compare(a.averageTries(), b.averageTries()))
                .map(stat -> String.format(
                        "<@%s> - %.2f (%d spel)",
                        stat.member().getMemberId(),
                        stat.averageTries(),
                        stat.totalRows()
                ))
                .collect(Collectors.joining("\n"));

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Wordle - Totalt antal spelade och genomsnittliga försök")
                .addField(EmbedCreateFields.Field.of("Användare - Snitt", description, true))
                .build();

        event.createFollowup()
                .withEmbeds(embed)
                .subscribe();
    }

    public void saveWordleStats(MessageCreateEvent event) {

        Message message = event.getMessage();
        String serverId = event.getGuildId().get().asString();
        List<ParsedWordleResult> parsed = parse(message);

        parsed.forEach(result -> {
                    se.skaegg.discordbot.entity.WordleStats stat = new se.skaegg.discordbot.entity.WordleStats();
                    String discordUserId = String.valueOf(result.discordUserId);
                    Member member = memberRepository.findByMemberIdAndServerId(discordUserId, serverId);
                    LocalDate date = LocalDate.now().minusDays(1);
                    stat.setMember(member);
                    stat.setTries(result.tries());
                    stat.setDate(date);
                    wordleStatsRepository.save(stat);
                });
    }

    private static List<ParsedWordleResult> parse(Message message) {

        List<ParsedWordleResult> results = new ArrayList<>();

        Map<Long, User> mentionedUsersById =
                message.getUserMentions().stream()
                        .collect(Collectors.toMap(
                                u -> u.getId().asLong(),
                                u -> u
                        ));

        Matcher lineMatcher = LINE_PATTERN.matcher(message.getContent());

        while (lineMatcher.find()) {

            String triesRaw = lineMatcher.group(1);
            Integer tries = triesRaw.equals("X") ? null : Integer.valueOf(triesRaw);

            String usersPart = lineMatcher.group(2);
            Matcher userMatcher = USER_PATTERN.matcher(usersPart);

            while (userMatcher.find()) {
                long userId = Long.parseLong(userMatcher.group(1));

                User user = mentionedUsersById.get(userId);
                if (user == null) {
                    // edge case: mention not resolvable
                    continue;
                }

                results.add(new ParsedWordleResult(user.getId().asLong(), tries));
            }
        }

        return results;
    }

    public record ParsedWordleResult(
            long discordUserId,
            Integer tries   // null = X/7
    ) {
    }

}