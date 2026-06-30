package com.plovdev.pornviewer.core.models.porn;

public enum Country {
    RUSSIA("ru", "rus", "russia", "россия", "russian"),
    UKRAINE("ua", "ukr", "ukraine", "украина", "ukrainian"),
    BELARUS("by", "blr", "belarus", "беларусь", "belarussian"),
    KAZAKHSTAN("kz", "kaz", "kazakhstan", "казахстан"),
    CZECH_REPUBLIC("cz", "cze", "czech", "czechrepublic", "czechia"),
    SLOVAKIA("sk", "svk", "slovakia", "slovak"),
    POLAND("pl", "pol", "poland", "polish"),
    FRANCE("fr", "fra", "france", "french"),
    GERMANY("de", "ger", "germany", "german", "deutschland"),
    ITALY("it", "ita", "italy", "italian"),
    SPAIN("es", "esp", "spain", "spanish"),
    UNITED_KINGDOM("uk", "gb", "gbr", "england", "english", "united kingdom", "britain"),
    NETHERLANDS("nl", "nld", "netherlands", "dutch", "holland"),
    SWEDEN("se", "swe", "sweden", "swedish"),
    NORWAY("no", "nor", "norway", "norwegian"),
    DENMARK("dk", "den", "denmark", "danish"),
    FINLAND("fi", "fin", "finland", "finnish"),
    AUSTRIA("at", "aut", "austria", "austrian"),
    SWITZERLAND("ch", "che", "switzerland", "swiss"),
    HUNGARY("hu", "hun", "hungary", "hungarian"),
    ROMANIA("ro", "rou", "romania", "romanian"),
    BULGARIA("bg", "bgr", "bulgaria", "bulgarian"),
    GREECE("gr", "grc", "greece", "greek"),
    USA("us", "usa", "united states", "america", "american", "united states of america"),
    CANADA("ca", "can", "canada", "canadian"),
    MEXICO("mx", "mex", "mexico", "mexican"),
    BRAZIL("br", "bra", "brazil", "brazilian"),
    ARGENTINA("ar", "arg", "argentina", "argentinian"),
    COLOMBIA("co", "col", "colombia", "colombian"),
    VENEZUELA("ve", "ven", "venezuela", "venezuelan"),
    PERU("pe", "per", "peru", "peruvian"),
    CHILE("cl", "chl", "chile", "chilean"),
    ECUADOR("ec", "ecu", "ecuador", "ecuadorian"),
    JAPAN("jp", "jpn", "japan", "japanese"),
    CHINA("cn", "chn", "china", "chinese"),
    SOUTH_KOREA("kr", "kor", "south korea", "korea", "korean"),
    THAILAND("th", "tha", "thailand", "thai"),
    PHILIPPINES("ph", "phl", "philippines", "filipino"),
    INDIA("in", "ind", "india", "indian"),
    INDONESIA("id", "idn", "indonesia", "indonesian"),
    VIETNAM("vn", "vnm", "vietnam", "vietnamese"),
    TAIWAN("tw", "twn", "taiwan", "taiwanese"),
    MALAYSIA("my", "mys", "malaysia", "malaysian"),
    SINGAPORE("sg", "sgp", "singapore", "singaporean"),
    PAKISTAN("pk", "pak", "pakistan", "pakistani"),
    AUSTRALIA("au", "aus", "australia", "australian"),
    NEW_ZEALAND("nz", "nzl", "new zealand", "new zealander"),
    SOUTH_AFRICA("za", "zaf", "south africa", "south african"),
    EGYPT("eg", "egy", "egypt", "egyptian"),
    MOROCCO("ma", "mar", "morocco", "moroccan"),
    TUNISIA("tn", "tun", "tunisia", "tunisian"),
    NIGERIA("ng", "nga", "nigeria", "nigerian"),
    KENYA("ke", "ken", "kenya", "kenyan"),
    OTHER();

    public static Country fromString(String cnt) {
        if (cnt == null || cnt.isBlank()) {
            return OTHER;
        }

        cnt = cnt.trim();
        for (Country country : values()) {
            if (country.name().equalsIgnoreCase(cnt)) {
                return country;
            } else {
                for (String alias : country.getAliases()) {
                    if (alias.equalsIgnoreCase(cnt)) {
                        return country;
                    }
                }
            }
        }

        return OTHER;
    }

    private final String[] aliases;

    Country(String... aliases) {
        this.aliases = aliases;
    }

    public String[] getAliases() {
        return aliases;
    }
}