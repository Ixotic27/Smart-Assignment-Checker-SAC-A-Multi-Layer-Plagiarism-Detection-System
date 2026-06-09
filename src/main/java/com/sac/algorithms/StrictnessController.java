package com.sac.algorithms;

import com.sac.models.SimilarityResult;

// Routes the scoring to the right set of algorithms based on strictness level.
// Easy:   Keyword Coverage only (did the student use the key terms?)
// Medium: Keyword Coverage + Concept Match (keywords + 3-word phrases)
// Hard:   Keyword Coverage + Concept Match + Sequence Match (keywords + phrases + word order)
public class StrictnessController {

    private final KeywordCoverage keywordCoverage = new KeywordCoverage();
    private final ConceptMatch conceptMatch = new ConceptMatch();
    private final SequenceMatch sequenceMatch = new SequenceMatch();

    public SimilarityResult analyze(String answerText, String studentText, String strictness,
                                     String doc1Name, String doc2Name) {

        SimilarityResult result = new SimilarityResult(doc1Name, doc2Name, strictness);
        double finalScore;

        switch (strictness.toLowerCase()) {
            case "easy":
                // Easy: only keyword coverage — did the student mention the key terms?
                double kwScore = keywordCoverage.calculateSimilarity(answerText, studentText);
                result.setKeywordCoverage(kwScore);
                finalScore = kwScore;
                break;

            case "medium":
                // Medium: keyword coverage + concept match (3-word phrases)
                double kwScoreM = keywordCoverage.calculateSimilarity(answerText, studentText);
                double cmScore = conceptMatch.calculateSimilarity(answerText, studentText);
                result.setKeywordCoverage(kwScoreM);
                result.setConceptMatch(cmScore);
                finalScore = (kwScoreM + cmScore) / 2.0;
                break;

            case "hard":
                // Hard: all three — keywords + concepts + sequence order
                double kwScoreH = keywordCoverage.calculateSimilarity(answerText, studentText);
                double cmScoreH = conceptMatch.calculateSimilarity(answerText, studentText);
                double seqScore = sequenceMatch.calculateSimilarity(answerText, studentText);
                result.setKeywordCoverage(kwScoreH);
                result.setConceptMatch(cmScoreH);
                result.setSequenceMatch(seqScore);
                finalScore = (kwScoreH * 0.3 + cmScoreH * 0.3 + seqScore * 0.4);
                break;

            default:
                throw new IllegalArgumentException("Unknown strictness level: " + strictness);
        }

        result.setFinalScore(finalScore);
        return result;
    }
}
