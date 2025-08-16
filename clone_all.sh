#!/bin/bash
# NOTE: when executing this script from WSL an error like 'bash: ./clone_all.sh: cannot execute: required file not found'
# NOTE: this relates to windows special characters (carriage-returns) which are not clear for Linux subsystem
# NOTE: replace such characters by executing: "sed -i 's/\r$//' clone_all.sh"

# Downloads all existed repositories using paging approach (github does not work stable without paging)
i=1

while true; do
    file="all_repos_page$i.json"
    echo "Processing file $file"

    curl "https://api.github.com/orgs/Netcracker/repos?per_page=50&page=$i" > "$file"

    # exit from the loop - in case downloaded file is similar to empty json, i.e. "[]" string
    size=$(stat -c%s "$file")
    if [[ "$size" -lt "6" ]]; then
        rm -f $file
        break
    fi

    # temp exit - change per_page to 50 also when removing
    # break

    # keep this ignore-rule in sync with com.github.exadmin.ostm.github.facade.GitHubFacade.getAllRepositories
    # cat "$file" | grep -v -e 'k8s-conformance' -e 'kafka' -e 'postgres' -e 'cassandra' -e 'keycloak' | grep -e 'clone_url*' | cut -d \" -f 4 | xargs -L1 --no-run-if-empty ./clone_or_pull.sh
    cat "$file" | grep -e 'clone_url*' | cut -d \" -f 4 | xargs -L1 --no-run-if-empty ./clone_or_pull.sh
    # cat "$file" | grep -e 'clone_url*' | grep -e '/qubership-' -e '/pgskipper-' | cut -d \" -f 4 | xargs -L1 --no-run-if-empty ./clone_or_pull.sh

    i=$((i + 1))
done
