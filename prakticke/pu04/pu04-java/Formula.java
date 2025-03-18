import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        switch (this) {
            case Negation n:
                return List.of(n.originalFormula());
            case Disjunction d:
                return d.disjuncts();
            case Conjunction c:
                return c.conjuncts();
            case BinaryFormula b:
                return List.of(b.leftSide(), b.rightSide());
            default:
                return List.of();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public int deg() {
        return 1 + subfs().stream()
                           .map(f -> f.deg())
                           .collect(Collectors.summingInt(Integer::intValue));
    }

    public Set<AtomicFormula> atoms() {
        return subfs()
                .stream()
                .map(f -> f.atoms())
                .reduce(new HashSet<>(), (acc, f) -> {acc.addAll(f); return acc; });
    }

    public Set<String> constants() {
        return subfs()
                .stream()
                .map(f -> f.constants())
                .reduce(new HashSet<>(), (acc, f) -> {acc.addAll(f); return acc; });
    }

    public Set<String> predicates() {
        return subfs()
                .stream()
                .map(f -> f.predicates())
                .reduce(new HashSet<>(), (acc, f) -> {acc.addAll(f); return acc; });
    }

    @Override
    public String toString() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isTrue(Structure m) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Convert this formula to CNF.
     *
     * Note: the result of calling this method on a formula that is not in NNF is unspecified!
     *
     * @return a formula in CNF form that is equivalent / equisatisfiable to this formula.
     */
    public final Cnf toCnf() {
        return toNnf().nnfToCnf();
    }

    /**
     * Convert this formula to CNF.
     *
     * Note: the result of calling this method on a formula that is not in NNF is unspecified!
     *
     * @return a formula in CNF form that is equivalent / equisatisfiable to this formula.
     */
    public Cnf nnfToCnf() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Convert this formula to nnf.
     * @return a formula in NNF
     */
    public Formula toNnf() {
        return toNnf(false);
    }

    public Formula toNnf(boolean isNegated) {
        throw new RuntimeException("Not implemented");
    }
}

class AtomicFormula extends Formula {
    @Override
    public int deg() {
        return 0;
    }

    @Override
    public Set<AtomicFormula> atoms() {
        return Set.of(this);
    }
}

class PredicateAtom extends AtomicFormula {
    private String name;
    private List<Constant> args;

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
    public String toString() {
        return String.format("%s(%s)",
                             name(),
                             constants()
                                     .stream()
                                     .map(d -> d.toString())
                                     .collect(Collectors.joining(",")));
    }

    @Override
    public boolean isTrue(Structure m) {
        List<String> iArgs = args.stream()
                                     .map(e -> e.eval(m))
                                     .collect(Collectors.toList());

        return m.iP(name).stream().anyMatch(pa -> pa.equals(iArgs));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        PredicateAtom otherC = (PredicateAtom) other;
        return name().equals(otherC.name()) && args.equals(otherC.arguments());
    }

    @Override
    public Set<String> constants() {
        return args.stream()
                .map(c -> c.name())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> predicates() {
        return Set.of(name);
    }

    public Formula toNnf(boolean isNegated) {
        if (isNegated)
            return new Negation(new PredicateAtom(name, args));
        return new PredicateAtom(name, args);
    }

    public Cnf nnfToCnf() {
        return new Cnf(new Clause(new Literal(this)));
    }
}

class Negation extends Formula {
    private Formula originalFormula;

    Negation(Formula originalFormula) {
        this.originalFormula = originalFormula;
    }

    public Formula originalFormula() {
        return originalFormula;
    }

    @Override
    public String toString() {
        return String.format("-%s", originalFormula().toString());
    }

    @Override
    public boolean isTrue(Structure m) {
        return !originalFormula().isTrue(m);
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Negation otherC = (Negation) other;
        return originalFormula().equals(otherC.originalFormula());
    }

    public Formula toNnf(boolean isNegated) {
        return originalFormula.toNnf(!isNegated);
    }

    public Cnf nnfToCnf() {
        if (originalFormula instanceof AtomicFormula)
            return new Cnf(new Clause(new Literal((AtomicFormula) originalFormula, true)));
        throw new RuntimeException("Negation not atomic");
    }
}

class Disjunction extends Formula {
    private List<Formula> disjuncts;

    Disjunction(List<Formula> disjuncts) {
        this.disjuncts = disjuncts;
    }

    public List<Formula> disjuncts() {
        return disjuncts;
    }

    @Override
    public String toString() {
        return String.format("(%s)",
                             disjuncts()
                                     .stream()
                                     .map(d -> d.toString())
                                     .collect(Collectors.joining("|")));
    }

    @Override
    public boolean isTrue(Structure m) {
        return disjuncts().stream().anyMatch(d -> d.isTrue(m));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Disjunction otherC = (Disjunction) other;
        return disjuncts().equals(otherC.disjuncts());
    }

    public Formula toNnf(boolean isNegated) {
        var subFormulas = disjuncts
                                  .stream()
                                  .map(c -> c.toNnf(isNegated))
                                  .collect(Collectors.toList());

        if (isNegated)
            return new Conjunction(subFormulas);
        return new Disjunction(subFormulas);
    }

    public Cnf nnfToCnf() {
        Cnf result = new Cnf();
        var cnfs = disjuncts().stream().map(c -> c.nnfToCnf()).collect(Collectors.toList());

        result.add(new Clause());

        for (Cnf list: cnfs) {
            var temp = new Cnf();
            for (Clause res: result) {
                for (Clause item: list) {
                    Clause newCombination = new Clause(res);
                    newCombination.addAll(item);
                    temp.add(newCombination);
                }
            }
            result = temp;
        }

        return result;
    }
}

class Conjunction extends Formula {
    private List<Formula> conjuncts;

    Conjunction(List<Formula> conjuncts) {
        this.conjuncts = conjuncts;
    }

    public List<Formula> conjuncts() {
        return conjuncts;
    }

    @Override
    public String toString() {
        return String.format("(%s)",
                             conjuncts()
                                     .stream()
                                     .map(d -> d.toString())
                                     .collect(Collectors.joining("&")));
    }

    @Override
    public boolean isTrue(Structure m) {
        return conjuncts().stream().allMatch(d -> d.isTrue(m));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Conjunction otherC = (Conjunction) other;
        return conjuncts().equals(otherC.conjuncts());
    }

    public Formula toNnf(boolean isNegated) {
        var subFormulas = conjuncts()
                                  .stream()
                                  .map(c -> c.toNnf(isNegated))
                                  .collect(Collectors.toList());

        if (isNegated)
            return new Disjunction(subFormulas);
        return new Conjunction(subFormulas);
    }

    public Cnf nnfToCnf() {
        return conjuncts()
                .stream()
                .map(c -> c.nnfToCnf())
                .reduce(new Cnf(), (acc, v) -> {
                    acc.addAll(v);
                    return acc;
                });
    }
}

class BinaryFormula extends Formula {
    private Formula leftSide, rightSide;
    private String connective;

    BinaryFormula(Formula leftSide, Formula rightSide, String connective) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.connective = connective;
    }

    public String connective() {
        return connective;
    }

    public Formula leftSide() {
        return leftSide;
    }

    public Formula rightSide() {
        return rightSide;
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        BinaryFormula otherC = (BinaryFormula) other;
        return leftSide().equals(otherC.leftSide()) &&
                rightSide().equals(otherC.rightSide()) &&
                connective().equals(otherC.connective());
    }
}

class Implication extends BinaryFormula {
    Implication(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "->");
    }

    @Override
    public String toString() {
        return String.format("(%s->%s)", leftSide().toString(), rightSide().toString());
    }

    @Override
    public boolean isTrue(Structure m) {
        return !leftSide().isTrue(m) || rightSide().isTrue(m);
    }

    public Formula toNnf(boolean isNegated) {
        return new Disjunction(
                       List.of(
                               new Negation(leftSide()),
                               rightSide()))
                .toNnf(isNegated);
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "<->");
    }

    @Override
    public String toString() {
        return String.format("(%s<->%s)", leftSide().toString(), rightSide().toString());
    }

    @Override
    public boolean isTrue(Structure m) {
        return (leftSide().isTrue(m) && rightSide().isTrue(m)) ||
                (!leftSide().isTrue(m) && !rightSide().isTrue(m));
    }

    public Formula toNnf(boolean isNegated) {
        return new Conjunction(
                       List.of(
                               new Implication(leftSide(), rightSide()),
                               new Implication(rightSide(), leftSide())))
                .toNnf(isNegated);
    }
}
