import json
import sys
from pathlib import Path


def require_object(value, path):
    if not isinstance(value, dict):
        raise ValueError(f"{path} must be an object")


def require_string(value, path):
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{path} must be a non-empty string")


def require_array(value, path):
    if not isinstance(value, list):
        raise ValueError(f"{path} must be an array")


def validate(document):
    require_object(document, "root")
    require_array(document.get("columns"), "columns")

    for column_index, column in enumerate(document["columns"]):
        column_path = f"columns[{column_index}]"
        require_object(column, column_path)
        require_string(column.get("column-id"), f"{column_path}.column-id")
        require_array(column.get("repositories"), f"{column_path}.repositories")

        for repository_index, repository in enumerate(column["repositories"]):
            repository_path = f"{column_path}.repositories[{repository_index}]"
            require_object(repository, repository_path)
            for field in ("name", "visual-value", "sort-by-value"):
                require_string(repository.get(field), f"{repository_path}.{field}")


def main():
    if len(sys.argv) != 2:
        print(f"Usage: {Path(sys.argv[0]).name} <json-file>", file=sys.stderr)
        return 2

    json_path = Path(sys.argv[1])
    try:
        with json_path.open(encoding="utf-8") as json_file:
            document = json.load(json_file)
        validate(document)
    except (OSError, json.JSONDecodeError, ValueError) as error:
        print(f"Invalid grand report overrides in {json_path}: {error}", file=sys.stderr)
        return 1

    print(f"Validated JSON structure in {json_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
