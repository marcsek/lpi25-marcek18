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
        if (this instanceof Negation) {
            return List.of(((Negation) this).originalFormula());
        } else if (this instanceof Disjunction) {
            return ((Disjunction) this).disjuncts();
        } else if (this instanceof Conjunction) {
            return ((Conjunction) this).conjuncts();
        } else if (this instanceof BinaryFormula) {
            BinaryFormula b = (BinaryFormula) this;
            return List.of(b.leftSide(), b.rightSide());
        } else {
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
     * Return the type (of a signed formula),
     * as if this was a signed formula signed with sign.
     *
     * @param sign the sign of the signed formula to be considered
     * @return SignedFormula.Type.Alpha or SignedFormula.Type.Beta
     */
    public SignedFormula.Type signedType(boolean sign) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Return a list of signed sub-formulas of this
     * formula, if this formula was signed with sign
     *
     * @param sign the sign of the signed formula to be considered
     * @return list of signed sub-formulas
     */
    public List<SignedFormula> signedSubfs(boolean sign) {
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return List.of();
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
}

class EqualityAtom extends AtomicFormula {
    private Constant left, right;

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

    @Override
    public String toString() {
        return String.format("%s=%s", left().name(), right().name());
    }

    @Override
    public boolean isTrue(Structure m) {
        return left.eval(m).equals(right.eval(m));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        EqualityAtom otherC = (EqualityAtom) other;
        return left().equals(otherC.left()) && right().equals(otherC.right());
    }

    @Override
    public Set<String> constants() {
        return Set.of(left.name(), right.name());
    }

    @Override
    public Set<String> predicates() {
        return Set.of();
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return List.of(new SignedFormula(!sign, originalFormula()));
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? SignedFormula.Type.Beta : SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return disjuncts()
                .stream()
                .map(d -> new SignedFormula(sign, d))
                .collect(Collectors.toList());
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? SignedFormula.Type.Alpha : SignedFormula.Type.Beta;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return conjuncts()
                .stream()
                .map(c -> new SignedFormula(sign, c))
                .collect(Collectors.toList());
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? SignedFormula.Type.Beta : SignedFormula.Type.Alpha;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return List.of(
                new SignedFormula(!sign, leftSide()),
                new SignedFormula(sign, rightSide()));
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

    @Override
    public SignedFormula.Type signedType(boolean sign) {
        return (sign) ? SignedFormula.Type.Alpha : SignedFormula.Type.Beta;
    }

    @Override
    public List<SignedFormula> signedSubfs(boolean sign) {
        return List.of(
                new SignedFormula(sign, new Implication(leftSide(), rightSide())),
                new SignedFormula(sign, new Implication(rightSide(), leftSide())));
    }
}
