#!/bin/bash

# Check if repository URL is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <repository-url>"
    exit 1
fi

# Extract repository name from URL
repo_url=$1
repo_name=$(basename "$repo_url" .git)

# Check if directory exists
if [ -d "$repo_name" ]; then
    echo "Repository $repo_name already exists. Pulling latest changes..."
    cd "$repo_name" || exit
    git pull
else
    echo "Cloning repository $repo_name..."
    git clone "$repo_url"
fi
