import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class Term {
    private String name;

    public Term(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        return name() == ((Term) other).name();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public Set<String> variables() {
        return Set.of();
    }

    public Set<String> constants() {
        return Set.of();
    }

    public Set<String> functions() {
        return Set.of();
    }

    public <D> D eval(Structure<D> m, Map<String, D> e) {
        throw new RuntimeException("Not implemented");
    }

    public Term substitute(String v, Term t) {
        throw new RuntimeException("Not implemented");
    }

    public Term copy() {
        throw new RuntimeException("Not implemented");
    }
}

class Variable extends Term {
    Variable(String name) {
        super(name);
    }

    public Set<String> variables() {
        return Collections.singleton(name());
    }

    public <D> D eval(Structure<D> m, Map<String, D> e) {
        return e.get(name());
    }

    public Term substitute(String v, Term t) {
        if (v.equals(name())) {
            if (t instanceof Variable)
                return new Variable(t.name());
            else if (t instanceof Constant)
                return new Constant(t.name());
            else if (t instanceof FunctionApplication) {
                return new FunctionApplication(t.name(),
                                               ((FunctionApplication) t)
                                                       .subts()
                                                       .stream()
                                                       .map(tr -> tr.copy())
                                                       .toList());
            }
        }
        return this.copy();
    }

    @Override
    public Term copy() {
        return new Variable(name());
    }
}

class Constant extends Term {
    Constant(String name) {
        super(name);
    }

    public Set<String> constants() {
        return Collections.singleton(name());
    }

    public <D> D eval(Structure<D> m, Map<String, D> e) {
        return m.iC(name());
    }

    public Term substitute(String v, Term t) {
        return new Constant(name());
    }

    @Override
    public Term copy() {
        return new Constant(name());
    }
}

class FunctionApplication extends Term {
    private List<Term> subts;

    FunctionApplication(String name, List<Term> subts) {
        super(name);
        this.subts = subts;
    }

    public List<Term> subts() {
        return subts;
    }

    @Override
    public Set<String> variables() {
        return subts()
                .stream()
                .flatMap(f -> f.variables().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> constants() {
        return subts()
                .stream()
                .flatMap(f -> f.constants().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> functions() {
        Set<String> fns = subts()
                                  .stream()
                                  .flatMap(f -> f.functions().stream())
                                  .collect(Collectors.toSet());
        fns.add(name());
        return fns;
    }

    @Override
    public <D> D eval(Structure<D> m, Map<String, D> e) {
        return m.iF(name()).get(subts().stream().map(f -> f.eval(m, e)).toList());
    }

    @Override
    public String toString() {
        String subtsString = subts().stream().map(Object::toString).collect(Collectors.joining(","));
        return String.format("%s(%s)", name(), subtsString);
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        FunctionApplication otherC = (FunctionApplication) other;
        return subts().equals(otherC.subts());
    }

    @Override
    public Term substitute(String v, Term t) {
        return new FunctionApplication(name(), subts().stream().map(tr -> tr.substitute(v, t)).toList());
    }

    @Override
    public Term copy() {
        return new FunctionApplication(name(), subts().stream().map(t -> t.copy()).toList());
    }
}

class Formula {
    public List<Formula> subfs() {
        return switch (this) {
            case Negation n -> List.of(n.originalFormula());
            case Disjunction d -> d.disjuncts();
            case Conjunction c -> c.conjuncts();
            case BinaryFormula b -> List.of(b.leftSide(), b.rightSide());
            case QuantifiedFormula q -> List.of(q.originalFormula());
            default -> List.of();
        };
    }

    @Override
    public String toString() {
        throw new RuntimeException("Not implemented");
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

    public Set<String> variables() {
        return subfs()
                .stream()
                .flatMap(f -> f.variables().stream())
                .collect(Collectors.toSet());
    }

    public Set<String> constants() {
        return subfs()
                .stream()
                .flatMap(f -> f.constants().stream())
                .collect(Collectors.toSet());
    }

    public Set<String> functions() {
        return subfs()
                .stream()
                .flatMap(f -> f.functions().stream())
                .collect(Collectors.toSet());
    }

    public Set<String> predicates() {
        return subfs()
                .stream()
                .flatMap(f -> f.predicates().stream())
                .collect(Collectors.toSet());
    }

    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> freeVariables() {
        return subfs()
                .stream()
                .flatMap(f -> f.freeVariables().stream())
                .collect(Collectors.toSet());
    }

    public Formula substitute(String var, Term t) throws NotApplicableException {
        throw new RuntimeException("Not implemented");
    }

    public Formula copy() {
        throw new RuntimeException("Not implemented");
    }
}

class AtomicFormula extends Formula {
    public List<Term> subts() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<String> variables() {
        return subts()
                .stream()
                .flatMap(t -> t.variables().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> constants() {
        return subts()
                .stream()
                .flatMap(t -> t.constants().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> functions() {
        return subts()
                .stream()
                .flatMap(t -> t.functions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> freeVariables() {
        return variables();
    }
}

class PredicateAtom extends AtomicFormula {
    private String name;
    private List<Term> subts;

    public PredicateAtom(String name, List<Term> subts) {
        this.name = name;
        this.subts = subts;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        String subtsString = subts().stream().map(Object::toString).collect(Collectors.joining(","));
        return String.format("%s(%s)", name(), subtsString);
    }

    @Override
    public List<Term> subts() {
        return subts;
    }

    @Override
    public Set<String> predicates() {
        return Set.of(name());
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return m.iP(name()).contains(subts().stream().map(t -> t.eval(m, e)).toList());
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        PredicateAtom otherC = (PredicateAtom) other;
        return name().equals(otherC.name()) && subts().equals(otherC.subts());
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        return new PredicateAtom(name, subts().stream().map(tr -> tr.substitute(var, t)).toList());
    }

    public PredicateAtom copy() {
        return new PredicateAtom(name, subts().stream().map(t -> t.copy()).toList());
    }
}

class EqualityAtom extends AtomicFormula {
    private Term leftTerm, rightTerm;

    EqualityAtom(Term leftTerm, Term rightTerm) {
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
    }

    Term leftTerm() {
        return leftTerm;
    }

    Term rightTerm() {
        return rightTerm;
    }

    @Override
    public String toString() {
        return String.format("%s=%s", leftTerm(), rightTerm());
    }

    @Override
    public List<Term> subts() {
        return List.of(leftTerm(), rightTerm());
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return leftTerm().eval(m, e).equals(rightTerm().eval(m, e));
    }

    @Override
    public Set<String> predicates() {
        return Set.of();
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        EqualityAtom otherC = (EqualityAtom) other;
        return leftTerm().equals(otherC.leftTerm()) && rightTerm().equals(otherC.rightTerm());
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        return new EqualityAtom(leftTerm().substitute(var, t), rightTerm().substitute(var, t));
    }

    public EqualityAtom copy() {
        return new EqualityAtom(leftTerm().copy(), rightTerm().copy());
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
        return String.format("-%s", originalFormula());
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return !originalFormula().isSatisfied(m, e);
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Negation otherC = (Negation) other;
        return originalFormula().equals(otherC.originalFormula());
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        return new Negation(originalFormula().substitute(var, t));
    }

    public Negation copy() {
        return new Negation(originalFormula);
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
                             disjuncts
                                     .stream()
                                     .map(Object::toString)
                                     .collect(Collectors.joining("|")));
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return disjuncts().stream().anyMatch(f -> f.isSatisfied(m, e));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Disjunction otherC = (Disjunction) other;
        return disjuncts().equals(otherC.disjuncts());
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        List<Formula> substituted = new ArrayList<>();
        for (var f: disjuncts()) substituted.add(f.substitute(var, t));
        return new Disjunction(substituted);
    }

    public Disjunction copy() {
        return new Disjunction(disjuncts().stream().map(d -> d.copy()).toList());
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
                             conjuncts
                                     .stream()
                                     .map(Object::toString)
                                     .collect(Collectors.joining("&")));
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return conjuncts().stream().allMatch(f -> f.isSatisfied(m, e));
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        Conjunction otherC = (Conjunction) other;
        return conjuncts().equals(otherC.conjuncts());
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        List<Formula> substituted = new ArrayList<>();
        for (var f: conjuncts()) substituted.add(f.substitute(var, t));
        return new Conjunction(substituted);
    }

    public Conjunction copy() {
        return new Conjunction(conjuncts().stream().map(d -> d.copy()).toList());
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

    public Formula leftSide() {
        return leftSide;
    }

    public Formula rightSide() {
        return rightSide;
    }

    @Override
    public String toString() {
        return String.format("(%s%s%s)", leftSide(), connective, rightSide());
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        BinaryFormula otherC = (BinaryFormula) other;
        return leftSide().equals(otherC.leftSide()) &&
                rightSide().equals(otherC.rightSide()) &&
                connective.equals(otherC.connective);
    }
}

class Implication extends BinaryFormula {
    Implication(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "->");
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return !leftSide().isSatisfied(m, e) || rightSide().isSatisfied(m, e);
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        return new Implication(leftSide().substitute(var, t),
                               rightSide().substitute(var, t));
    }

    public Implication copy() {
        return new Implication(leftSide().copy(), rightSide().copy());
    }
}

class Equivalence extends BinaryFormula {
    Equivalence(Formula leftSide, Formula rightSide) {
        super(leftSide, rightSide, "<->");
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        return leftSide().isSatisfied(m, e) == rightSide().isSatisfied(m, e);
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        return new Equivalence(leftSide().substitute(var, t),
                               rightSide().substitute(var, t));
    }

    public Equivalence copy() {
        return new Equivalence(leftSide().copy(), rightSide().copy());
    }
}

class QuantifiedFormula extends Formula {
    private String quantifier, qvar;
    private Formula originalFormula;

    QuantifiedFormula(String quantifier, String qvar, Formula originalFormula) {
        this.quantifier = quantifier;
        this.qvar = qvar;
        this.originalFormula = originalFormula;
    }

    public Formula originalFormula() {
        return originalFormula;
    }

    public String qvar() {
        return qvar;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s", quantifier, qvar, originalFormula);
    }

    @Override
    public Set<String> variables() {
        Set<String> originalVars = super.variables();
        originalVars.add(qvar());
        return originalVars;
    }

    @Override
    public Set<String> freeVariables() {
        Set<String> originalFreeVars = originalFormula().freeVariables();
        originalFreeVars.remove(qvar());
        return originalFreeVars;
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) return false;
        QuantifiedFormula otherC = (QuantifiedFormula) other;
        return originalFormula().equals(otherC.originalFormula()) &&
                qvar().equals(otherC.qvar()) &&
                quantifier.equals(otherC.quantifier);
    }

    public Formula copy() {
        return new QuantifiedFormula(quantifier, qvar(), originalFormula().copy());
    }
}

class ForAll extends QuantifiedFormula {
    ForAll(String qvar, Formula originalFormula) {
        super("∀", qvar, originalFormula);
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        HashMap<String, D> newEvaluation = new HashMap<>(e);
        return m.domain().stream().allMatch(d -> {
            newEvaluation.put(qvar(), d);
            return originalFormula().isSatisfied(m, newEvaluation);
        });
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        if (t.variables().contains(qvar()) && this.freeVariables().contains(var))
            throw new NotApplicableException(this, var, t);

        if (var.equals(qvar()))
            return new ForAll(qvar(), originalFormula().copy());

        return new ForAll(qvar(), originalFormula().substitute(var, t));
    }

    public ForAll copy() {
        return new ForAll(qvar(), originalFormula().copy());
    }
}

class Exists extends QuantifiedFormula {
    Exists(String qvar, Formula originalFormula) {
        super("∃", qvar, originalFormula);
    }

    @Override
    public <D> boolean isSatisfied(Structure<D> m, Map<String, D> e) {
        HashMap<String, D> newEvaluation = new HashMap<>(e);
        return m.domain().stream().anyMatch(d -> {
            newEvaluation.put(qvar(), d);
            return originalFormula().isSatisfied(m, newEvaluation);
        });
    }

    @Override
    public Formula substitute(String var, Term t) throws NotApplicableException {
        if (t.variables().contains(qvar()) && this.freeVariables().contains(var))
            throw new NotApplicableException(this, var, t);

        if (var.equals(qvar()))
            return new Exists(qvar(), originalFormula().copy());

        return new Exists(qvar(), originalFormula().substitute(var, t));
    }

    public Exists copy() {
        return new Exists(qvar(), originalFormula().copy());
    }
}
