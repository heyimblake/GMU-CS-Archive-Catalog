package com.blakekhan.gmu.csarchivecatalog;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blake Khan
 */
@EqualsAndHashCode
@Data
@RequiredArgsConstructor
public class SemesterCoursesResult {

    @NonNull
    private final Semester semester;

    @NonNull
    private final List<String> courses;

    public SemesterCoursesResult(Semester semester) {
        this(semester, new ArrayList<>());
    }
}
