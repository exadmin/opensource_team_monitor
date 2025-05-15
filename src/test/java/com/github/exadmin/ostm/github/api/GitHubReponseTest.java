package com.github.exadmin.ostm.github.api;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class GitHubReponseTest {

    @Test
    public void testGetMultipleValues() {
        String responseBody = """
                {
                  "data": {
                    "user": {
                      "email": "",
                      "createdAt": "2010-09-01T10:39:12Z",
                      "contributionsCollection": {
                        "contributionCalendar": {
                          "totalContributions": 0,
                          "weeks": [
                            {
                              "contributionDays": [
                                {
                                  "weekday": 2,
                                  "date": "2025-04-01T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 3,
                                  "date": "2025-04-02T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 4,
                                  "date": "2025-04-03T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 5,
                                  "date": "2025-04-04T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 6,
                                  "date": "2025-04-05T00:00:00.000+00:00",
                                  "contributionCount": 0
                                }
                              ]
                            },
                            {
                              "contributionDays": [
                                {
                                  "weekday": 0,
                                  "date": "2025-04-06T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 1,
                                  "date": "2025-04-07T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 2,
                                  "date": "2025-04-08T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 3,
                                  "date": "2025-04-09T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 4,
                                  "date": "2025-04-10T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 5,
                                  "date": "2025-04-11T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 6,
                                  "date": "2025-04-12T00:00:00.000+00:00",
                                  "contributionCount": 0
                                }
                              ]
                            },
                            {
                              "contributionDays": [
                                {
                                  "weekday": 0,
                                  "date": "2025-04-13T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 1,
                                  "date": "2025-04-14T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 2,
                                  "date": "2025-04-15T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 3,
                                  "date": "2025-04-16T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 4,
                                  "date": "2025-04-17T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 5,
                                  "date": "2025-04-18T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 6,
                                  "date": "2025-04-19T00:00:00.000+00:00",
                                  "contributionCount": 0
                                }
                              ]
                            },
                            {
                              "contributionDays": [
                                {
                                  "weekday": 0,
                                  "date": "2025-04-20T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 1,
                                  "date": "2025-04-21T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 2,
                                  "date": "2025-04-22T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 3,
                                  "date": "2025-04-23T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 4,
                                  "date": "2025-04-24T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 5,
                                  "date": "2025-04-25T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 6,
                                  "date": "2025-04-26T00:00:00.000+00:00",
                                  "contributionCount": 0
                                }
                              ]
                            },
                            {
                              "contributionDays": [
                                {
                                  "weekday": 0,
                                  "date": "2025-04-27T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 1,
                                  "date": "2025-04-28T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 2,
                                  "date": "2025-04-29T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 3,
                                  "date": "2025-04-30T00:00:00.000+00:00",
                                  "contributionCount": 0
                                },
                                {
                                  "weekday": 4,
                                  "date": "2025-05-01T00:00:00.000+00:00",
                                  "contributionCount": 0
                                }
                              ]
                            }
                          ]
                        }
                      }
                    }
                  }
                }
                """;

        GitHubResponse response = new GitHubResponse(200, responseBody);
        List<Map<String, Object>> listOfMaps = response.getObject("data", "user", "contributionsCollection", "contributionCalendar", "weeks");
        assertNotNull(listOfMaps);
    }
}
