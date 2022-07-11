package se.skaegg.discordbot.handlers;

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

        String helpText = "" +
                "`/ping` - Kolla om jag lever\n" +
                "`/bolagetöppet` - Öppettider för Bolaget i Norrtälje\n" +
                "`/codenames` - Skapar en länk för Codenames på horsepaste.com\n" +
                "`/lag [Namn kommaseparerat]` - Slumpar fram 2 lag utifrån namnen som angivits\n" +
                "`/lagvoice` - Tar alla namn som är i någon voicekanal och slumpar 2 lag\n" +
                "`/fredag` - Är det fredag?\n" +
                "`/film [Film- eller seriennamn på orginalspråk]` - Visa information om film/serie från OMDB api\n" +
                "`/nedräkning visa [Namn på nedräkning]` - Visar hur lång tid det är kvar till datumet på nedräkningen\n" +
                "`/nedräkning ny [Namn på nedräkning, datum i format yyyy-MM-dd HH:mm]` - Lägger till nedräkning\n" +
                "`/nedräkning lista` - Listar nedräkningar med namn och ID\n" +
                "`/nedräkning tabort [ID]` - Tar bort nedräkning\n" +
                "`/restaurang` - Tips på restaurang. Default är i Norrtälje. Möjligt att lägga till annan stad eller plats som parameter\n" +
                "`/restaurangduell` - Rösta på 2 olika slumpmässiga restauranger. Default är i Norrtälje. Möjligt att lägga till annan stad eller plats som parameter" +
                "`/statistik` - Visar statistik för kommandon och vilka användare som använt boten mest";

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