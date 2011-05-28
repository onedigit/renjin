/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import com.google.common.collect.Sets;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.Set;

/**
 * Default implementations of match() related functions.
 */
public class Match {

  private static final int UNMATCHED = -1;

  private Match() { }

  /**
   * match returns a vector of the positions of (first) matches of its first argument in its second.
   * @param search vector or NULL: the values to be matched.
   * @param table vector or NULL: the values to be matched against.
   * @param noMatch the value to be returned in the case when no match is found. Note that it is coerced to integer.
   * @param incomparables a vector of values that cannot be matched. Any value in x matching a value in this vector is assigned the nomatch value.
   *        For historical reasons, FALSE is equivalent to NULL.
   * @return
   */
  public static int[] match(AtomicVector search, AtomicVector table, int noMatch, AtomicVector incomparables) {
    //For historical reasons, FALSE is equivalent to NULL.
    if(incomparables.equals( LogicalVector.FALSE ) ) {
      incomparables = Null.INSTANCE;
    }

    int[] matches = new int[search.length()];
    for(int i=0;i!=search.length();++i) {
      if( incomparables.contains(search, i)) {
        matches[i] = noMatch;
      } else {
        int pos = table.indexOf(search, i, 0);
        matches[i] = pos >= 0 ? pos+1 : noMatch;
      }
    }
    return matches;
  }

  /**
   * pmatch seeks matches for the elements of its first argument among those of its second.
   *
   * The behaviour differs by the value of duplicates.ok. Consider first the case
   * if this is true. First exact matches are considered, and the positions of the
   * first exact matches are recorded. Then unique partial matches are considered,
   * and if found recorded. (A partial match occurs if the whole of the element of x
   * matches the beginning of the element of table.) Finally, all remaining elements of
   * x are regarded as unmatched. In addition, an empty string can match nothing, not even an
   * exact match to an empty string. This is the appropriate behaviour for partial matching
   * of character indices, for example.
   *
   * <p>If duplicates.ok is FALSE, values of table once matched are excluded from the
   * search for subsequent matches. This behaviour is equivalent to the R algorithm
   * for argument matching, except for the consideration of empty strings (which in
   * argument matching are matched after exact and partial matching to any remaining arguments).
   *
   * @param x the values to be matched
   * @param table the values to be matched against: converted to a character vector.
   * @param noMatch the value to be returned at non-matching or multiply partially matching positions.
   * @param duplicatesOk should elements be in table be used more than once?
   * @return An integer vector (possibly including NA if nomatch = NA) of the same length as x,
   * giving the indices of the elements in table which matched, or {@code nomatch}.
   */
  public static IntVector pmatch(StringVector x, StringVector table, int noMatch, boolean duplicatesOk) {
    IntVector.Builder result = new IntVector.Builder(x.length());
    boolean matchedTable[] = new boolean[table.length()];
    boolean matchedSearch[] = new boolean[x.length()];

    // first pass : exact matches
    for(int i=0;i!= x.length();++i) {
      String toMatch = pmatchElementAt(x, i);
      int match = exactMatch(toMatch, table);
      if(match != UNMATCHED && (duplicatesOk || !matchedTable[match])) {
        result.set(i, match+1);
        matchedTable[match] = true;
        matchedSearch[i] = true;
      }
    }

    // second pass : partial matches
    for(int i=0;i!= x.length();++i) {
      if(!matchedSearch[i]) {
        String toMatch = pmatchElementAt(x, i);
        int match = uniquePartialMatch(toMatch, table);
        if(match != UNMATCHED && (duplicatesOk || !matchedTable[match])) {
          result.set(i, match+1);
          matchedTable[match] = true;
        } else {
          result.set(i, noMatch);
        }
      }
    }
    return result.build();
  }

  private static int exactMatch(String toMatch, StringVector table) {
    for(int i=0;i!=table.length();++i) {
         String t = pmatchElementAt(table, i);
         if(toMatch.equals(t)) {
           return i;
         }
    }
    return -1;
  }

  private static int uniquePartialMatch(String toMatch, StringVector table) {
    int partialMatch = UNMATCHED;
    for(int i=0;i!=table.length();++i) {
      String t = pmatchElementAt(table, i);
      if(t.startsWith(toMatch)) {
        // if we've previously found a partial match, abort
        if(partialMatch != UNMATCHED) {
          return UNMATCHED;
        }
        partialMatch = i;
      }
    }
    return partialMatch;
  }

  // NA values are treated as if they were the string constant "NA".
  private static String pmatchElementAt(StringVector vector, int i) {
    return vector.isElementNA(i) ? "NA" : vector.getElementAsString(i);
  }

  /**
   * unique returns a vector, data frame or array like x but with duplicate elements/rows removed.
   *
   * @param x a vector
   * @param incomparables a vector of values that cannot be compared. FALSE is a special value,
   *          meaning that all values can be compared, and may be the only value accepted for methods
   *        other than the default. It will be coerced internally to the same type as x.
   * @param fromLast
   * @return logical indicating if duplication should be considered from the last, i.e., the last
   *       (or rightmost) of identical elements will be kept. This only matters for names or dimnames.
   */
  public static Vector unique(AtomicVector x, AtomicVector incomparables, boolean fromLast) {
    Vector.Builder result = x.newBuilder(0);
    int resultIndex=0;
    for(int i=0;i!= x.length();++i) {
      if(   incomparables.contains(x, i) ||
          (fromLast && x.indexOf(x, i, i+1) == -1) ||
          (!fromLast && x.indexOf(x, i, 0) == i)) {

        result.setFrom(resultIndex++, x, i);

      }
    }
    return result.build();
  }

  /**
   * Determines which elements of a vector or data frame are duplicates of elements with smaller
   * subscripts, and returns a logical vector indicating which elements (rows) are duplicates.
   * @param x a vector
   * @param incomparables a vector of values that cannot be compared. FALSE is a special value, meaning
   *        that all values can be compared, and may be the only value accepted for methods
   *      other than the default. It will be coerced internally to the same type as x.
   * @param fromLast logical indicating if duplication should be considered from the reverse side, i.e.,
   *      the last (or rightmost) of identical elements would correspond to duplicated=FALSE.
   * @return a non-negative integer (of length one).
   */
  public static int anyDuplicated(Vector x, AtomicVector incomparables, boolean fromLast) {

    if(incomparables instanceof LogicalVector && incomparables.length() == 1 &&
        incomparables.getElementAsLogical(0).equals(Logical.FALSE)) {
      incomparables = Null.INSTANCE;
    }

    if(incomparables != Null.INSTANCE) {
      throw new EvalException("incomparables in anyDuplicated not yet supported!");
    }

    Set<Object> seen = Sets.newHashSet();
    if(fromLast) {
      for(int i=x.length()-1;i>=0;--i) {
        Object element = x.getElementAsObject(i);
        if(seen.contains(element)) {
          return i+1;
        } else {
          seen.add(element);
        }
      }
    } else {
      for(int i=0;i!=x.length();++i) {
        Object element = x.getElementAsObject(i);
        if(seen.contains(element)) {
          return i+1;
        } else {
          seen.add(element);
        }
      }
    }
    return 0;
  }
}
