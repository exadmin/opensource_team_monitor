package com.github.exadmin.ostm.github.facade;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnlyKnownUsers {
    private static final Map<String, String> ALIAS_TO_NAME = new HashMap<>();
    static {
        // READ TEAM
        ALIAS_TO_NAME.put("alagishev", "Aleksandr Agishev");
        ALIAS_TO_NAME.put("kagw95", "Aleksandr Kapustin");
        ALIAS_TO_NAME.put("al-matushkin", "Aleksandr Matushkin");
        ALIAS_TO_NAME.put("karpov-aleksandr", "Aleksandr V. Karpov");
        ALIAS_TO_NAME.put("sukhochevaleksei", "Aleksei Sukhochev");
        // ALIAS_TO_NAME.put("Unknown", "Alexander Fadeev");
        ALIAS_TO_NAME.put("b41ex", "Alexey Bochencev");
        ALIAS_TO_NAME.put("NuTTeR", "Andrei Chumak");
        ALIAS_TO_NAME.put("andyroode", "Andrei Rudchenko");
        //ALIAS_TO_NAME.put("Unknown", "Anton Korchak");
        ALIAS_TO_NAME.put("Anton-Pecherskikh", "Anton Pecherskikh");
        ALIAS_TO_NAME.put("borislavr", "Boris Lavrishchev");
        ALIAS_TO_NAME.put("ard951", "Denis Arychkov");
        ALIAS_TO_NAME.put("denifilatoff", "Denis Filatov");
        ALIAS_TO_NAME.put("AkhDmitrii", "Dmitrii Akhtyrskii");
        ALIAS_TO_NAME.put("dmitriipisarev", "Dmitrii Pisarev");
        ALIAS_TO_NAME.put("dmitry618", "Dmitriy Myasnikov");
        //ALIAS_TO_NAME.put("Unknown", "Dmitriy Sivaschenko");
        //ALIAS_TO_NAME.put("Unknown", "Elena Khvostova");
        ALIAS_TO_NAME.put("popoveugene", "Evgenii A. Popov");
        ALIAS_TO_NAME.put("estetsenko", "Evgenii Stetsenko");
        ALIAS_TO_NAME.put("FedorProshin", "Fedor Proshin");
        ALIAS_TO_NAME.put("LightlessOne", "Igor Lebedev");
        ALIAS_TO_NAME.put("IldarMinaev", "Ildar Minaev");
        ALIAS_TO_NAME.put("exadmin", "Ilya Smirnov");
        ALIAS_TO_NAME.put("iurii-golovinskii", "Iurii Golovinskii");
        //ALIAS_TO_NAME.put("Unknown", "Ivan Kuznetsov");
        ALIAS_TO_NAME.put("Sid775", "Ivan Sviridov");
        ALIAS_TO_NAME.put("Ksiona", "Kseniia Shmoilova");
        ALIAS_TO_NAME.put("namu0421", "Nadezhda Murashova");
        ALIAS_TO_NAME.put("NikolaiKuziaevQubership", "Nikolai Kuziaev");
        ALIAS_TO_NAME.put("likelios", "Nikolai Maksak");
        ALIAS_TO_NAME.put("nookyo", "Pavel Anikin");
        ALIAS_TO_NAME.put("PavelYadrov", "Pavel Iadrov");
        ALIAS_TO_NAME.put("makeev-pavel", "Pavel Makeev");
        ALIAS_TO_NAME.put("KryukovaPolina", "Polina Kriukova");
        ALIAS_TO_NAME.put("RomanB89", "Roman Barmin");
        ALIAS_TO_NAME.put("kichasov", "Roman Kichasov");
        ALIAS_TO_NAME.put("ormig", "Roman Romanov");
        ALIAS_TO_NAME.put("JayLim2", "Sergei Komarov");
        ALIAS_TO_NAME.put("alsergs", "Sergei S. Aleksandrov");
        ALIAS_TO_NAME.put("SSNikolaevich", "Sergei Skuratovich");
        ALIAS_TO_NAME.put("pankratovsa", "Sergey Pankratov");
        ALIAS_TO_NAME.put("Beauline", "Valentina Feshina");
        ALIAS_TO_NAME.put("viacheslav-lunev", "Viacheslav Lunev");
        //ALIAS_TO_NAME.put("Unknown", "Viktor Iarmola");
        ALIAS_TO_NAME.put("shumnic", "Viktor Solovev");
        ALIAS_TO_NAME.put("vlsi", "Vladimir Sitnikov");
        ALIAS_TO_NAME.put("TaurMorchant", "Vladislav Larkin");
        ALIAS_TO_NAME.put("Vladislav-Romanov27", "Vladislav Romanov");
        //ALIAS_TO_NAME.put("Unknown", "Yulia Gomer");
        //ALIAS_TO_NAME.put("Unknown", "Yuliya Kulikova");

        // NON-RED TEAM
        ALIAS_TO_NAME.put("ifdru74", "Alexey Shtykov");
        ALIAS_TO_NAME.put("rparf", "Roman Parfinenko");
        ALIAS_TO_NAME.put("lis0x90", "Sergey Lisovoy");
        ALIAS_TO_NAME.put("vlme0618", "Vladislav Medvedev");
        ALIAS_TO_NAME.put("dale1020", "Daria Lebedeva");
        ALIAS_TO_NAME.put("Bogdan-Bairamov", "Bogdan Bairamov");
        ALIAS_TO_NAME.put("DmitryChurbanov", "Dmitry Churbanov");
        ALIAS_TO_NAME.put("asatt", "Alexey Karasev");
        ALIAS_TO_NAME.put("denisanfimov", "Denis Anfimov");
        ALIAS_TO_NAME.put("Derananer", "Ilia Lisetckii");
        ALIAS_TO_NAME.put("k1shk1n", "Vladislav Kishkin");
        ALIAS_TO_NAME.put("andrewluckyguy", "Andrey Lukiyanenko");
        ALIAS_TO_NAME.put("miyamuraga", "Kristina Maltceva");
        ALIAS_TO_NAME.put("GlimmerCape", "Nurlybek Kamelov");
        ALIAS_TO_NAME.put("chethana-shastry-p", "Chethana Shastry");
    }

    static Map<String, String> getKnowUsersOnly() {
        return Collections.unmodifiableMap(ALIAS_TO_NAME);
    }

    static String getRealNameByLogin(String login) {
        return ALIAS_TO_NAME.get(login);
    }
}
