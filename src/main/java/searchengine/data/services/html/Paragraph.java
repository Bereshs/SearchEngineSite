package searchengine.data.services.html;


import java.util.Arrays;
import java.util.List;

public class Paragraph {
    String paragraph;
    List<String> paragraphList;
    int paragraphSize=40;

    public Paragraph(String paragraph) {
        this.paragraph = paragraph;
    }

    private void getList() {
        paragraphList = Arrays.stream(paragraph.split("\\s")).toList();
    }

    private int getIndexWord(String word) {
        for (int i = 0; i < paragraphList.size(); i++) {
            if (paragraphList.get(i).toLowerCase().contains(word.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    private String makeParagraph(int index) {
        StringBuilder result = new StringBuilder();
        int start = calculateStartPosition(index);
        int end = calculateEndPosition(index,start);
        for (int i = start; i < end; i++) {
            if (i == index) {
                result.append("<b>").append(paragraphList.get(i)).append("</b> ");
                continue;
            }
            result.append(paragraphList.get(i)).append(" ");
        }
        return result.toString();
    }
    private int calculateEndPosition(int index, int start) {
        int end = index+paragraphSize/2;
        if(start==0) {
            end=end-(index-paragraphSize/2);
        }
        if(end>paragraphList.size()) {
            end= paragraphList.size();
        }
        return end;
    }


    private int calculateStartPosition(int index) {
        int start = index - paragraphSize/2;
        if (start < 0) {
            start = 0;
        }
        return start;
    }

    public String getParagraph(String word) {
        getList();
        int index = getIndexWord(word);
        return makeParagraph(index);
    }
}
