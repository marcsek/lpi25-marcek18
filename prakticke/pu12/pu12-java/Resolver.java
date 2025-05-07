import java.util.*;

public class Resolver {

    public static Set<Clause> resolve(Clause a, Clause b) {
        var result = new HashSet<Clause>();
        var resolvable = a.stream().filter(l -> b.contains(l.not())).toList();

        for (var lit: resolvable) {
            var newLits = new HashSet<>(a);
            newLits.remove(lit);
            var newLitsB = new HashSet<>(b);
            newLitsB.remove(lit.not());
            newLits.addAll(newLitsB);
            result.add(new Clause(newLits));
        }

        return result;
    }

    public static boolean isSatisfiable(Cnf theory) {
        var clauses = new ArrayList<>(theory);
        var seen = new HashSet<>(theory);

        for (int i = 0; i < clauses.size(); i++) {
            var c1 = clauses.get(i);
            if (c1.isEmpty()) return false;

            for (int j = i - 1; j >= 0; j--) {
                var c2 = clauses.get(j);
                if (c2.isEmpty()) return false;

                var resolvents = resolve(c1, c2);
                if (resolvents.contains(new Clause())) return false;

                // ak je iba nadmnozina uz videneho tak skip
                resolvents.removeIf(r -> seen.stream().anyMatch(c -> r.containsAll(c)));

                resolvents.removeAll(seen);
                seen.addAll(resolvents);
                clauses.addAll(resolvents);
            }
        }
        return true;
    }
}
