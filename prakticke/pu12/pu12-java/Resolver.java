import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Resolver {

    public static Set<Clause> resolve(Clause a, Clause b) {
        Set<Clause> result = new HashSet<>();

        for (Literal lit: a) {
            if (b.contains(lit.not())) {
                Set<Literal> newLits = new HashSet<>(a);
                newLits.remove(lit);
                for (Literal l: b) {
                    if (!l.equals(lit.not())) {
                        newLits.add(l);
                    }
                }
                result.add(new Clause(newLits));
            }
        }

        return result;
    }

    public static boolean isSatisfiable(Cnf theory) {
        Set<Clause> clauses = new HashSet<>(theory);
        List<Clause> clauseList = new ArrayList<>(clauses);

        int idx = 0;
        while (idx < clauseList.size()) {
            Clause ci = clauseList.get(idx);
            if (ci.isEmpty()) return false;
            for (int j = 0; j < idx; j++) {
                Clause cj = clauseList.get(j);
                if (cj.isEmpty()) return false;
                Set<Clause> resolvents = resolve(ci, cj);
                if (resolvents.contains(new Clause())) return false;
                resolvents.removeAll(clauses);
                clauses.addAll(resolvents);
                clauseList.addAll(resolvents);
            }
            idx++;
        }
        return true;
    }
}
