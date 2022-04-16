package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class CodeNames implements SlashCommand {

    @Override
    public String getName() {
        return "codenames";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        //Taken from swear word generator https://www.fantasynamegenerators.com/swear-words.php
        List<String> PATHS = Arrays.asList(
                "oh_coconuts", "slip_and_slide", "fairydust", "oblivious_ogre", "fire_and_brimstone", "storm_and_thunder", "blasted_burrito",
                "thunder_and_lightning", "mother_father", "blangdang", "doubleheaded_nimwit", "turd_in_a_suit", "eternal_oblivion", "sand_crackers",
                "blazing_inferno", "death_and_taxes", "ignorant_ogre", "hogwash", "weenie_in_a_beanie", "shut_the_front_door", "burned_gravy",
                "oh_patches", "balderdash", "swizzle_sticks", "monkey_disco", "whack_a_holy_moly", "burps_and_farts", "birdbrained_bandit"
        );
        int SIZE = PATHS.size();
        Random RANDOM = new Random();

        String codeNamesUrl = "https://horsepaste.com/" + PATHS.get(RANDOM.nextInt(SIZE));

        return event.reply()
                .withContent(codeNamesUrl);
    }


}
