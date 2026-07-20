# Opensource Team Monitor
Allows building reports about open-source team effectiveness

# Local development setup

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

# Running OSTM

Build the application, then run it with five positional arguments:

```shell
mvn -B package
java -jar target/ostm.jar \
  GITHUB_TOKEN_OR_FILE \
  CLONED_REPOSITORIES_DIRECTORY \
  OUTPUT_REPORT_FILE \
  GITHUB_RESPONSE_CACHE_DIRECTORY \
  REPORT_OVERRIDES_FILE
```

CyberFerret is configured through environment variables:

- `CYBER_FERRET_CLI_PATH` is the required absolute path to a readable `cyberferret-cli.jar`.
- `CYBER_FERRET_PASSWORD` is the required dictionary password. OSTM passes it to CyberFerret through the child
  process environment, not as a command-line argument.
- `CYBER_FERRET_CACHE_DIR` is an optional absolute parent directory for the run-specific dictionary snapshot. The
  system temporary directory is used when this variable is absent.
- `CYBER_FERRET_TIMEOUT_SECONDS` is an optional positive per-command timeout. The default is 300 seconds.

OSTM asks CyberFerret for the dictionary version once, then scans every repository in offline quick mode against the
same run-specific snapshot. Findings appear as warnings in the report and do not make OSTM fail. Exit code `2` means
one or more scans or the metadata lookup were incomplete; OSTM still writes the partial report before exiting.

# Scheduled report workflow

The scheduled workflow downloads a versioned CyberFerret CLI release, verifies its SHA-256 checksum, generates a
fresh report in an isolated temporary directory, and publishes completed reports for OSTM exit codes `0` and `2`.
It runs a final status gate after publication, so a partial report is published before the workflow fails and triggers
GitHub's normal failure notifications. An unchanged report is a successful publication outcome.

The workflow intentionally contains `SET_AFTER_CYBERFERRET_RELEASE` in `CYBERFERRET_VERSION` and
`CYBERFERRET_SHA256`. Replace both values with a released version and checksum before marking the integration ready.
