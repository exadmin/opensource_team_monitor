package com.github.exadmin.ostm.github.facade;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnlyKnownUsers {
    // READ TEAM
    private static final Map<String, String> ALIAS_TO_NAME_RED = new HashMap<>();
    static {
        ALIAS_TO_NAME_RED.put("alagishev", "Aleksandr Agishev");
        ALIAS_TO_NAME_RED.put("kagw95", "Aleksandr Kapustin");
        ALIAS_TO_NAME_RED.put("al-matushkin", "Aleksandr Matushkin");
        ALIAS_TO_NAME_RED.put("karpov-aleksandr", "Aleksandr V. Karpov");
        ALIAS_TO_NAME_RED.put("sukhochevaleksei", "Aleksei Sukhochev");
        ALIAS_TO_NAME_RED.put("b41ex", "Alexey Bochencev");
        ALIAS_TO_NAME_RED.put("NuTTeR", "Andrei Chumak");
        ALIAS_TO_NAME_RED.put("andyroode", "Andrei Rudchenko");
        ALIAS_TO_NAME_RED.put("Anton-Pecherskikh", "Anton Pecherskikh");
        ALIAS_TO_NAME_RED.put("borislavr", "Boris Lavrishchev");
        ALIAS_TO_NAME_RED.put("ard951", "Denis Arychkov");
        ALIAS_TO_NAME_RED.put("denifilatoff", "Denis Filatov");
        ALIAS_TO_NAME_RED.put("AkhDmitrii", "Dmitrii Akhtyrskii");
        ALIAS_TO_NAME_RED.put("dmitriipisarev", "Dmitrii Pisarev");
        ALIAS_TO_NAME_RED.put("dmitry618", "Dmitriy Myasnikov");
        ALIAS_TO_NAME_RED.put("popoveugene", "Evgenii A. Popov");
        ALIAS_TO_NAME_RED.put("estetsenko", "Evgenii Stetsenko");
        ALIAS_TO_NAME_RED.put("FedorProshin", "Fedor Proshin");
        ALIAS_TO_NAME_RED.put("LightlessOne", "Igor Lebedev");
        ALIAS_TO_NAME_RED.put("IldarMinaev", "Ildar Minaev");
        ALIAS_TO_NAME_RED.put("exadmin", "Ilya Smirnov");
        ALIAS_TO_NAME_RED.put("iurii-golovinskii", "Iurii Golovinskii");
        ALIAS_TO_NAME_RED.put("Sid775", "Ivan Sviridov");
        ALIAS_TO_NAME_RED.put("Ksiona", "Kseniia Shmoilova");
        ALIAS_TO_NAME_RED.put("namu0421", "Nadezhda Murashova");
        ALIAS_TO_NAME_RED.put("NikolaiKuziaevQubership", "Nikolai Kuziaev");
        ALIAS_TO_NAME_RED.put("likelios", "Nikolai Maksak");
        ALIAS_TO_NAME_RED.put("nookyo", "Pavel Anikin");
        ALIAS_TO_NAME_RED.put("PavelYadrov", "Pavel Iadrov");
        ALIAS_TO_NAME_RED.put("makeev-pavel", "Pavel Makeev");
        ALIAS_TO_NAME_RED.put("KryukovaPolina", "Polina Kriukova");
        ALIAS_TO_NAME_RED.put("RomanB89", "Roman Barmin");
        ALIAS_TO_NAME_RED.put("kichasov", "Roman Kichasov");
        ALIAS_TO_NAME_RED.put("ormig", "Roman Romanov");
        ALIAS_TO_NAME_RED.put("JayLim2", "Sergei Komarov");
        ALIAS_TO_NAME_RED.put("alsergs", "Sergei S. Aleksandrov");
        ALIAS_TO_NAME_RED.put("SSNikolaevich", "Sergei Skuratovich");
        ALIAS_TO_NAME_RED.put("pankratovsa", "Sergey Pankratov");
        ALIAS_TO_NAME_RED.put("Beauline", "Valentina Feshina");
        ALIAS_TO_NAME_RED.put("viacheslav-lunev", "Viacheslav Lunev");
        ALIAS_TO_NAME_RED.put("shumnic", "Viktor Solovev");
        ALIAS_TO_NAME_RED.put("vlsi", "Vladimir Sitnikov");
        ALIAS_TO_NAME_RED.put("TaurMorchant", "Vladislav Larkin");
        ALIAS_TO_NAME_RED.put("Vladislav-Romanov27", "Vladislav Romanov");
    }

    // BLUE TEAM
    private static final Map<String, String> ALIAS_TO_NAME_BLUE = new HashMap<>();
    static {
        ALIAS_TO_NAME_BLUE.put("lis0x90", "Sergey Lisovoy");
        ALIAS_TO_NAME_BLUE.put("vlme0618", "Vladislav Medvedev");
        ALIAS_TO_NAME_BLUE.put("dale1020", "Daria Lebedeva");
        ALIAS_TO_NAME_BLUE.put("Bogdan-Bairamov", "Bogdan Bairamov");
        ALIAS_TO_NAME_BLUE.put("DmitryChurbanov", "Dmitry Churbanov");
        ALIAS_TO_NAME_BLUE.put("asatt", "Alexey Karasev");
        ALIAS_TO_NAME_BLUE.put("denisanfimov", "Denis Anfimov");
        ALIAS_TO_NAME_BLUE.put("Derananer", "Ilia Lisetckii");
        ALIAS_TO_NAME_BLUE.put("k1shk1n", "Vladislav Kishkin");
        ALIAS_TO_NAME_BLUE.put("andrewluckyguy", "Andrey Lukiyanenko");
        ALIAS_TO_NAME_BLUE.put("miyamuraga", "Kristina Maltceva");
        ALIAS_TO_NAME_BLUE.put("GlimmerCape", "Nurlybek Kamelov");
        ALIAS_TO_NAME_BLUE.put("chethana-shastry-p", "Chethana Shastry");
        ALIAS_TO_NAME_BLUE.put("dmitriikazanin", "Dmitrii Kazanin");
    }

    private static Map<String, String> ALIAS_TO_NAME_ALL = new HashMap<>();
    static {
        ALIAS_TO_NAME_ALL.putAll(ALIAS_TO_NAME_RED);
        ALIAS_TO_NAME_ALL.putAll(ALIAS_TO_NAME_BLUE);

        // Other aliases (neither RED nor BLUE)
        ALIAS_TO_NAME_ALL.put("ifdru74", "Alexey Shtykov");
        ALIAS_TO_NAME_ALL.put("rparf", "Roman Parfinenko");

        ALIAS_TO_NAME_ALL = Collections.unmodifiableMap(ALIAS_TO_NAME_ALL);

    }

    static Map<String, String> getKnowUsersOnly() {
        return ALIAS_TO_NAME_ALL;
    }

    static String getRealNameByLogin(String login) {
        return ALIAS_TO_NAME_ALL.get(login);
    }

    public static Map<String, String> getRedUsersOnly() {
        return Collections.unmodifiableMap(ALIAS_TO_NAME_RED);
    }

    public static Map<String, String> getBlueUsersOnly() {
        return Collections.unmodifiableMap(ALIAS_TO_NAME_BLUE);
    }
}
