package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    public static void main(String[] args) {
        try {
            OverwatchAPI api = new OverwatchAPI();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String profileJson = api.getPlayerProfile("Miraneek-2426");
            PlayerProfile profile = mapper.readValue(profileJson, PlayerProfile.class);

            System.out.println("\nGenerating profile card...");
            ProfileCardGenerator generator = new ProfileCardGenerator();
            generator.generateCard(profile, "/home/miraneek/Pictures/overwatch stats/overwatchStats.png");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
