package com.github.exadmin.ostm.collectors.impl.teams;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubContributorData;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.util.*;

public class TeamKnownNames extends AbstractCollector {
    private static final Map<String, String> ALIAS_TO_NAME = new HashMap<>();
    static {
        ALIAS_TO_NAME.put("alagishev", "Aleksandr Agishev");
        ALIAS_TO_NAME.put("kagw95", "Aleksandr Kapustin");
        ALIAS_TO_NAME.put("al-matushkin", "Aleksandr Matushkin");
        ALIAS_TO_NAME.put("karpov-aleksandr", "Aleksandr V Karpov");
        ALIAS_TO_NAME.put("b41ex", "Alexey Bochencev");
        ALIAS_TO_NAME.put("ifdru74", "Alexey Shtykov");
        ALIAS_TO_NAME.put("andyroode", "Andrei Rudchenko");
        ALIAS_TO_NAME.put("Anton-Pecherskikh", "Anton Pecherskikh");
        ALIAS_TO_NAME.put("borislavr", "Boris Lavrishchev");
        ALIAS_TO_NAME.put("denifilatoff", "Denis Filatov");
        ALIAS_TO_NAME.put("AkhDmitrii", "Dmitrii Akhtyrskii");
        ALIAS_TO_NAME.put("dmitriipisarev", "Dmitrii Pisarev");
        ALIAS_TO_NAME.put("egbu", "Egor Budrin");
        ALIAS_TO_NAME.put("popoveugene", "Evgenii A. Popov");
        ALIAS_TO_NAME.put("estetsenko", "Evgenii Stetsenko");
        ALIAS_TO_NAME.put("FedorProshin", "Fedor Proshin");
        ALIAS_TO_NAME.put("IldarMinaev", "Ildar Minaev");
        ALIAS_TO_NAME.put("exadmin", "Ilya Smirnov");
        ALIAS_TO_NAME.put("iurii-golovinskii", "Iurii Golovinskii");
        ALIAS_TO_NAME.put("Sid775", "Ivan Sviridov");
        ALIAS_TO_NAME.put("NikolaiKuziaevQubership", "Nikolai Kuziaev");
        ALIAS_TO_NAME.put("nookyo", "Pavel Anikin");
        ALIAS_TO_NAME.put("PavelYadrov", "Pavel Iadrov");
        ALIAS_TO_NAME.put("makeev-pavel", "Pavel Makeev");
        ALIAS_TO_NAME.put("KryukovaPolina", "Polina Kriukova");
        ALIAS_TO_NAME.put("RomanB89", "Roman Barmin");
        ALIAS_TO_NAME.put("kichasov", "Roman Kichasov");
        ALIAS_TO_NAME.put("rparf", "Roman Parfinenko");
        ALIAS_TO_NAME.put("SSNikolaevich", "Sergei Skuratovich");
        ALIAS_TO_NAME.put("pankratovsa", "Sergey Pankratov");
        ALIAS_TO_NAME.put("Beauline", "Valentina Feshina");
        ALIAS_TO_NAME.put("viacheslav-lunev", "Viacheslav Lunev");
        ALIAS_TO_NAME.put("shumnic", "Viktor Solovev");
        ALIAS_TO_NAME.put("vlsi", "Vladimir Sitnikov");
        ALIAS_TO_NAME.put("TaurMorchant", "Vladislav Larkin");
        ALIAS_TO_NAME.put("Vladislav-Romanov27", "Vladislav Romanov");

    }

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade) {
        Set<String> uniqueLogins = new HashSet<>();

        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository ghRepo : allRepositories) {
            List<GitHubContributorData> ghContributionDataList = gitHubFacade.getContributionsForRepository("Netcracker", ghRepo.getName());
            for (GitHubContributorData data: ghContributionDataList) {
                uniqueLogins.add(data.getLogin());
            }
        }

        // create report
        final TheColumn colLogin = theReportModel.findColumn(TheColumId.COL_USER_LOGIN);
        final TheColumn colRealName = theReportModel.findColumn(TheColumId.COL_USER_REAL_NAME);

        for (String login : uniqueLogins) {
            String rowId = "row:" + login;

            TheCellValue cvLogin = new TheCellValue(login, login, SeverityLevel.INFO);
            colLogin.addValue(rowId, cvLogin);

            SeverityLevel severity = SeverityLevel.INFO;
            String realName = ALIAS_TO_NAME.get(login);
            if (realName == null) {
                realName = "---";
                severity = SeverityLevel.WARN;
            }
            TheCellValue cvRealName = new TheCellValue(realName, realName, severity);
            colRealName.addValue(rowId, cvRealName);
        }
    }
}
