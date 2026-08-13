# Opensource Team Monitor
Allows building reports about open-source team effectiveness

## Local development setup

Add the following configuration to `settings.xml` (`c:\Users\YOUR_PROFILE\.m2\settings.xml` on Windows):

```xml
    <settings>
    <!-- ... other settings ... -->
    <servers>
        <server>
            <id>github</id>
            <username>USER_NAME</username>
            <password>ACCESS_TOKEN</password>
        </server>
    </servers>
    <!-- ... other settings ... -->
</settings>
```

## Running OSTM

Build the application, then run it with six positional arguments:

```shell
mvn -B package
java -jar target/ostm.jar \
  GITHUB_TOKEN_OR_FILE \
  CLONED_REPOSITORIES_DIRECTORY \
  OUTPUT_REPORT_FILE \
  GITHUB_RESPONSE_CACHE_DIRECTORY \
  REPORT_OVERRIDES_FILE \
  CYBER_FERRET_CLI_PATH
```

`CYBER_FERRET_CLI_PATH` is the absolute path to a readable `cfcli` executable.

CyberFerret also uses these environment variables:

- `CYBER_FERRET_PASSWORD` is the required dictionary password. OSTM passes it to CyberFerret through the child
  process environment, not as a command-line argument.
- `CYBER_FERRET_TIMEOUT_SECONDS` is an optional positive per-command timeout that must be representable in
  milliseconds. The default is 300 seconds.

OSTM runs `cfcli --mode=quick` for every repository. Findings appear as warnings in the report and do not make OSTM
fail. Exit code `2` means one or more scans were incomplete; OSTM still writes the partial report before exiting.

## Scheduled report workflow

The scheduled workflow downloads the latest `cfcli` release, generates a fresh report in an isolated temporary
directory, and publishes completed reports for OSTM exit codes `0` and `2`.
It runs a final status gate after publication, so a partial report is published before the workflow fails and triggers
GitHub's normal failure notifications. An unchanged report is a successful publication outcome.
