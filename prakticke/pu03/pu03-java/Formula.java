import java.util.List;
import java.util.Set;

class Constant {
    String name;

    public Constant(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public String eval(Structure m) {
        return m.iC(name());
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        Constant otherC = (Constant) other;
        return name().equals(otherC.name());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

class Formula {
    public List<Formula> subfs() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean equals(Object other) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public int deg() {
        throw new RuntimeException("Not implemented");
    }

    public Set<AtomicFormula> atoms() {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> constants() {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> predicates() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isTrue(Structure m) {
        throw new RuntimeException("Not implemented");
    }
}

class AtomicFormula extends Formula {
}

class PredicateAtom extends AtomicFormula {
    String name;
    List<Constant> args;

    PredicateAtom(String name, List<Constant> args) {
        this.name = name;
        this.args = args;
    }

    String name() {
        return name;
    }

    List<Constant> arguments() {
        return args;
    }

    @Override
    public int deg() {
        return 0;
    }
}

class EqualityAtom extends AtomicFormula {
    Constant left, right;

    EqualityAtom(Constant left, Constant right) {
        this.left = left;
        this.right = right;
    }

    Constant left() {
        return left;
    }

    Constant right() {
        return right;
    }
}

class Negation extends Formula {
    Formula originalFormula;

    Negation(Formula originalFormula) {
        this.originalFormula = originalFormula;
    }

    public Formula originalFormula() {
        return originalFormula;
    }

    @Override
    public int deg() {
        return originalFormula.deg() + 1;
    }
}

class Disjunction extends Formula {
    Disjunction(List<Formula> disjuncts) {
        throw new RuntimeException("Not implemented");
    }
}

class Conjunction extends Formula {
    Conjunction(List<Formula> conjuncts) {
        throw new RuntimeException("Not implemented");
    }
}

class BinaryFormula extends Formula {
    Formula leftSide, rightSide;
    String connective;

    BinaryFormula(Formula leftSide, Formula rightSide, String connective) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.connective = connective;
    }

    public Formula leftSide() {
        return leftSide;
    }

    public Formula rightSide() {
        return rightSide;
    }

    @Override
    public int deg() {
        return leftSide.deg() + rightSide.deg() + 1;
    }
}

class Implication extends BinaryFormula {
    Implication(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "->");
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "<->");
    }
}
