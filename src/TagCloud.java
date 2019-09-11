import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Taking input txt file and output html Tag Cloud. Using Standard java
 * components in this proj.
 *
 * @author Nam B Nguyen & Simon Manning
 */
public final class TagCloud {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloud() {
        // no code needed here
    }

    private static final String SEPARATORS = " \t\n\r,-.!?[]';:/()\"*\'`";

    private static final int MAX_FONT = 48;

    private static final int MIN_FONT = 11;

    /**
     * Compare Pair of Map in graphical order.
     *
     */
    private static class StringLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    /**
     * Compare Pair of Map in numerical order.
     *
     */
    private static class IntegerLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
    }

    /**
     * Take in a string of character and make it to the set.
     *
     * @param separators
     *            string of character that notified at separators
     * @return set of character separators
     */
    private static Set<Character> separators(String separators) {

        Set<Character> strSet = new HashSet<Character>();

        for (int i = 0; i < SEPARATORS.length(); i++) {
            char c = SEPARATORS.charAt(i);
            if (!strSet.contains(c)) {
                strSet.add(c);
            }
        }

        return strSet;
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code SEPARATORS}) or "separator string" (maximal length string of
     * characters in {@code SEPARATORS}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            set characters of separators
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection entries(SEPARATORS) = {}
     * then
     *   entries(nextWordOrSeparator) intersection entries(SEPARATORS) = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection entries(SEPARATORS) /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of entries(SEPARATORS)  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of entries(SEPARATORS))
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int endIndex = position;

        boolean ifSep = separators.contains(text.charAt(position));
        while (endIndex < text.length()
                && separators.contains(text.charAt(endIndex)) == ifSep) {
            endIndex++;
        }

        return text.substring(position, endIndex);

    }

    /**
     * Makes a HTML body page for the input file and output it in the generator.
     *
     * @param out
     *            the output text file we are writing to with
     *            {@code SimpleWriter}
     * @param input
     *            the input text file read in by {@code SimpleReader}
     * @param num
     *            the number of words to be generated in the tag cloud
     * @param separator
     *            Set of separator characters
     * @throws IOException
     *             problem reading input and output file
     *
     */
    private static void generateBody(int num, String out, String input,
            Set<Character> separator) throws IOException {
        PrintWriter output = new PrintWriter(
                new BufferedWriter(new FileWriter(out)));
        output.println("<html>");
        output.println("<head> ");
        output.println("<title>Top " + num + " words in " + input + "</title>");
        output.println("<link href=" + "\""
                + "http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/"
                + "projects/tag-cloud-generator/data/tagcloud.css" + "\""
                + " rel=" + "\"" + "stylesheet" + "\"" + " type =" + "\""
                + "text/css" + "\"" + ">");

        output.println("</head>");
        output.println("<body>");
        int size = countWords(input, separator).size();
        int check = num;
        if (check > size) {
            check = size;
        }
        output.println("<h2>Top " + check + " words in " + input + "</h2>");
        output.println("<hr>");

        output.println("<div class=\"cdiv\">");

        output.println("<p class =" + '"' + "cbox" + '"' + ">");
        generateSpan(output, input, num, separator);
        output.println("</p>");
        output.println("</div>");
        output.println("</body>");
        output.println("</html>");
        output.close();
    }

    /**
     * Print out the span html for the amount of top words.
     *
     * @param output
     *            the output text file we are writing to with
     *            {@code SimpleWriter}
     * @param input
     *            the input text file read in by {@code SimpleReader}
     * @param num
     *            the number of words to be generated in the tag cloud
     * @param separator
     *            Set of separator characters
     *
     */
    private static void generateSpan(PrintWriter output, String input, int num,
            Set<Character> separator) {

        Map<String, Integer> words = countWords(input, separator);
        Map<Map.Entry<String, Integer>, Integer> fontSize = assignFonts(words);

        Comparator<Map.Entry<String, Integer>> ss = new StringLT();
        Comparator<Map.Entry<String, Integer>> si = new IntegerLT();

        List<Map.Entry<String, Integer>> sortCount = new ArrayList<Map.Entry<String, Integer>>();
        List<Map.Entry<String, Integer>> sortWords = new ArrayList<Map.Entry<String, Integer>>();

        for (Map.Entry<String, Integer> k : words.entrySet()) {
            sortCount.add(0, k);
        }

        Collections.sort(sortCount, si);

        // Get the amount of nums or smaller depends on words size that user
        // requested.
        int size = sortCount.size();
        for (int i = 0; i < size && i < num; i++) {
            sortWords.add(sortCount.remove(0));
        }
        // Sort everything in alphabetical order.
        Collections.sort(sortWords, ss);
        while (sortWords.size() > 0) {
            Map.Entry<String, Integer> pair = sortWords.remove(0);
            output.println("<span style=" + '"' + "cursor:default" + '"'
                    + " class=" + '"' + 'f' + fontSize.get(pair).toString()
                    + '"' + " title=" + '"' + "count: " + pair.getValue() + '"'
                    + ">" + pair.getKey() + "</span>");
        }

    }

    /**
     * This function take in the Map that already had words and their counts, to
     * assign the font size for them.
     *
     * @param words
     *            Map contains words and their counts. {@Map<String,Integer>}
     * @return A map with each pair of word and their font size value.
     *         {@Map<Map.Pair<String, Integer>, Integer>}
     */
    private static Map<Map.Entry<String, Integer>, Integer> assignFonts(
            Map<String, Integer> words) {

        Map<Map.Entry<String, Integer>, Integer> fonts = new HashMap<>();

        // Have to make it floating number so we can change it to integer later
        // and we can assign font size propotinally
        int max = maxCount(words);

        Iterator<Map.Entry<String, Integer>> it = words.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = it.next();
            Integer value = pair.getValue();
            // Can't never be smaller than MIN_FONT or bigger than MAX_FONT
            int font = (value * (MAX_FONT - MIN_FONT) / max) + MIN_FONT;
            fonts.put(pair, font);
        }

        return fonts;

    }

    /**
     * Take in a map with each word and value as its count and return the
     * maximum counts.
     *
     * @param words
     *            Map contains words and their counts. {@Map<String,Integer>}
     * @return Return an integer of maximum count of one word out of every words
     *         {@ Integer}
     */
    private static int maxCount(Map<String, Integer> words) {
        int maxCount = 0;
        for (Integer value : words.values()) {
            if (value > maxCount) {
                maxCount = value;
            }
        }
        return maxCount;
    }

    /**
     * Take in the input file and count the amount of times each words appear.
     *
     * @param input
     *            the input text file read in by
     *
     * @param separators
     *            set<Charactors> for separators
     *
     * @return the map that contains every words in the input file and their
     *         count
     */
    private static Map<String, Integer> countWords(String input,
            Set<Character> separators) {

        Map<String, Integer> words = new HashMap<String, Integer>();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(input));
        } catch (IOException e) {
            System.err.println("Error opening file");
        }

        if (in != null) {
            try {
                String line = in.readLine();
                while (line != null) {
                    line = line.toLowerCase();

                    int position = 0;

                    while (position < line.length()) {
                        String token = nextWordOrSeparator(line, position,
                                separators);
                        if (!separators.contains(token.charAt(0))) {
                            // If words don't have declare key and add 1.
                            if (!words.containsKey(token)) {
                                words.put(token, 1);
                            } else { // If words already existed, incremented by 1.
                                words.put(token, words.get(token) + 1);
                            }
                        }
                        position += token.length();
                    }
                    line = in.readLine();
                }
            } catch (IOException e) {
                System.err.println("Error reading file");
            }
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error closing file");
            }
        }

        return words;

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.print("Please Enter the name of the input file: ");
        String input = scan.nextLine();

        System.out.print("Please Enter the name of the output file: ");
        String output = scan.nextLine();

        System.out.print(
                "Please enter a positive number of words to be generated in "
                        + "the tag cloud: ");
        int num = scan.nextInt();

        Set<Character> separator = separators(SEPARATORS);

        try {
            generateBody(num, output, input, separator);
        } catch (IOException e) {
            System.err.println("Error from input output");
        }
        scan.close();

    }

}
