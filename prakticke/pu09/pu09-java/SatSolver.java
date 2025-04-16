import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class SatSolver {
    public static class Result {
        public boolean sat;
        public Map<String, Boolean> valuation;
        public Result(boolean sat, Map<String, Variable> vars) {
            this.sat = sat;
            valuation = new HashMap<String, Boolean>();
            for (String k: vars.keySet()) {
                valuation.put(k, vars.get(k).val());
            }
        }
        public static Result sat(Map<String, Variable> vars) { return new Result(true, vars); }
        public static Result unsat() { return new Result(false, Collections.emptyMap()); }
    }

    public Result solve(Theory t) {
        var units = new HashSet<UnitClause>();
        if (!t.initWatched(units)) return Result.unsat();
        unitPropagate(t, units);

        return recurse(t, 0);
    }

    private Result recurse(Theory t, int varIdx) {
        if (varIdx >= t.vars().size())
            return Result.sat(t.vars());

        int back = t.nAssigned();
        var vars = t.vars().values().stream().toList();
        for (int i = varIdx; i < vars.size(); i++) {
            if (!vars.get(i).isSet()) {
                varIdx = i;
                break;
            }
        }

        var variable = vars.get(varIdx);
        for (boolean sign: List.of(true, false)) {
            var units = new HashSet<UnitClause>();
            boolean isOk = t.setLiteral(variable.lit(sign), units);

            if (isOk && unitPropagate(t, units)) {
                Result res = recurse(t, varIdx + 1);
                if (res.sat) return res;
            }

            while (t.nAssigned() > back)
                t.unsetLiteral();
        }

        return Result.unsat();
    }

    private boolean unitPropagate(Theory t, Set<UnitClause> units) {
        Queue<UnitClause> unitQueue = new LinkedList<>(units);

        while (!unitQueue.isEmpty()) {
            var unit = unitQueue.poll();

            var newUnits = new HashSet<UnitClause>();
            if (!t.setLiteral(unit.unsetLiteral(), newUnits)) return false;
            unitQueue.addAll(newUnits);
        }

        return true;
    }
}
