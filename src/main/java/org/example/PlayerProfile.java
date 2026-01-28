package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerProfile {
    public Summary summary;
}

class Summary {
    public String username;
    public String avatar;
    public String namecard;
    public String title;
    public Endorsement endorsement;
    public Competitive competitive;

    @JsonProperty("last_updated_at")
    public long lastUpdatedAt;
}

class Endorsement {
    public int level;
    public String frame;
}

class Competitive {
    public CompetitiveData pc;
    public CompetitiveData console;  // může být null
}

class CompetitiveData {
    public Rank tank;
    public Rank damage;
    public Rank support;
    public Rank open;  // může být null
    public int season;
}

class Rank {
    public String division;
    public int tier;

    @JsonProperty("role_icon")
    public String roleIcon;

    @JsonProperty("rank_icon")
    public String rankIcon;

    @JsonProperty("tier_icon")
    public String tierIcon;
}
