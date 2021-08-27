package com.blakekhan.gmu.csarchivecatalog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Blake Khan
 */
@RequiredArgsConstructor
public class SyllabusScraper implements Callable<SemesterCoursesResult> {

    public static final Logger LOGGER = Logger.getLogger(SyllabusScraper.class.getName());
    private static final String USER_AGENT = "Mozilla/5.0";

    static {
        LOGGER.setLevel(Level.WARNING);
        LOGGER.addHandler(new ConsoleHandler());
    }

    @NonNull
    private final Logger logger;

    @NonNull
    private final String prefixUri;

    @NonNull
    private final Semester semester;

    private final int startingYearInclusive;
    private final int endingYearInclusive;

    private Optional<Document> connectAndGetDocument() {
        int year = semester.getYear();
        String url = prefixUri + WordUtils.capitalizeFully(semester.getTerm().name()) + year;

        // Validate year
        if (year > endingYearInclusive || year < startingYearInclusive) {
            logger.log(Level.WARNING, String.format("%s is out of range. (Attempted to connect to %s)", year, url));
            return Optional.empty();
        }

        // Connect and download document
        try {
            Connection connection = SSLHelper.getConnection(url).userAgent(USER_AGENT);
            return Optional.ofNullable(connection.get());
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, String.format("could not connect to %s", url));
        }

        return Optional.empty();
    }

    public List<String> getCoursesBySemester() {
        Optional<Document> documentOptional = connectAndGetDocument();

        if (!documentOptional.isPresent()) {
            logger.log(Level.WARNING, String.format("document does not exist for %s", semester));
            return Collections.emptyList();
        }

        Document doc = documentOptional.get();
        Element table = doc.selectFirst("table.syllabus-table");

        if (table == null) {
            logger.log(Level.WARNING, String.format("no table found for %s", semester));
            return Collections.emptyList();
        }

        Elements rows = table.select("tr");

        if (rows.isEmpty()) {
            logger.log(Level.WARNING, String.format("no rows found for %s", semester));
            return Collections.emptyList();
        }

        Set<String> courses = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);

            // Skip headers
            if (i == 0) {
                continue;
            }

            Element data = row.selectFirst("td");

            if (data == null) {
                logger.log(Level.WARNING, String.format("table data not found (index %o, %s)", i, semester));
                continue;
            }

            Element hyperlink = data.selectFirst("a[href]");

            if (hyperlink == null) {
                continue;
            }

            // Get course name (remove spaces, make uppercase)
            String course = hyperlink.text().trim().replaceAll(" ", "").toUpperCase();
            courses.add(course);
        }

        List<String> list = new ArrayList<>(courses);
        Collections.sort(list);

        return list;
    }

    @Override
    public SemesterCoursesResult call() throws Exception {
        SemesterCoursesResult result = new SemesterCoursesResult(semester);
        result.getCourses().addAll(getCoursesBySemester());
        return result;
    }
}
