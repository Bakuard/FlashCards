package com.bakuard.flashcards.dal.impl;

import java.util.Arrays;

public class StoredProcedures {

    public static int levenshteinDistance(String left, String right, int threshold) {
        if (left != null && right != null) {
            if (threshold < 0) {
                throw new IllegalArgumentException("Threshold must not be negative");
            } else {
                int n = left.length();
                int m = right.length();
                if (n == 0) {
                    return m <= threshold ? m : -1;
                } else if (m == 0) {
                    return n <= threshold ? n : -1;
                } else {
                    if (n > m) {
                        String tmp = left;
                        left = right;
                        right = tmp;
                        n = m;
                        m = tmp.length();
                    }

                    if (m - n > threshold) {
                        return -1;
                    } else {
                        int[] p = new int[n + 1];
                        int[] d = new int[n + 1];
                        int boundary = Math.min(n, threshold) + 1;

                        int j;
                        for(j = 0; j < boundary; p[j] = j++) {
                        }

                        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
                        Arrays.fill(d, Integer.MAX_VALUE);

                        for(j = 1; j <= m; ++j) {
                            char rightJ = right.charAt(j - 1);
                            d[0] = j;
                            int min = Math.max(1, j - threshold);
                            int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(n, j + threshold);
                            if (min > 1) {
                                d[min - 1] = Integer.MAX_VALUE;
                            }

                            int lowerBound = Integer.MAX_VALUE;

                            for(int i = min; i <= max; ++i) {
                                if (left.charAt(i - 1) == rightJ) {
                                    d[i] = p[i - 1];
                                } else {
                                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
                                }

                                lowerBound = Math.min(lowerBound, d[i]);
                            }

                            if (lowerBound > threshold) {
                                return -1;
                            }

                            int[] tempD = p;
                            p = d;
                            d = tempD;
                        }

                        if (p[n] <= threshold) {
                            return p[n];
                        } else {
                            return -1;
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("CharSequences must not be null");
        }
    }

}
