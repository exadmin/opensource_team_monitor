package com.github.exadmin.ostm.collectors.impl;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubRequestBuilder;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportTable;
import com.github.exadmin.ostm.uimodel.TheSheet;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class NumberOfCommitsPerWeekPerUser extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade) {
        final TheSheet theSheet = theReportTable.findSheet("sheet:team-summary", newSheet -> newSheet.setTitle("Team Summary"));

        LocalDate todayDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        LocalDate fromDate  = todayDate.minusMonths(3).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        String fromStr = dateToStr(fromDate);
        String toStr  = dateToStr(todayDate);

        final String queryTemplate = """
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

        List<String> uniqueUsers = gitHubFacade.getUniqueUsers("Netcracker");

        // there can be no respose for some users - but we need to fulfill them with zeros or some other stubs
        // so let's remember such users and all the table columns to fulfill
        List<String> usersWithNoResponse = new ArrayList<>();
        Set<TheColumn> columns = new HashSet<>();

        for (String login : uniqueUsers) {
            String query = queryTemplate.replace("USERXXX", login);
            query = query.replace("FROMXXX", fromStr);
            query = query.replace("TOXXX", toStr);

            GitHubRequest request = GitHubRequestBuilder.graphQL().useQuery(query).build();
            GitHubResponse response = request.execute();

            List<Map<String, Object>> weeksMap = response.getObject("data", "user", "contributionsCollection", "contributionCalendar", "weeks");
            if (weeksMap == null) {
                usersWithNoResponse.add(login);
                continue;
            }

            for (Map<String, Object> weekMap : weeksMap) {
                List<Map<String, Object>> weekDays = (List<Map<String, Object>>) weekMap.get("contributionDays");

                int totalCount = 0;
                LocalDate weekBeginingDate = todayDate;

                for (Map<String, Object> weekDay : weekDays) {
                    // Integer weekDayNumber   = (Integer) weekDay.get("weekday");
                    String dateStr          = ((String) weekDay.get("date"));
                    Integer count           = (Integer) weekDay.get("contributionCount");

                    LocalDate tmpDate = strToDate(dateStr);
                    if (tmpDate.isBefore(weekBeginingDate)) weekBeginingDate = tmpDate;

                    totalCount = totalCount + count;
                }

                // here we have date of week beginning and total contributiones cound per that week
                String weekShortDateStr = dateToStr(weekBeginingDate).substring(0, 10);
                String weekName = "column:week:" + weekShortDateStr;
                final TheColumn theColumn = theSheet.findColumn(weekName, newColumn -> {
                    newColumn.setTitle("Week " + weekShortDateStr);
                    newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
                    // newColumn.setRenderingOrder(10);
                });

                columns.add(theColumn);

                TheCellValue cellValue = new TheCellValue("" + totalCount);
                cellValue.setToolTipText("test tooltip");
                String rowId = "row:" + login;

                theColumn.addValue(rowId, cellValue);
            }

        }

        for (String login : usersWithNoResponse) {
            for (TheColumn theColumn : columns) {
                TheCellValue cellValue = new TheCellValue("0");
                String rowId = "row:" + login;

                theColumn.addValue(rowId, cellValue);
            }
        }
    }

    private static String dateToStr(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    private static LocalDate strToDate(String strDateISO6801) {
        if (strDateISO6801.length() == 10) strDateISO6801 = strDateISO6801 + "T00:00:00.000+00:00"; // to fix strange case for graphQL response from GitHub

        return OffsetDateTime.parse(strDateISO6801).toLocalDate();
    }
}
