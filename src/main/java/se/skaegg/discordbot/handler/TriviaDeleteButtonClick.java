package se.skaegg.discordbot.handler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.TriviaButtonClicks;
import se.skaegg.discordbot.entity.TriviaQuestions;
import se.skaegg.discordbot.repository.MemberRepository;
import se.skaegg.discordbot.repository.TriviaButtonClicksRepository;
import se.skaegg.discordbot.repository.TriviaQuestionsRepository;

@Component
public class TriviaDeleteButtonClick implements SlashCommand {

	TriviaButtonClicksRepository triviaButtonClicksRepository;
	TriviaQuestionsRepository triviaQuestionsRepository;
	MemberRepository memberRepository;

	private static final Logger LOG = LoggerFactory.getLogger(TriviaDeleteButtonClick.class);

	public TriviaDeleteButtonClick(TriviaButtonClicksRepository triviaButtonClicksRepository,
			TriviaQuestionsRepository triviaQuestionsRepository, MemberRepository memberRepository) {
		this.triviaButtonClicksRepository = triviaButtonClicksRepository;
		this.triviaQuestionsRepository = triviaQuestionsRepository;
		this.memberRepository = memberRepository;
	}

	@Override
	public String getName() {
		return "trivia_delete_button_click";
	}

	@Override
	public Mono<Void> handle(ChatInputInteractionEvent event) {
		return showSelectMenuButtonClicks(event, event.getOptions());
	}

	private Mono<Void> showSelectMenuButtonClicks(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {
		event.deferReply().withEphemeral(true).subscribe();

		LocalDate date = options.getFirst().getValue()
				.map(ApplicationCommandInteractionOptionValue::asString)
				.map(LocalDate::parse)
				.orElseThrow(() -> new IllegalArgumentException("Date option is required"));

		TriviaQuestions question = triviaQuestionsRepository.findByQuestionDate(date);
		List<TriviaButtonClicks> dbResults = triviaButtonClicksRepository.findByQuestion(question);

		if (dbResults.isEmpty()) {
			return event.editReply("Ingen användare verkar ha hämtat frågan för angivet datum")
					.then();
		}

		Map<Member, String> names = new HashMap<>();

		dbResults.forEach(tbc -> {
			Member member = memberRepository.findByMemberIdAndServerId(tbc.getUserId(), event.getInteraction().getGuildId().get().asString());
			names.put(member, tbc.getId().toString());
		});

		List<SelectMenu.Option> userList = new ArrayList<>();
		names.forEach((name, id) -> userList.add(SelectMenu.Option.of(name.getDisplayName(), id)));

		return event.editReply()
				.withComponents(ActionRow.of(SelectMenu.of("triviaButtonClicks", userList))).then();
	}

	public Mono<Message> deleteButtonClick(SelectMenuInteractionEvent event) {
		event.deferReply().withEphemeral(true).subscribe();
		int buttonClickId = Integer.parseInt(event.getValues().getFirst());

		if (triviaButtonClicksRepository.findById(buttonClickId).isEmpty()) {
			event.editReply("Det finns inget buttonClick med det IDt")
					.subscribe();

			LOG.warn("Tried to delete a button click with ID {}, but it does not exist", buttonClickId);
			return Mono.empty();
		}

		TriviaButtonClicks buttonClick = triviaButtonClicksRepository.findById(buttonClickId).get();

		String channelId = event.getInteraction().getChannelId().asString();
		String answeringUserId = buttonClick.getUserId();
		String deleterUserId = event.getInteraction().getUser().getId().asString();
		LocalDate questionDate = triviaButtonClicksRepository.findQuestionDateById(buttonClickId); // fetch date with separate query to avoid lazy loading issues
		GatewayDiscordClient client = event.getClient();

		String publicMessage = String.format(":warning: <@%s> rensade hämtning av fråga från %s för användare <@%s>",
				deleterUserId, questionDate, answeringUserId);

		triviaButtonClicksRepository.delete(buttonClick);
		event.editReply("ButtonClick med ID " + buttonClickId + " raderades")
				.subscribe();

		client.getChannelById(Snowflake.of(channelId))
				.ofType(MessageChannel.class)
				.flatMap(channel -> channel.createMessage(publicMessage))
				.subscribe();

		// TODO: Maybe try to disable the select menu after it has been used once but it's tricky when it's ephemeral

		return Mono.empty();
	}
}
