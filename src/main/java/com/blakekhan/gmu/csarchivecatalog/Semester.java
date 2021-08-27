package com.blakekhan.gmu.csarchivecatalog;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;

/**
 * @author Blake Khan
 */
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class Semester implements Comparable<Semester> {

    @NonNull
    private final Term term;
    private final int year;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(term.name()) + " " + year;
    }

    @Override
    public int compareTo(Semester o) {
        if (year == o.year) {
            if (term == o.term) {
                return 0;
            }

            return Integer.compare(term.ordinal(), o.term.ordinal());
        }

        return Integer.compare(year, o.year);
    }
}
