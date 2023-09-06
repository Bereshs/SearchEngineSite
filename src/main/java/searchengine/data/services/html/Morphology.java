package searchengine.data.services.html;

import lombok.Data;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Data
public class Morphology {

    private final HashMap<String, Integer> lemmas;

    private final Logger logger = Logger.getLogger(Morphology.class.getName());

    public Morphology() {
        this.lemmas = new HashMap<>();
    }


    public void createLemmasMap(String text) throws IOException {
        String withoutPunctText = text.replaceAll("\\p{Punct}", "").toLowerCase()+" ";
        logger.info("Creating lemmas, received text " + withoutPunctText.length() + "bytes ");
        int firstIndex = 0;
        int lastIndex;
        while (firstIndex < withoutPunctText.length()) {
            lastIndex = withoutPunctText.indexOf(' ', firstIndex);
            if (lastIndex < 0) {
                lastIndex = withoutPunctText.length() - 1;
            }
            addToLemmasList(createLemmasList(withoutPunctText
                    .substring(firstIndex, lastIndex)));
            firstIndex = lastIndex + 1;
        }

        logger.info("created " + getLemmas().size());

    }



    public List<String> createLemmasList(String word) throws IOException {
        LuceneMorphology luceneMorph = getLuceneMorphology(word);
        if (luceneMorph == null) {
            return new ArrayList<>();
        }
        List<String> info = luceneMorph.getMorphInfo(word);
        String wordInfo = info.get(0);
        if (isNotWord(wordInfo)) {
            return new ArrayList<>();
        }
        return luceneMorph.getNormalForms(word);
    }

    private boolean isNotWord(String wordInfo) {
        return wordInfo.contains("МЕЖД")
                || wordInfo.contains("СОЮЗ")
                || wordInfo.contains("МС")
                || wordInfo.contains("VERB")
                || wordInfo.contains("ВВОДН")
                || wordInfo.contains("ЧАСТ")
                || wordInfo.contains("ПРЕДЛ");
    }

    private void addToLemmasList(List<String> lemmasList) {
        lemmasList.forEach(this::increaseLemma);
    }

    private void increaseLemma(String lemma) {
        int value = 1;
        if (lemmas.containsKey(lemma)) {
            value = lemmas.get(lemma) + 1;
        }
        lemmas.put(lemma, value);
    }

    private LuceneMorphology getLuceneMorphology(String word) throws IOException {
        if (word.length() <= 2) {
            return null;
        }
        String ru = "[а-яА-Я]+";
        String en = "[a-zA-z]+";
        if (word.matches(ru)) {
            return new RussianLuceneMorphology();
        } else if (word.matches(en)) {
            return new EnglishLuceneMorphology();
        }
        return null;
    }

}
