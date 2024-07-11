package se.skaegg.discordbot.handler;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Help implements SlashCommand{

    @Override
    public String getName() {
        return "hjälp";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String helpText = """                
                `/ping` - Kolla om jag lever
                `/bolagetöppet` - Öppettider för Bolaget i Norrtälje
                `/codenames` - Skapar en länk för Codenames på horsepaste.com
                `/lag [Namn kommaseparerat]` - Slumpar fram 2 lag utifrån namnen som angivits
                `/lagvoice` - Tar alla namn som är i någon voicekanal och slumpar 2 lag
                `/fredag` - Är det fredag?\
                `/film [Film- eller seriennamn på orginalspråk]` - Visa information om film/serie från OMDB api
                `/nedräkning visa [Namn på nedräkning]` - Visar hur lång tid det är kvar till datumet på nedräkningen
                `/nedräkning ny [Namn på nedräkning, datum i format yyyy-MM-dd HH:mm]` - Lägger till nedräkning
                `/nedräkning lista` - Listar nedräkningar med namn och ID
                `/nedräkning tabort [ID]` - Tar bort nedräkning
                `/restaurang` - Tips på restaurang. Default är i Norrtälje. Möjligt att lägga till annan stad eller plats som parameter
                `/restaurangduell` - Rösta på 2 olika slumpmässiga restauranger. Default är i Norrtälje. Möjligt att lägga till annan stad eller plats som parameter
                `/statistik` - Visar statistik för kommandon och vilka användare som använt boten mest
                `/trivia dagens` - Ger dagens fråga
                `/trivia ställning_innevarande` - Visar Trivia-ställningen för innevarande månad
                `/trivia ställning_föregående` - Visar Trivia-ställningen för föregående månad
                `/trivia ställning_alltime` - Visar Trivia-ställningen för all tid
                `/omröstning skapa` - Skapa en omröstning
                `/omröstning visa` - Sök och visa en omröstning
                `/emojistats topplista` - Visar en lista över de mest använda custom emojisarna
                """;

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Kommandorörelser")
                .description(helpText)
                .build();

        return event.reply()
                .withEphemeral(true)
                .withEmbeds(embed)
                .onErrorResume(throwable -> event.reply()
                        .withEphemeral(true)
                        .withContent("Nu gick något fel. Testa igen vettja!"));
    }
}