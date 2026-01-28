package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

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
}

class Endorsement {
    public int level;
    public String frame;
}

class Competitive {
    public CompetitiveData pc;
}

class CompetitiveData {
    public Rank tank;
    public Rank damage;
    public Rank support;
}

class Rank {
    public String division;
    public int tier;

    @JsonProperty("role_icon")
    public String roleIcon;

    @JsonProperty("rank_icon")
    public String rankIcon;
}