#!/usr/bin/env python3

import os
import sys

sys.path[0:0] = [os.path.join(sys.path[0], "..", "..", "..", "examples", "sat")]
import sat

Agatha = 0
Butler = 1
Charles = 2
People = [Agatha, Butler, Charles]
PeopleNames = ["Agatha", "Butler", "Charles"]
P = len(People)


def killed(p1, p2):
    return 0 * P * P + p2 * P + p1 + 1


def hates(p1, p2):
    return 1 * P * P + p1 * P + p2 + 1


def richer(p1, p2):
    return 2 * P * P + p1 * P + p2 + 1


class MurderMystery(object):
    def writeTheory(self, w: sat.DimacsWriter) -> None:
        """Zapise teoriu do DimacsWriter-a w."""

        # Someone in Dreadsbury Mansion killed Aunt Agatha.
        # Ex killed(x,Agatha)
        # t.j.  ( killed(Agatha,Agatha) v killed(Butler,Agatha) v ...)
        w.writeClause(killed(x, Agatha) for x in People)

        # Agatha, the butler, and Charles live in Dreadsbury Mansion, and are the only ones to live there.
        #  -- toto nezapisujeme, hovori nam to koho dosadzujeme

        # A killer always hates, and is no richer than his victim.
        # ∀x∀y (killed(x, y) -> hates(x, y) ∧  ¬richer(x, y))
        for x in People:
            for y in People:
                w.writeImpl(killed(x, y), hates(x, y))
                w.writeImpl(killed(x, y), -richer(x, y))

        # Charles hates noone that Agatha hates.
        # ∀x (hates(Agatha, x) -> ¬hates(Charles, x))
        for x in People:
            w.writeImpl(hates(Agatha, x), -hates(Charles, x))

        # Agatha hates everybody except the butler.
        # ∀x (x != Butler -> hates(Agatha, x))
        for x in People:
            if x != Butler:
                w.writeClause([hates(Agatha, x)])

        # The butler hates everyone not richer than Aunt Agatha.
        # ∀x (¬richer(x, Agatha) -> hates(Butler, x))
        for x in People:
            w.writeImpl(-richer(x, Agatha), hates(Butler, x))

        # The butler hates everyone whom Agatha hates.
        # ∀x (hates(Agatha, x) -> hates(Butler, x))
        for x in People:
            w.writeImpl(hates(Agatha, x), hates(Butler, x))

        # Noone hates everyone.
        # ∀xEy ¬hates(x, y)
        for x in People:
            w.writeClause(-hates(x, y) for y in People)

        # Who killed Agatha?

    def prove(self) -> str:
        solver = sat.SatSolver()

        # test ci ma model
        w = sat.DimacsWriter("test_theory_in.txt")
        self.writeTheory(w)

        ok, sol = solver.solve(w, "test_theory_out.txt")

        if not ok:
            return "Theory doesn't have a model"

        possibleKillers = map(lambda x: x - 1, filter(lambda x: 1 <= x <= 3, sol))

        # dokazovanie moznych vrahov
        killers: [str] = list()
        for p in possibleKillers:
            w = sat.DimacsWriter("test_theory_in.txt")
            self.writeTheory(w)

            # negacia dokazovaneho tvrdenia
            w.writeClause([-killed(p, Agatha)])
            ok, _ = solver.solve(w, "test_theory_out.txt")

            if not ok:
                killers.append(PeopleNames[p])

        # "... má vrátiť zoznam všetkých osôb ..." ?
        return killers[0]


if __name__ == "__main__":
    mm = MurderMystery()
    print(mm.prove())
