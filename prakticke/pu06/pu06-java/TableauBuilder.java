import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

class TableauBuilder {
    public Tableau build(SignedFormula[] sfs) {
        Tableau t = new Tableau();

        var initial = t.addInitial(sfs);
        var alphas = new LinkedList<Node>();
        var betas = new LinkedList<Node>();
        var branch = new ArrayList<Node>();

        for (var node: initial)
            if (processNode(node, alphas, betas, branch))
                return t;

        expand(t, branch.getLast(), alphas, betas, branch);
        return t;
    }

    private boolean processNode(Node n, Deque<Node> alphas, Deque<Node> betas, ArrayList<Node> branch) {
        var compl = n.sf().neg();
        if (n.sf().f() instanceof AtomicFormula) {
            for (var node: branch) {
                if (node.sf().equals(compl)) {
                    n.close(node);
                    return true;
                }
            }
        }

        branch.add(n);

        if (n.sf().type() == SignedFormula.Type.Alpha)
            alphas.add(n);
        else if (n.sf().type() == SignedFormula.Type.Beta)
            betas.add(n);

        return false;
    }

    private void expand(Tableau t, Node leaf, Deque<Node> alphas, Deque<Node> betas, ArrayList<Node> branch) {
        var newLeaf = leaf;
        while (!alphas.isEmpty()) {
            var alpha = alphas.poll();
            for (int i = 0; i < alpha.sf().subfs().size(); i++) {
                newLeaf = t.extendAlpha(newLeaf, alpha, i);
                if (processNode(newLeaf, alphas, betas, branch))
                    return;
            }
        }

        if (betas.isEmpty())
            return;

        var beta = betas.poll();
        var newBetas = t.extendBeta(newLeaf, beta);
        for (var newBeta: newBetas) {
            var branchCopy = new ArrayList<>(branch);
            var betasCopy = new LinkedList<>(betas);
            var alphasCopy = new LinkedList<>(alphas);
            processNode(newBeta, alphasCopy, betasCopy, branchCopy);
            expand(t, newBeta, alphasCopy, betasCopy, branchCopy);
        }
    }
}
