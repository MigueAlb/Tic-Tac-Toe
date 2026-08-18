package espol.com.tresenraya.ai;

import espol.com.tresenraya.model.Move;
import espol.com.tresenraya.structures.Tree;

import java.util.List;

public class Decision {
    private Move move;
    private int minimaxValue;
    private Tree<BoardState> decisionTree;
    private List<CandidateScore> candidates;

    public Decision(Move move, int minimaxValue, Tree<BoardState> decisionTree, List<CandidateScore> candidates) {
        this.move = move;
        this.minimaxValue = minimaxValue;
        this.decisionTree = decisionTree;
        this.candidates = candidates;
    }

    public Move getMove() {
        return move;
    }

    public int getMinimaxValue() {
        return minimaxValue;
    }

    public Tree<BoardState> getDecisionTree() {
        return decisionTree;
    }

    public List<CandidateScore> getCandidates() {
        return candidates;
    }

    public static class CandidateScore {
        private Move move;
        private int worstUtility;

        public CandidateScore(Move move, int worstUtility) {
            this.move = move;
            this.worstUtility = worstUtility;
        }

        public Move getMove() {
            return move;
        }

        public int getWorstUtility() {
            return worstUtility;
        }
    }
}

