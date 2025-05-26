package com.github.exadmin.ostm.collectors.impl.teams;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.github.facade.GitHubCommitsForPeriod;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class NumberOfCommitsPerWeekPerUser extends AbstractCollector {
    private static final String GQL_QUERY_TEMPLATE = """
                {
                  user(login: "USERXXX") {
                    contributionsCollection(
                      from: "FROMXXX"
                      to: "TOXXX"
                    ) {
                      contributionCalendar {
                        totalContributions
                        weeks {
                          contributionDays {
                            weekday
                            date
                            contributionCount
                          }
                        }
                      }
                    }
                  }
                }
                """;

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        LocalDate todayDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        LocalDate fromDate  = todayDate.minusMonths(3).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        String fromStr = MiscUtils.dateToStr(fromDate);
        String toStr  = MiscUtils.dateToStr(todayDate);

        List<String> uniqueUsers = gitHubFacade.getLoginsOfTheTeam();

        // there can be no respose for some users - but we need to fulfill them with zeros or some other stubs
        // so let's remember such users and all the table columns to fulfill
        List<String> usersWithNoResponse = new ArrayList<>();
        Set<TheColumn> columns = new HashSet<>();

        for (String login : uniqueUsers) {
            String query = GQL_QUERY_TEMPLATE.replace("USERXXX", login);
            query = query.replace("FROMXXX", fromStr);
            query = query.replace("TOXXX", toStr);

            GitHubRequest request = HttpRequestBuilder.gitHubGraphQLCall().useQuery(query).build();
            GitHubResponse response = request.execute();

            List<Map<String, Object>> weeksMap = response.getObject("/data/user/contributionsCollection/contributionCalendar/weeks");
            if (weeksMap == null) {
                usersWithNoResponse.add(login);
                continue;
            }

            int weekBackNumber = 1;
            for (Map<String, Object> weekMap : weeksMap) {
                if (weekBackNumber > 12) break;

                List<Map<String, Object>> weekDays = (List<Map<String, Object>>) weekMap.get("contributionDays");

                int totalCount = 0;
                LocalDate weekBeginingDate = todayDate;

                for (Map<String, Object> weekDay : weekDays) {
                    // Integer weekDayNumber   = (Integer) weekDay.get("weekday");
                    String dateStr          = ((String) weekDay.get("date"));
                    Integer count           = (Integer) weekDay.get("contributionCount");

                    LocalDate tmpDate = MiscUtils.strToDate(dateStr);
                    if (tmpDate.isBefore(weekBeginingDate)) weekBeginingDate = tmpDate;

                    totalCount = totalCount + count;
                }

                final TheColumn theColumn = theReportModel.findColumn(TheColumId.findById("column:week_back_" + weekBackNumber));
                if (theColumn == null) throw new IllegalStateException("Can't find column for week with number = " + weekBackNumber);
                weekBackNumber++;

                // here we have date of week beginning and total contributiones cound per that week
                String weekShortDateStr = MiscUtils.dateToStr(weekBeginingDate).substring(0, 10);
                String weekName = "Week " + weekShortDateStr;
                theColumn.setTitle(weekName);

                columns.add(theColumn);

                GitHubCommitsForPeriod info = gitHubFacade.getNumberOfCommitsForPeriod(login, weekBeginingDate, weekBeginingDate.plusWeeks(1));
                StringBuilder sbTooltip = new StringBuilder();
                sbTooltip.append(login).append("<br><br>");

                sbTooltip.append("Commits:<br>");
                for (Map.Entry<String, Integer> me : info.getCommitsMap().entrySet()) {
                    sbTooltip.append("&nbsp;&nbsp;").append(noDomain(me.getKey())).append(": ").append(me.getValue()).append("<br>");
                }

                sbTooltip.append("PRs:<br>");
                for (Map.Entry<String, Integer> me : info.getPrsMap().entrySet()) {
                    sbTooltip.append("&nbsp;&nbsp;").append(noDomain(me.getKey())).append(": ").append(me.getValue()).append("<br>");
                }

                sbTooltip.append("Issue Reports:<br>");
                for (Map.Entry<String, Integer> me : info.getIssuesMap().entrySet()) {
                    sbTooltip.append("&nbsp;&nbsp;").append(noDomain(me.getKey())).append(": ").append(me.getValue()).append("<br>");
                }

                TheCellValue cellValue = new TheCellValue(totalCount, totalCount, SeverityLevel.INFO);
                cellValue.setToolTipText(sbTooltip.toString());
                String rowId = "row:" + login;

                theColumn.addValue(rowId, cellValue);
            }

        }

        for (String login : usersWithNoResponse) {
            for (TheColumn theColumn : columns) {
                TheCellValue cellValue = new TheCellValue("No data", "0", SeverityLevel.WARN);
                String rowId = "row:" + login;

                theColumn.addValue(rowId, cellValue);
            }
        }
    }

    private static String noDomain(String repoName) {
        return repoName.substring("https://github.com/".length());
    }


}
