# GMU CS Course-Semester Scraper

## Purpose

The intended purpose of this tool is to find out in which semesters were specific CS classes offered.

You can use this to get an idea of when courses will be offered, to help you plan your schedule.

This program outputs in [JSON](https://www.json.org/json-en.html) format.

## Requirements

1. [Java 11](https://adoptopenjdk.net/?variant=openjdk11)
2. [Apache Maven](https://maven.apache.org/)
3. An internet connection

## Compiling

Run `mvn clean package` from the project's root directory.

The output JAR file is `./target/CS-Archive-Catalog.jar` from the project's root directory.

## Execution

1. Compile the project (see above).
2. Run the created jar file (see above for path) using `java -jar`. Provide one argument which determines the key type of the output json: `semester` or `course`.

    - If you provide `semester`, then courses will be grouped by the semesters it was offered.
    - If you provide `course`, then the semesters will be grouped by the course number.

You can save the output of this program by using redirection in your shell.

## Sample Outputs

These samples have been truncated for brevity.

1. Sorted by `semester`:

```json
{
  "Summer 2021": [
    "CS100",
    "CS112",
    "CS211",
    "CS262",
    "CS321"
  ],
  "Fall 2021": [
    "CS100",
    "CS110",
    "CS112",
    "CS211",
    "CS222",
    "CS306",
    "CS310",
    "CS321"
  ]
}
```

2. Sorted by `course`:

```json
{
  "CS797": [
    {
      "term": "FALL",
      "year": 2019
    }
  ],
  "CS800": [
    {
      "term": "FALL",
      "year": 2008
    },
    {
      "term": "FALL",
      "year": 2009
    }
  ],
  "CS803": [
    {
      "term": "SPRING",
      "year": 2009
    }
  ]
}
```

## Disclaimer

Each time you run this program, you are making real HTTP(S) requests to GMU's servers. Please be mindful about this and do not send too many requests in a short period of time. Consider saving your responses for caching.