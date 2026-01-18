# Opensource Team Monitor
Allows building reports about open-source team effectiveness

# Setup local-dev env:
Add following code into settings.xml (c:\Users\YOUR_PROFILE\.m2\settings.xml for Windows)

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