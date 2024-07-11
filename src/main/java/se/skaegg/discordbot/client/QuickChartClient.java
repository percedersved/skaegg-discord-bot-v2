package se.skaegg.discordbot.client;

import io.quickchart.QuickChart;

import java.util.Map;

/**
 * Using the quickchart.io API to create charts
 */
public class QuickChartClient {

    public String createPollChart(String question, Map<String, Long> alternativeVotes) {

        StringBuilder altsSb = new StringBuilder();
        StringBuilder noVotesSb = new StringBuilder();
        for (Map.Entry<String, Long> alts : alternativeVotes.entrySet()) {
            altsSb.append("'");
            altsSb.append(alts.getKey());
            altsSb.append("'");
            altsSb.append(", ");
            noVotesSb.append("'");
            noVotesSb.append(alts.getValue().toString());
            noVotesSb.append("'");
            noVotesSb.append(", ");
        }
        QuickChart chart = getChart(question, altsSb, noVotesSb);

        return chart.getUrl();
    }

    private static QuickChart getChart(String question, StringBuilder altsSb, StringBuilder noVotesSb) {
        String altsString = altsSb.toString();
        String votes = noVotesSb.toString();


        QuickChart chart = new QuickChart();
        chart.setWidth(400);
        chart.setHeight(300);
        chart.setVersion("4");
        chart.setConfig(String.format("""
                {
                    type: 'bar',
                    data: {
                        labels: [%s],
                        datasets: [
                            {
                                label: '',
                                backgroundColor: 'rgba(90, 130, 180, 0.5)',
                                borderColor: 'rgb(90,130,180)',
                                borderRadius: 10,
                                data: [%s]
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        indexAxis: 'x',
                        elements: {
                            bar: {
                                borderWidth: 2,
                            }
                        },
                        plugins: {
                            legend: false,
                            title: {
                                display: true,
                                text: '%s'
                            },
                            datalabels: {
                                anchor: 'center',
                                align: 'center',
                                color: 'rgb(128,128,128)',
                                font: {
                                  weight: 'bold'
                                }
                            }
                        }
                    }
                }
                """, altsString, votes, question));
        return chart;
    }
}
