package com.blakekhan.gmu.csarchivecatalog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author Blake Khan
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Must provide 1 argument (key type): \"semester\" or \"course\"");
            return;
        }

        boolean keySemester = args[0].equalsIgnoreCase("semester");
        boolean keyCourse = args[0].equalsIgnoreCase("course");

        // Check key type
        if (!keySemester && !keyCourse) {
            System.err.println("ERROR: Invalid argument.");
            return;
        }

        // Start and End years
        int startInclusive = 2007;
        int endInclusive = LocalDate.now().getYear();

        // Each thread/callable is a request to a semester's syllabi page
        ExecutorService executorService = Executors.newFixedThreadPool((endInclusive - startInclusive + 1) * Term.values().length);
        List<SyllabusScraper> scrapers = populateScrapers(startInclusive, endInclusive);
        List<Future<SemesterCoursesResult>> futures = invokeScrapers(scrapers, executorService);
        List<SemesterCoursesResult> semesterCoursesResults = blockAndGetResults(futures);

        // Shutdown executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Determine what to print out
        if (keySemester) {
            System.out.println(gson.toJson(getSemesterCourses(semesterCoursesResults)));
        } else {
            System.out.println(gson.toJson(getCourseSemesters(semesterCoursesResults)));
        }
    }

    private static List<SemesterCoursesResult> blockAndGetResults(List<Future<SemesterCoursesResult>> futures) {
        List<SemesterCoursesResult> results = new ArrayList<>();

        for (Future<SemesterCoursesResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private static List<SyllabusScraper> populateScrapers(int startingYearInclusive, int endingYearInclusive) {
        List<SyllabusScraper> scrapers = new LinkedList<>();

        // Iterate over year and term
        IntStream.rangeClosed(startingYearInclusive, endingYearInclusive).boxed().forEach(year -> {
            Arrays.stream(Term.values()).forEach(term -> {
                Semester semester = new Semester(term, year);
                scrapers.add(new SyllabusScraper(SyllabusScraper.LOGGER, "https://cs.gmu.edu/syllabi/", semester, startingYearInclusive, endingYearInclusive));
            });
        });

        return Collections.unmodifiableList(scrapers);
    }

    private static List<Future<SemesterCoursesResult>> invokeScrapers(@NonNull List<SyllabusScraper> scrapers, @NonNull ExecutorService executorService) {
        try {
            return executorService.invokeAll(scrapers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private static Map<Semester, List<String>> getSemesterCourses(List<SemesterCoursesResult> results) {
        // Map to store results
        Map<Semester, List<String>> map = new TreeMap<>();
        results.forEach(result -> map.put(result.getSemester(), result.getCourses()));
        // Return results
        return map;
    }

    private static Map<String, List<Semester>> getCourseSemesters(List<SemesterCoursesResult> results) {
        Map<Semester, List<String>> semesterCourseMap = getSemesterCourses(results);
        Map<String, List<Semester>> courseSemesterMap = new TreeMap<>();

        for (Map.Entry<Semester, List<String>> entry : semesterCourseMap.entrySet()) {
            List<String> courses = entry.getValue();
            Semester semester = entry.getKey();

            courses.forEach(course -> {
                if (!courseSemesterMap.containsKey(course)) {
                    courseSemesterMap.put(course, new ArrayList<>());
                }

                courseSemesterMap.get(course).add(semester);
                Collections.sort(courseSemesterMap.get(course));
            });
        }

        return courseSemesterMap;
    }
}
