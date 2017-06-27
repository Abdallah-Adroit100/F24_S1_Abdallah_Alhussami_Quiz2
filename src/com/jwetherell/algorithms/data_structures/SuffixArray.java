package com.jwetherell.algorithms.data_structures;

import java.util.ArrayList;
import java.util.Collections;

/**
 * In computer science, a suffix array is a sorted array of all suffixes of a string.
 * It is a data structure used, among others, in full text indices, data compression
 * algorithms and within the field of bibliometrics.
 *
 * https://en.wikipedia.org/wiki/Suffix_array
 *
 * This implementation returns starting indexes instead of full suffixes
 *
 * @author Jakub Szarawarski <kubaszarawarski@gmail.com>
 */
public class SuffixArray {
    private static final char DEFAULT_END_SEQ_CHAR = '$';
    private char END_SEQ_CHAR;
    private String string;
    private ArrayList<Integer> suffixArray = null;
    private ArrayList<Integer> KMRarray = null;

    public SuffixArray(CharSequence sequence) {
        this(sequence, DEFAULT_END_SEQ_CHAR);
    }

    public SuffixArray(CharSequence sequence, char endChar) {
        END_SEQ_CHAR = endChar;
        string = buildStringWithEndChar(sequence);
    }

    public ArrayList<Integer> getSuffixArray() {
        if(suffixArray == null){
            KMRalgorithm();
        }
        return suffixArray;
    }

    /**
     * @return inverted suffix array
     */
    public ArrayList<Integer> getKMRarray() {
        if (KMRarray == null) {
            KMRalgorithm();
        }
        return KMRarray;
    }

    /**
     * Creates suffix array using KMR algorithm with O(n log^2 n) complexity.
     *
     * For radius r:
     *      KMR[i] == k,
     *      when string[i..i+r-1] is kth r-letter substring of string sorted lexicographically
     * KMR is counted for radius = 1,2,4,8 ...
     * KMR for radius bigger than string length is the inverted suffix array
     */
    private void KMRalgorithm() {
        int length = string.length();

        ArrayList<Integer> KMR = getBasicKMR(length);
        ArrayList<KMRsWithIndex> KMRinvertedList = new ArrayList<KMRsWithIndex>();

        int radius = 1;

        while(radius < length){
            KMRinvertedList = getKMRinvertedList(KMR, radius, length);
            KMR = getKMR(KMRinvertedList, radius, length);
            radius *= 2;
        }

        KMRarray = new ArrayList<Integer>(KMR.subList(0, length));
        suffixArray = new ArrayList<Integer>();
        KMRinvertedList.forEach(k -> suffixArray.add(k.index));
    }

    /**
     * Creates KMR array for new radius from nearly inverted array.
     * Elements from inverted array need to be grouped by substring tey represent.
     *
     * @param KMRinvertedList       indexes are nearly inverted KMR array
     * @param length                string length
     * @return KMR array for new radius
     */
    private ArrayList<Integer> getKMR(ArrayList<KMRsWithIndex> KMRinvertedList, int radius, int length) {
        ArrayList<Integer> KMR = new ArrayList<Integer>(length*2);
        for(int i=0; i<2*length; i++) KMR.add(-1);

        int counter = 0;
        for(int i=0; i<length; i++){
            if(i>0 && substringsAreEqual(KMRinvertedList, i))
                counter++;
            KMR.set(KMRinvertedList.get(i).index, counter);
        }

        return KMR;
    }

    private boolean substringsAreEqual(ArrayList<KMRsWithIndex> KMRinvertedList, int i) {
        return KMRinvertedList.get(i-1).beginKMR.equals(KMRinvertedList.get(i).beginKMR) == false ||
            KMRinvertedList.get(i-1).endKMR.equals(KMRinvertedList.get(i).endKMR) == false;
    }

    /**
     * helper method to create KMR array for radius = radius from KMR array for radius = radius/2
     *
     * @param KMR       KMR array for radius = radius/2
     * @param radius    new radius
     * @param length    string length
     * @return list of KMRsWithIndex which indexes are nearly inverted KMR array
     */
    private ArrayList<KMRsWithIndex> getKMRinvertedList(ArrayList<Integer> KMR, int radius, int length) {
        ArrayList<KMRsWithIndex> KMRinvertedList = new ArrayList<KMRsWithIndex>();

        for(int i=0; i<length; i++){
            KMRinvertedList.add(new KMRsWithIndex(KMR.get(i), KMR.get(i+radius), i));
        }

        Collections.sort(KMRinvertedList, (A, B) -> {
            if(A.beginKMR.equals(B.beginKMR) == false){
                return A.beginKMR.compareTo(B.beginKMR);
            }
            if(A.endKMR.equals(B.endKMR) == false){
                return A.endKMR.compareTo(B.endKMR);
            }
            return A.index.compareTo(B.index);
        });

        return KMRinvertedList;
    }

    /**
     * KMR array for radius=1, instead of initial natural numbers ascii codes are used
     *
     * @param length        length of string
     * @return pseudo KMR array for radius=1
     */
    private ArrayList<Integer> getBasicKMR(int length) {
        ArrayList<Integer> result = new ArrayList<Integer>(length*2);
        char[] characters = string.toCharArray();
        for(int i=0; i<length; i++){
            result.add(new Integer(characters[i]));
        }
        for(int i=0; i<length; i++){
            result.add(-1);
        }

        return result;
    }

    private String buildStringWithEndChar(CharSequence sequence) {
        StringBuilder builder = new StringBuilder(sequence);
        if (builder.indexOf(String.valueOf(END_SEQ_CHAR)) < 0)
            builder.append(END_SEQ_CHAR);
        return builder.toString();
    }

    private class KMRsWithIndex{
        Integer beginKMR;
        Integer endKMR;
        Integer index;

        KMRsWithIndex(Integer begin, Integer end, Integer index){
            this.beginKMR = begin;
            this.endKMR = end;
            this.index = index;
        }
    }
}